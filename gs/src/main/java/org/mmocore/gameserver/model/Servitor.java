package org.mmocore.gameserver.model;

import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_PVP_FLAG;
import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_SIEGE_FLAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.ai.ServitorAI;
import org.mmocore.gameserver.handler.onshiftaction.OnShiftActionHolder;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.actor.recorder.SummonStatsChangeRecorder;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.impl.SingleMatchEvent;
import org.mmocore.gameserver.model.instances.PetInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.PetInventory;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.*;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.taskmanager.DecayTaskManager;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.npc.NpcTemplate;


public abstract class Servitor extends Playable
{
	public static final int[] BEAST_SHOTS = { 6645, 6646, 6647, 20332, 20333, 20334 };

	private static final int SUMMON_DISAPPEAR_RANGE = 2500;

	public static final int SUMMON_TYPE = 1;
	public static final int PET_TYPE = 2;

	private final Player _owner;

	private int _spawnAnimation = 2;
	protected long _exp = 0;
	protected int _sp = 0;
	private int _maxLoad, _spsCharged;
	private boolean _follow = true, _depressed, _ssCharged;

	private Future<?> _decayTask;

	public Servitor(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		setTitle(owner.getName());
		_owner = owner;

		if(template.getSkills().size() > 0)
			for(SkillEntry skillEntry : template.getSkills().values())
				addSkill(skillEntry);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_spawnAnimation = 0;

		Player owner = getPlayer();
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public ServitorAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new ServitorAI(this);
			}

		return (ServitorAI) _ai;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public abstract int getServitorType();

	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, (Class<Servitor>) getClass(), this, true))
			return;

		Player owner = getPlayer();

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP, StatusUpdate.CUR_MP, StatusUpdate.MAX_MP));
			}
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else if(player == owner)
		{
			player.sendPacket(new PetInfo(this).update());

			if(!isInRangeZ(player, INTERACTION_DISTANCE))
			{
				if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				return;
			}

			if(!player.isActionsDisabled())
				player.sendPacket(new PetStatusShow(this));

			player.sendPacket(ActionFail.STATIC);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else
					player.sendActionFailed();
			}
			else
				player.sendActionFailed();
		}
	}

	public long getExpForThisLevel()
	{
		return Experience.getExpForLevel(getLevel());
	}

	public long getExpForNextLevel()
	{
		return Experience.getExpForLevel(getLevel() + 1);
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(final long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	@Override
	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	@Override
	public int getBuffLimit()
	{
		Player owner = getPlayer();
		return (int) calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	public abstract void saveEffects();

	public abstract int getId(); // идентификатор сервитора: skillId для самона и itemId для пета

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		startDecay(8500L);

		Player owner = getPlayer();

		if(killer == null || killer == owner || killer == this || isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof Servitor)
			killer = killer.getPlayer();

		if(killer == null)
			return;

		if(killer.isPlayer())
		{
			Player pk = (Player) killer;

			if(isInZone(ZoneType.SIEGE))
				return;

			if (owner.getPvpFlag() == 0 && !owner.atMutualWarWith(pk) && getKarma() <= 0)
			{
				final SingleMatchEvent matchEvent = getEvent(SingleMatchEvent.class);
				if(matchEvent == null || matchEvent != pk.getEvent(SingleMatchEvent.class))
				{
					int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
					pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
				}
			}

			// Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
			pk.sendChanges();
		}
	}

	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	protected void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	@Override
	protected void onDecay()
	{
		deleteMe();
	}

	public void endDecayTask()
	{
		stopDecay();
		doDecay();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		Player owner = getPlayer();

		sendStatusUpdate();

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.CUR_HP);
		broadcastToStatusListeners(su);

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
	}

	public void sendStatusUpdate()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetStatusUpdate(this));
	}

	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		final boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		final boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;

		final boolean isInCombatZone = isInCombatZone();
		final boolean isOnSiegeField = isOnSiegeField();

		_zoneMask = 0;

		if(isInCombatZone)
			_zoneMask |= ZONE_PVP_FLAG;
		if(isOnSiegeField)
			_zoneMask |= ZONE_SIEGE_FLAG;

		if(lastInCombatZone != isInCombatZone || lastOnSiegeField != isOnSiegeField)
			broadcastCharInfo();
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
		owner.sendPacket(new PetDelete(getObjectId(), getServitorType()));
		owner.setServitor(null);

		for (int itemId : BEAST_SHOTS)
			if (owner.getAutoSoulShot().contains(itemId))
			{
				owner.removeAutoSoulShot(itemId);
				owner.sendPacket(new ExAutoSoulShot(itemId, false));
			}

		stopDecay();
		super.onDelete();
	}

	public void unSummon(boolean saveEffects, boolean store)
	{
		if(saveEffects)
			saveEffects();

		deleteMe();
	}

	public void setFollowMode(boolean state)
	{
		Player owner = getPlayer();

		_follow = state;

		getAI().clearStoredIntention();
		if(_follow)
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || (getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW && getFollowTarget() != owner))
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
		}
		else if(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW && getFollowTarget() == owner)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public boolean isFollowMode()
	{
		return _follow;
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateEffectIconsImpl()
	{
		Player owner = getPlayer();
		PartySpelled ps = new PartySpelled(this, true);
		Party party = owner.getParty();
		if(party != null)
			party.broadCast(ps);
		else
			owner.sendPacket(ps);
	}

	public int getControlItemObjId()
	{
		return 0;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(final GameObject object)
	{}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setRunning();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(true);
	}

	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public abstract void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic);

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		Player owner = getPlayer();

		if(spirit)
		{
			if(_spsCharged != 0)
			{
				_spsCharged = 0;
				owner.autoShot();
				return true;
			}
		}
		else if(_ssCharged)
		{
			_ssCharged = false;
			owner.autoShot();
			return true;
		}

		return false;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		return _ssCharged;
	}

	@Override
	public int getChargedSpiritShot(boolean first)
	{
		return _spsCharged;
	}

	public void chargeSoulShot()
	{
		_ssCharged = true;
	}

	public void chargeSpiritShot(final int state)
	{
		_spsCharged = state;
	}

	public int getSoulshotConsumeCount()
	{
		return 0;
	}

	public int getSpiritshotConsumeCount()
	{
		return 0;
	}

	public boolean isDepressed()
	{
		return _depressed;
	}

	public void setDepressed(final boolean depressed)
	{
		_depressed = depressed;
	}

	public boolean isInRange()
	{
		Player owner = getPlayer();
		return getDistance(owner) < SUMMON_DISAPPEAR_RANGE;
	}

	public void teleportToOwner()
	{
		Player owner = getPlayer();

		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		teleToLocation(owner.getLoc(), owner.getReflection());

		if(!isDead() && _follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public void broadcastCharInfoImpl()
	{
		broadcastCharInfoImpl(World.getAroundObservers(this));
	}

	public void broadcastCharInfoImpl(Iterable<Player> players)
	{
		Player owner = getPlayer();

		for(Player player : players)
			if(player == owner)
				player.sendPacket(new PetInfo(this).update());
			else
				player.sendPacket(new NpcInfo(this, player).update());
	}

	private Future<?> _petInfoTask;

	private class PetInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendPetInfoImpl();
			_petInfoTask = null;
		}
	}

	private void sendPetInfoImpl()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetInfo(this).update());
	}

	public void sendPetInfo()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_petInfoTask != null)
			{
				_petInfoTask.cancel(false);
				_petInfoTask = null;
			}
			sendPetInfoImpl();
			return;
		}

		if(_petInfoTask != null)
			return;

		_petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(), Config.USER_INFO_INTERVAL);
	}

	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo, PetInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		Player owner = getPlayer();
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		Player owner = getPlayer();
		return owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		Player owner = getPlayer();
		return owner.getKarma();
	}

	@Override
	public TeamType getTeam()
	{
		Player owner = getPlayer();
		return owner.getTeam();
	}

	@Override
	public Player getPlayer()
	{
		return _owner;
	}

	public abstract double getExpPenalty();

	@Override
	public SummonStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new SummonStatsChangeRecorder(this);
			}

		return (SummonStatsChangeRecorder) _statsRecorder;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		Player owner = getPlayer();

		if(owner == forPlayer)
		{
			list.add(new PetInfo(this));
			list.add(new PartySpelled(this, true));

			if(isPet())
				list.add(new PetItemList((PetInstance) this));
		}
		else
		{
			Party party = forPlayer.getParty();
			if(getReflection() == ReflectionManager.GIRAN_HARBOR && (owner == null || party == null || party != owner.getParty()))
				return list;
			list.add(new NpcInfo(this, forPlayer));
			if(owner != null && party != null && party == owner.getParty())
				list.add(new PartySpelled(this, true));
		}

		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));

		if(isMoving || isFollow)
			list.add(movePacket());
		return list;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Player player = getPlayer();
		if(player != null)
			player.startAttackStanceTask0();
	}

	@Override
	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		else
			return super.getEvent(eventClass);
	}

	@Override
	public Set<Event> getEvents()
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		else
			return super.getEvents();
	}

	@Override
	public void sendReuseMessage(SkillEntry skill)
	{
		Player player = getPlayer();
		if(player != null)
			player.sendPacket(SystemMsg.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
	}

	@Override
	public boolean startFear()
	{
		final boolean result = super.startFear();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopFear()
	{
		final boolean result = super.stopFear();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startRooted()
	{
		final boolean result = super.startRooted();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopRooted()
	{
		final boolean result = super.stopRooted();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startSleeping()
	{
		final boolean result = super.startSleeping();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopSleeping()
	{
		final boolean result = super.stopSleeping();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startStunning()
	{
		final boolean result = super.startStunning();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopStunning()
	{
		final boolean result = super.stopStunning();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startParalyzed()
	{
		final boolean result = super.startFear();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopParalyzed()
	{
		final boolean result = super.stopFear();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean isServitor()
	{
		return true;
	}
}