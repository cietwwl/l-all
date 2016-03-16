package org.mmocore.gameserver.model.instances;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CharacterAI;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.data.xml.holder.MultiSellHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.handler.bypass.BypassHolder;
import org.mmocore.gameserver.handler.onshiftaction.OnShiftActionHolder;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.instancemanager.DimensionalRiftManager;
import org.mmocore.gameserver.instancemanager.QuestManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.listener.NpcListener;
import org.mmocore.gameserver.model.*;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.actor.listener.NpcListenerList;
import org.mmocore.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.DimensionalRift;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.SevenSigns;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.objects.TerritoryWardObject;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.entity.residence.ClanHall;
import org.mmocore.gameserver.model.entity.residence.Dominion;
import org.mmocore.gameserver.model.entity.residence.Fortress;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestEventType;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.network.l2.c2s.L2GameClientPacket;
import org.mmocore.gameserver.network.l2.c2s.RequestRefine;
import org.mmocore.gameserver.network.l2.c2s.RequestRefineCancel;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.*;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.taskmanager.DecayTaskManager;
import org.mmocore.gameserver.taskmanager.LazyPrecisionTaskManager;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.npc.Faction;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.templates.spawn.SpawnRange;
import org.mmocore.gameserver.utils.CertificationFunctions;
import org.mmocore.gameserver.utils.HtmlUtils;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.NpcUtils;
import org.mmocore.gameserver.utils.PositionUtils;
import org.mmocore.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcInstance extends Creature
{
	public static final String NO_CHAT_WINDOW = "noChatWindow";
	public static final String NO_RANDOM_WALK = "noRandomWalk";
	public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
	public static final String NO_SHIFT_CLICK = "noShiftClick";
	public static final String NO_LETHAL = "noLethal";
	public static final String TARGETABLE = "TargetEnabled";
	public static final String SHOW_NAME = "showName";

	private static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);

	private int _personalAggroRange = -1;
	private int _level = 0;

	private long _dieTime = 0L;

	protected int _spawnAnimation = 2;

	private int _currentLHandId;
	private int _currentRHandId;

	private double _currentCollisionRadius;
	private double _currentCollisionHeight;

	private int npcState = 0;

	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	protected boolean _hasChatWindow;

	protected Future<?> _decayTask;
	private Future<?> _animationTask;

	private AggroList _aggroList;

	private boolean _isTargetable;
	private boolean _noShiftClick;
	private boolean _noLethal;

	private boolean _showName;

	private Castle _nearestCastle;
	private Fortress _nearestFortress;
	private ClanHall _nearestClanHall;
	private Dominion _nearestDominion;

	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;

	protected Spawner _spawn;
	protected Location _spawnedLoc = new Location();
	protected SpawnRange _spawnRange;

	private NpcInstance _master = null;
	private MinionList _minionList = null;

	private MultiValueSet<String> _parameters = StatsSet.EMPTY;
	private boolean _itemDropEnabled = true;

	public NpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		if(template == null)
			throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");

		setUndying(SpecialEffectState.TRUE);
		setParameters(template.getAIParams());

		_hasRandomAnimation = !getParameter(NO_RANDOM_ANIMATION, false) && Config.MAX_NPC_ANIMATION > 0;
		_hasRandomWalk = !getParameter(NO_RANDOM_WALK, false);
		_noShiftClick = getParameter(NO_SHIFT_CLICK, false);
		_noLethal = getParameter(NO_LETHAL, false);
		setHasChatWindow(!getParameter(NO_CHAT_WINDOW, false));
		setTargetable(getParameter(TARGETABLE, true), false);
		setShowName(getParameter(SHOW_NAME, true));

		if(template.getSkills().size() > 0)
			for(SkillEntry skillEntry : template.getSkills().values())
				addSkill(skillEntry);

		setName(template.name);
		setTitle(template.title);

		// инициализация параметров оружия
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);

		// инициализация коллизий
		setCollisionHeight(getTemplate().collisionHeight);
		setCollisionRadius(getTemplate().collisionRadius);

		_aggroList = new AggroList(this);

		setFlying(getParameter("isFlying", false));

		if (!template.getMinionData().isEmpty())
			getMinionList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}

	@Override
	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = getTemplate().getNewAI(this);
			}

		return _ai;
	}

	/**
	 * Return the position of the spawned point.<BR><BR>
	 * Может возвращать случайную точку, поэтому всегда следует кешировать результат вызова!
	 */
	public Location getSpawnedLoc()
	{
		return getLeader() != null ? getLeader().getLoc() : _spawnedLoc;
	}

	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}

	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public void setCollisionHeight(double offset)
	{
		_currentCollisionHeight = offset;
	}

	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public void setCollisionRadius(double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, SkillEntry skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(attacker.isPlayable())
			getAggroList().addDamageHate(attacker, (int) damage, 0);

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_dieTime = System.currentTimeMillis();

		if(isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
			startDecay(20000L);
		else if(isBoss())
			startDecay(20000L);
		else if(isFlying())
			startDecay(4500L);
		else
			startDecay(8500L);

		// установка параметров оружия и коллизий по умолчанию
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		setCollisionHeight(getTemplate().collisionHeight);
		setCollisionRadius(getTemplate().collisionRadius);

		getAI().stopAITask();
		stopAttackStanceTask();
		stopRandomAnimation();

		if(getLeader() != null)
			getLeader().notifyMinionDied(this);

		super.onDeath(killer);
	}

	public long getDeadTime()
	{
		if(_dieTime <= 0L)
			return 0L;
		return System.currentTimeMillis() - _dieTime;
	}

	public AggroList getAggroList()
	{
		return _aggroList;
	}

	public void setLeader(NpcInstance leader)
	{
		_master = leader;
	}

	public NpcInstance getLeader()
	{
		return _master;
	}

	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	public MinionList getMinionList()
	{
		if (_minionList == null)
			_minionList = new MinionList(this);

		return _minionList;
	}

	public boolean hasMinions()
	{
		return _minionList != null && _minionList.hasMinions();
	}

	public void notifyMinionDied(NpcInstance minion)
	{

	}

	public Location getRndMinionPosition()
	{
		return Location.findPointToStay(this, (int)getColRadius() + 30, (int)getColRadius() + 50);
	}

	public void spawnMinion(NpcInstance minion)
	{
		minion.setReflection(getReflection());
		minion.setHeading(getHeading());
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
		minion.spawnMe(getRndMinionPosition());
		if (isRunning())
			minion.setRunning();
	}

	@Override
	public void setReflection(Reflection reflection)
	{
		super.setReflection(reflection);

		if(hasMinions())
			for(NpcInstance m : getMinionList().getAliveMinions())
				m.setReflection(reflection);
	}

	public void dropItem(Player lastAttacker, int itemId, long itemCount, boolean forceAutoLoot)
	{
		if(itemCount == 0 || lastAttacker == null || !_itemDropEnabled)
			return;

		if (lastAttacker.isAutoLootEnabled() == Player.AUTO_LOOT_ALL_EXCEPT_ARROWS && ItemFunctions.isArrow(itemId))
			return;

		ItemInstance item;

		for(long i = 0; i < itemCount; i++)
		{
			item = ItemFunctions.createItem(itemId);
			for(Event e : getEvents())
				item.addEvent(e);

			// Set the Item quantity dropped if L2ItemInstance is stackable
			if(item.isStackable())
			{
				i = itemCount; // Set so loop won't happent again
				item.setCount(itemCount); // Set item count
			}

			if(isRaid() || this instanceof ReflectionBossInstance)
			{
				SystemMessage sm;
				if(itemId == ItemTemplate.ITEM_ID_ADENA)
				{
					sm = new SystemMessage(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addNumber(item.getCount());
				}
				broadcastPacket(sm);
			}

			lastAttacker.doAutoLootOrDrop(item, this, forceAutoLoot);
		}
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_dieTime = 0L;
		_spawnAnimation = 0;

		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);

		getListeners().onSpawn();

		if(getAI().isGlobalAI() || getCurrentRegion() != null && getCurrentRegion().isActive())
		{
			getAI().startAITask();
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			startRandomAnimation();
		}

		if (hasMinions())
			ThreadPoolManager.getInstance().schedule(getMinionList(), 1500L); // DS: AI мастера должен запуститься раньше спавна миньонов.
	}

	@Override
	protected void onDespawn()
	{
		getAggroList().clear();

		stopRandomAnimation();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		getAI().notifyEvent(CtrlEvent.EVT_DESPAWN);

		super.onDespawn();
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	protected boolean _unAggred = false;

	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}

	/**
	 * Return True if the L2NpcInstance is aggressive (ex : L2MonsterInstance in function of aggroRange).<BR><BR>
	 */
	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}

	public int getAggroRange()
	{
		if(_unAggred)
			return 0;

		if(_personalAggroRange >= 0)
			return _personalAggroRange;

		return getTemplate().aggroRange;
	}

	/**
	 * Устанавливает данному npc новый aggroRange.
	 * Если установленый aggroRange < 0, то будет братся аггрорейндж с темплейта.
	 * @param aggroRange новый agrroRange
	 */
	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}

	/**
	 * Возвращает группу социальности
	 */
	public Faction getFaction()
	{
		return getTemplate().getFaction();
	}

	public boolean isInFaction(NpcInstance npc)
	{
		return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
	}

	@Override
	public int getMAtk(Creature target, SkillEntry skill)
	{
		return (int) (super.getMAtk(target, skill) * Config.ALT_NPC_MATK_MODIFIER);
	}

	@Override
	public int getPAtk(Creature target)
	{
		return (int) (super.getPAtk(target) * Config.ALT_NPC_PATK_MODIFIER);
	}

	@Override
	public int getPDef(Creature target)
	{
		return (int) (super.getPDef(target) * Config.ALT_NPC_PDEF_MODIFIER);
	}

	@Override
	public int getMDef(Creature target, SkillEntry skill)
	{
		return (int) (super.getMDef(target, skill) * Config.ALT_NPC_MDEF_MODIFIER);
	}

	@Override
	public int getMaxHp()
	{
		return (int) (super.getMaxHp() * Config.ALT_NPC_MAXHP_MODIFIER);
	}

	@Override
	public int getMaxMp()
	{
		return (int) (super.getMaxMp() * Config.ALT_NPC_MAXMP_MODIFIER);
	}

	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
	}

	public long getSpReward()
	{
		return (long) calcStat(Stats.SP, getTemplate().rewardSp, null, null);
	}

	@Override
	protected void onDelete()
	{
		stopDecay();
		if(_spawn != null)
			_spawn.stopRespawn();
		setSpawn(null);

		if (hasMinions())
			getMinionList().deleteMinions();

		super.onDelete();
	}

	public Spawner getSpawn()
	{
		return _spawn;
	}

	public void setSpawn(Spawner spawn)
	{
		_spawn = spawn;
	}

	public final void decayOrDelete()
	{
		onDecay();
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();

		_spawnAnimation = 2;

		if(_spawn != null)
			_spawn.decreaseCount(this);
		else if (!isMinion()) // Респавн миньонов производится в MinionList, удалять их нельзя
			deleteMe(); // Если этот моб заспавнен не через стандартный механизм спавна значит посмертие ему не положено и он умирает насовсем
	}

	/**
	 * Запустить задачу "исчезновения" после смерти
	 */
	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	/**
	 * Отменить задачу "исчезновения" после смерти
	 */
	public void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	/**
	 * Отменить и завершить задачу "исчезновения" после смерти
	 */
	public void endDecayTask()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
		doDecay();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	@Override
	public int getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}

	private int _displayId = 0;

	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}

	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().rhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);

		if(!(item instanceof WeaponTemplate))
			return null;

		return (WeaponTemplate) item;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instances
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().lhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);

		if(!(item instanceof WeaponTemplate))
			return null;

		return (WeaponTemplate) item;
	}

	@Override
	public void sendChanges()
	{
		if(isFlying()) // FIXME
			return;
		super.sendChanges();
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
		if(!isVisible())
			return;

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public void broadcastCharInfoImpl()
	{
		for(Player player : World.getAroundObservers(this))
			player.sendPacket(new NpcInfo(this, player).update());
	}

	// У NPC всегда 2
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.GREETING));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	public void startRandomAnimation()
	{
		if(!hasRandomAnimation())
			return;
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}

	public void stopRandomAnimation()
	{
		if(_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}

	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}

	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}

	public Castle getCastle()
	{
		if(getReflection() == ReflectionManager.PARNASSUS && Config.SERVICES_PARNASSUS_NOTAX)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.GIRAN_HARBOR)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.PARNASSUS)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(ZoneType.offshore))
			return null;
		if(_nearestCastle == null)
			_nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
		return _nearestCastle;
	}

	public Castle getCastle(Player player)
	{
		return getCastle();
	}

	public Fortress getFortress()
	{
		if(_nearestFortress == null)
			_nearestFortress = ResidenceHolder.getInstance().findNearestResidence(Fortress.class, getX(), getY(), getZ(), getReflection(), 32768);

		return _nearestFortress;
	}

	public ClanHall getClanHall()
	{
		if(_nearestClanHall == null)
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);

		return _nearestClanHall;
	}

	public Dominion getDominion()
	{
		if(getReflection() != ReflectionManager.DEFAULT)
			return null;

		if(_nearestDominion == null)
		{
			if(getTemplate().getCastleId() == 0)
				return null;

			Castle castle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
			_nearestDominion = castle.getDominion();
		}

		return _nearestDominion;
	}

	protected long _lastSocialAction;

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(!isTargetable()/* && !player.isGM()*/)
		{
			player.sendActionFailed();
			return;
		}

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));

			player.sendPacket(new ValidateLocation(this), ActionFail.STATIC);
			return;
		}

		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, NpcInstance.class, this, true))
			return;

		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}

		if(!isInRangeZ(player, INTERACTION_DISTANCE))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && !player.isGM() && !(this instanceof WarehouseInstance))
		{
			player.sendActionFailed();
			return;
		}

		// С NPC нельзя разговаривать мертвым, сидя и во время каста
		if((!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		player.sendActionFailed();
		if (player.isMoving)
			player.stopMove();
		player.sendPacket(new MoveToPawn(player, this, INTERACTION_DISTANCE));
		player.setLastNpcInteractionTime();

		if(_isBusy)
			showBusyWindow(player);
		else if(isHasChatWindow())
		{
			boolean flag = false;
			Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(qlst != null && qlst.length > 0)
				for(Quest q : qlst)
				{
					QuestState qs = player.getQuestState(q);
					if((qs == null || !qs.isCompleted()) && q.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
				showChatWindow(player, 0);
		}
	}

	public void showQuestWindow(Player player, String questName)
	{
		if(!player.isQuestContinuationPossible(true))
			return;

		int count = 0;
		for(QuestState quest : player.getAllQuestsStates())
			if(quest != null && quest.getQuest().isVisible() && quest.isStarted() && quest.getCond() > 0 && !quest.getQuest().isUnderLimit())
				count++;

		if(count > 40)
		{
			showChatWindow(player, "quest-limit.htm");
			return;
		}

		if (!questName.isEmpty())
		{
			String[] qn = questName.split("_");
			if (qn.length > 1)
			{
				int questId = Integer.parseInt(qn[1]);
				if(questId > 0)
				{
					// Get the state of the selected quest
					QuestState qs = player.getQuestState(questId);
					if(qs != null)
					{
						if(qs.isCompleted())
						{
							showChatWindow(player, "completed-quest.htm");
							return;
						}
						if(qs.getQuest().notifyTalk(this, qs))
							return;
					}
					else
					{
						Quest q = QuestManager.getQuest(questId);
						if(q != null)
						{
							// check for start point
							Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
							if(qlst != null && qlst.length > 0)
								for(Quest element : qlst)
									if(element == q)
									{
										qs = q.newQuestState(player, Quest.CREATED);
										if(qs.getQuest().notifyTalk(this, qs))
											return;
										break;
									}
						}
					}
				}
			}
		}

		showChatWindow(player, "no-quest.htm");
	}

	public static boolean canBypassCheck(Player player, NpcInstance npc)
	{
		if(npc == null || player.isDead() || !npc.isInRangeZ(player, INTERACTION_DISTANCE))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}

	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(getTemplate().getTeleportList().size() > 0 && checkForDominionWard(player))
			return;

		try
		{
			if(command.equalsIgnoreCase("TerritoryStatus"))
			{
				HtmlMessage html = new HtmlMessage(this);
				html.setFile("merchant/territorystatus.htm");

				Castle castle = getCastle(player);
				if(castle != null && castle.getId() > 0)
				{
					html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
					html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));

					if(castle.getOwnerId() > 0)
					{
						Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if(clan != null)
						{
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.replace("%clanname%", "unexistant clan");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%clanname%", "NPC");
						html.replace("%clanleadername%", "None");
					}
				}
				else
				{
					html.replace("%castlename%", "Open");
					html.replace("%taxpercent%", "0");

					html.replace("%clanname%", "No");
					html.replace("%clanleadername%", "");
				}

				player.sendPacket(html);
			}
			else if(command.startsWith("QuestEvent"))
			{
				StringTokenizer tokenizer = new StringTokenizer(command);
				tokenizer.nextToken();

				String questName = tokenizer.nextToken();

				int questId = Integer.parseInt(questName.split("_")[1]); //FIXME [VISTALL] может по другом?

				player.processQuestEvent(questId, command.substring(12 + questName.length()), this);
			}
			else if(command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if(quest.length() == 0)
					showQuestWindow(player);
				else
					showQuestWindow(player, quest);
			}
			else if(command.startsWith("Chat"))
				try
				{
					if (command.length() > 5)
					{
						int val = Integer.parseInt(command.substring(5));
						showChatWindow(player, val);
					}
					else
						showChatWindow(player, 0);
				}
				catch(NumberFormatException nfe)
				{
					String filename = command.substring(5).trim();
					if(filename.length() == 0)
						showChatWindow(player, "npcdefault.htm");
					else
						showChatWindow(player, filename);
				}
			else if(command.startsWith("AttributeCancel"))
				player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
			else if(command.startsWith("NpcLocationInfo"))
			{
				int val = Integer.parseInt(command.substring(16));
				NpcInstance npc = GameObjectsStorage.getByNpcId(val);
				if(npc != null)
				{
					// Убираем флажок на карте и стрелку на компасе
					player.sendPacket(new RadarControl(2, 2, npc.getLoc()));
					// Ставим флажок на карте и стрелку на компасе
					player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
				}
			}
			else if(command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				String listId = command.substring(9).trim();
				Castle castle = getCastle(player);
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, getObjectId(), castle != null ? castle.getTaxRate() : 0);
			}
			else if(command.startsWith("EnterRift"))
			{
				if(checkForDominionWard(player))
					return;

				StringTokenizer st = new StringTokenizer(command);
				st.nextToken(); //no need for "enterRift"

				Integer b1 = Integer.parseInt(st.nextToken()); //type

				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			else if(command.startsWith("ChangeRiftRoom"))
			{
				if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					((DimensionalRift) player.getParty().getReflection()).manualTeleport(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.startsWith("ExitRift"))
			{
				if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					((DimensionalRift) player.getParty().getReflection()).manualExitRift(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.equalsIgnoreCase("SkillList"))
				showSkillList(player);
			else if(command.startsWith("SubUnitSkillList"))
				showSubUnitSkillList(player);
			else if(command.equalsIgnoreCase("TransformationSkillList"))
				showTransformationSkillList(player, AcquireType.TRANSFORMATION);
			else if(command.equalsIgnoreCase("CertificationSkillList"))
				showTransformationSkillList(player, AcquireType.CERTIFICATION);
			else if(command.equalsIgnoreCase("CollectionSkillList"))
				showCollectionSkillList(player);
			else if(command.equalsIgnoreCase("BuyTransformation"))
				showTransformationMultisell(player);
			else if(command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if(cmdChoice == 1)
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				else if(cmdChoice == 2)
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
			}
			else if(command.startsWith("Link"))
			{
				if (command.length() > 5)
					showChatWindow(player, command.substring(5));
			}
			else if(command.startsWith("Teleport"))
			{
				int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if(list != null)
					showTeleportList(player, list, cmdChoice);
				else
					player.sendMessage("Ссылка неисправна, сообщите администратору.");
			}
			else if(command.startsWith("Tele20Lvl"))
			{
				int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if(player.getLevel() > 20)
					showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm");
				else if(list != null)
					showTeleportList(player, list, cmdChoice);
				else
					player.sendMessage("Ссылка неисправна, сообщите администратору.");
			}
			else if(command.startsWith("open_gate"))
			{
				int val = Integer.parseInt(command.substring(10));
				ReflectionUtils.getDoor(val).openMe();
				player.sendActionFailed();
			}
			else if(command.equalsIgnoreCase("TransferSkillList"))
				showTransferSkillList(player);
			else if(command.equalsIgnoreCase("CertificationCancel"))
				CertificationFunctions.cancelCertification(this, player);
			else if(command.startsWith("RemoveTransferSkill"))
			{
				AcquireType type = AcquireType.transferType(player.getActiveClassId());
				if(type == null)
					return;

				Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(null, type);
				if(skills.isEmpty())
				{
					player.sendActionFailed();
					return;
				}

				List<SkillLearn> toRemove = new ArrayList<SkillLearn>(skills.size());
				for(SkillLearn skill : skills)
				{
					SkillEntry knownSkill = player.getKnownSkill(skill.getId());
					if(knownSkill != null)
					{
						switch (knownSkill.getEntryType())
						{
							case TRANSFER_CARDINAL:
								if (type == AcquireType.TRANSFER_CARDINAL)
									toRemove.add(skill);
								break;
							case TRANSFER_EVA_SAINTS:
								if (type == AcquireType.TRANSFER_EVA_SAINTS)
									toRemove.add(skill);
								break;
							case TRANSFER_SHILLIEN_SAINTS:
								if (type == AcquireType.TRANSFER_SHILLIEN_SAINTS)
									toRemove.add(skill);
								break;
						}
						break;
					}
				}

				if(toRemove.isEmpty())
				{
					player.sendActionFailed();
					return;
				}

				if(!player.reduceAdena(10000000L, true))
				{
					showChatWindow(player, "common/skill_share_healer_no_adena.htm");
					return;
				}

				for(SkillLearn skill : toRemove)
					if(player.removeSkill(skill.getId(), true) != null)
						ItemFunctions.addItem(player, skill.getItemId(), skill.getItemCount());

				player.sendPacket(new SkillList(player));
				player.sendPacket(new ShortCutInit(player));
			}
			else if(command.startsWith("ExitFromQuestInstance"))
			{
				Reflection r = player.getReflection();
				r.startCollapseTimer(60000);
				player.teleToLocation(r.getReturnLoc(), 0);
				if(command.length() > 22)
					try
					{
						int val = Integer.parseInt(command.substring(22));
						showChatWindow(player, val);
					}
					catch(NumberFormatException nfe)
					{
						String filename = command.substring(22).trim();
						if(filename.length() > 0)
							showChatWindow(player, filename);
					}
			}
			else if(command.startsWith("birthdayHelper"))
			{
				for(NpcInstance n : World.getAroundNpc(this))
					if(n.getNpcId() == 32600)
					{
						showChatWindow(player, "Birthday-spawned.htm");
						return;
					}

				player.sendPacket(PlaySound.HB01);

				int x = (int) (getX() + 40 * Math.cos(headingToRadians(getHeading() - 32768 + 8000)));
				int y = (int) (getY() + 40 * Math.sin(headingToRadians(getHeading() - 32768 + 8000)));

				NpcUtils.spawnSingle(32600, x, y, getZ(), PositionUtils.calculateHeadingFrom(x, y, player.getX(), player.getY()), 180000);
			}
			else
			{
				String word = command.split("\\s+")[0];

				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
					b.getValue().invoke(b.getKey(), player, this, command.substring(word.length()).trim().split("\\s+"));
				else
					_log.warn("Unknown command=[" + command + "] npcId:" + getTemplate().npcId);
			}
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]", nfe);
		}
		catch(Exception sioobe)
		{
			_log.warn("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]", sioobe);
		}
	}

	public void showTeleportList(Player player, TeleportLocation[] list, int cmdChoice)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("&$556;").append("<br><br>");

		if(list != null && !player.isTeleportBlocked())
		{
			for(TeleportLocation tl : list)
				if(tl.getItem().getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					// TODO: DS: убрать хардкод
					double pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0. : player.isNoble() && cmdChoice > 1 ? Config.GATEKEEPER_NOBLE_MODIFIER : Config.GATEKEEPER_MODIFIER;
					if(tl.getPrice() > 0 && pricemod > 0)
					{
						//On Saturdays and Sundays from 8 PM to 12 AM, gatekeeper teleport fees decrease by 50%.
						Calendar calendar = Calendar.getInstance();
						int day = calendar.get(Calendar.DAY_OF_WEEK);
						int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						if((day == Calendar.SUNDAY || day == Calendar.SATURDAY) && (hour >= 20 && hour <= 12))
							pricemod /= 2;
					}
					sb.append("[npc_%objectId%_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
					if(tl.getCastleId() != 0)
						sb.append(" ").append(tl.getCastleId());
					sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(HtmlUtils.htmlNpcString(tl.getName()));
					if(tl.getPrice() * pricemod > 0)
						sb.append(" - ").append((long) (tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(ItemTemplate.ITEM_ID_ADENA));
					sb.append("]<br1>\n");
				}
				else
					sb.append("[npc_%objectId%_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append("|").append(HtmlUtils.htmlNpcString(tl.getName())).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("]<br1>\n");
		}
		else
			sb.append("No teleports available for you.");

		HtmlMessage html = new HtmlMessage(this);
		html.setHtml(HtmlUtils.bbParse(sb.toString()));
		player.sendPacket(html);
	}

	public void showQuestWindow(Player player)
	{
		// collect awaiting quests and start points
		List<Quest> options = new ArrayList<Quest>();

		List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);

		if(awaits != null)
			for(QuestState x : awaits)
				if(!options.contains(x.getQuest()))
					if(x.getQuest().getId() > 0)
						options.add(x.getQuest());

		if(starts != null)
			for(Quest x : starts)
				if(!options.contains(x))
					if(x.getId() > 0)
						options.add(x);

		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if(options.size() > 1)
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		else if(options.size() == 1)
			showQuestWindow(player, options.get(0).getName());
		else
			showQuestWindow(player, "");
	}

	public void showQuestChooseWindow(Player player, Quest[] quests)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<html><body><title>Talk about:</title><br>");

		for(Quest q : quests)
		{
			if(!q.isVisible())
				continue;

			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
		}

		sb.append("</body></html>");

		HtmlMessage html = new HtmlMessage(this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(getTemplate().getTeleportList().size() > 0 && checkForDominionWard(player))
			return;

		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int npcId = getNpcId();
		switch(npcId)
		{
			case 31111: // Gatekeeper Spirit (Disciples)
				int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int compWinner = SevenSigns.getInstance().getCabalHighestScore();
				if(playerCabal == sealAvariceOwner && playerCabal == compWinner)
					switch(sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				else
					filename += "spirit_null.htm";
				break;
			case 31112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 30298:
				if(player.getPledgeType() == Clan.SUBUNIT_ACADEMY)
					filename = getHtmlPath(npcId, 1, player);
				else
					filename = getHtmlPath(npcId, 0, player);
				break;
			default:
				if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
					return;
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val, player);
				break;
		}

		HtmlMessage packet = new HtmlMessage(this, filename);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i+= 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public void showChatWindow(Player player, String filename, Object... replace)
	{
		HtmlMessage packet = new HtmlMessage(this).setFile(filename);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i+= 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

 		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + pom + ".htm";

		String temp = "default/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "trainer/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "npcdefault.htm";
	}

	private boolean _isBusy;
	private String _busyMessage = "";

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	public void showBusyWindow(Player player)
	{
		HtmlMessage html = new HtmlMessage(this);
		html.setFile("npcbusy.htm");
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}

	public void showSkillList(Player player)
	{
		ClassId classId = player.getClassId();

		if(classId == null)
			return;

		int npcId = getTemplate().npcId;

		if(getTemplate().getTeachInfo().isEmpty())
		{
			_log.error("TeachInfo is empty! Npc: " + this + ", player : " + player + "!");
			player.sendActionFailed();
			return;
		}

		if(!(getTemplate().canTeach(classId) || getTemplate().canTeach(classId.getParent(player.getSex()))))
		{
			if(this instanceof WarehouseInstance)
				showChatWindow(player, "warehouse/" + getNpcId() + "-noteach.htm");
			else if(this instanceof TrainerInstance)
				showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");

			player.sendActionFailed();
			return;
		}

		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);

		final AcquireSkillList asl = new AcquireSkillList(AcquireType.NORMAL, skills.size());
		int counts = 0;

		for(SkillLearn s : skills)
		{
			if(s.isClicked())
				continue;

			SkillEntry sk = SkillTable.getInstance().getSkillEntry(s.getId(), s.getLevel());
			if(sk == null || !sk.getTemplate().getCanLearn(player.getClassId()) || !sk.getTemplate().canTeachBy(npcId))
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
		}

		if(counts == 0)
		{
			int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(player, AcquireType.NORMAL);

			if(minlevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
				player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			player.sendPacket(AcquireSkillDone.STATIC);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showTransferSkillList(Player player)
	{
		ClassId classId = player.getClassId();
		if(classId == null)
			return;

		if(player.getLevel() < 76 || classId.getLevel() < 4)
		{
			HtmlMessage html = new HtmlMessage(this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		AcquireType type = AcquireType.transferType(player.getActiveClassId());
		if(type == null)
			return;

		showAcquireList(type, player);
	}

	public static void showCollectionSkillList(Player player)
	{
		showAcquireList(AcquireType.COLLECTION, player);
	}

	public void showTransformationMultisell(Player player)
	{
		if(!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
			if(!player.isQuestCompleted(136))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-nobuy.htm");
				return;
			}

		Castle castle = getCastle(player);
		MultiSellHolder.getInstance().SeparateAndSend(32323, player, getObjectId(), castle != null ? castle.getTaxRate() : 0);
		player.sendActionFailed();
	}

	public void showTransformationSkillList(Player player, AcquireType type)
	{
		if(!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
			if(!player.isQuestCompleted(136))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noquest.htm");
				return;
			}

		showAcquireList(type, player);
	}

	public static void showFishingSkillList(Player player)
	{
		showAcquireList(AcquireType.fishingType(player), player);
	}

	public static void showAcquireList(AcquireType t, Player player)
	{
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);

		final AcquireSkillList asl = new AcquireSkillList(t, skills.size());

		for(SkillLearn s : skills)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);

		if(skills.size() == 0)
		{
			player.sendPacket(AcquireSkillDone.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public static void showSubUnitSkillList(Player player)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;

		if((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) != Clan.CP_CL_TROOPS_FAME)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		Set<SkillLearn> learns = new TreeSet<SkillLearn>();
		for(SubUnit sub : player.getClan().getAllSubUnits())
			learns.addAll(SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.SUB_UNIT, sub));

		final AcquireSkillList asl = new AcquireSkillList(AcquireType.SUB_UNIT, learns.size());

		for(SkillLearn s : learns)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 1, Clan.SUBUNIT_KNIGHT4);

		if(learns.size() == 0)
		{
			player.sendPacket(AcquireSkillDone.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}
	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public double getColRadius()
	{
		return getCollisionRadius();
	}

	@Override
	public double getColHeight()
	{
		return getCollisionHeight();
	}

	public int calculateLevelDiffForDrop(int charLevel)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;

		int mobLevel = getLevel();
		// According to official data (Prima), deep blue mobs are 9 or more levels below players
		int deepblue_maxdiff = this instanceof RaidBossInstance ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public boolean isSevenSignsMonster()
	{
		return getFaction().getName().equalsIgnoreCase("c_dungeon_clan");
	}

	@Override
	public String toString()
	{
		return getNpcId() + " " + getName();
	}

	public void refreshID()
	{
		GameObjectsStorage.remove(this);

		objectId = IdFactory.getInstance().getNextId();

		GameObjectsStorage.put(this);
	}

	private boolean _isUnderground = false;

	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}

	public boolean isUnderground()
	{
		return _isUnderground;
	}

	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}

	public void setTargetable(boolean value, boolean cancelTargets)
	{
		_isTargetable = value;
		if(!value && cancelTargets)
		{
			for(Player player : World.getAroundPlayers(this))
				if(player.getTarget() == this)
					player.setTarget(null);
		}
	}

	public boolean isShowName()
	{
		return _showName;
	}

	public void setShowName(boolean value)
	{
		_showName = value;
	}

	@Override
	public NpcListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new NpcListenerList(this);
			}

		return (NpcListenerList) listeners;
	}

	public <T extends NpcListener> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends NpcListener> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new NpcStatsChangeRecorder(this);
			}

		return (NpcStatsChangeRecorder) _statsRecorder;
	}

	public void setNpcState(int stateId)
	{
		broadcastPacket(new ExChangeNpcState(getObjectId(), stateId));
		npcState = stateId;
	}

	public int getNpcState()
	{
		return npcState;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>(3);
		list.add(new NpcInfo(this, forPlayer));

		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));

		if(isMoving || isFollow)
			list.add(movePacket());

		return list;
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return loc.z;
		if(isNpc())
		{
			if(_spawnRange instanceof Territory)
				return GeoEngine.getHeight(loc, getGeoIndex());
			return loc.z;
		}

		return super.getGeoZ(loc);
	}

	@Override
	public Clan getClan()
	{
		Dominion dominion = getDominion();
		if(dominion == null)
			return null;
		int lordObjectId = dominion.getLordObjectId();
		return lordObjectId == 0 ? null : dominion.getOwner();
	}

	public NpcString getNameNpcString()
	{
		return _nameNpcString;
	}

	public NpcString getTitleNpcString()
	{
		return _titleNpcString;
	}

	public void setNameNpcString(NpcString nameNpcString)
	{
		_nameNpcString = nameNpcString;
	}

	public void setTitleNpcString(NpcString titleNpcString)
	{
		_titleNpcString = titleNpcString;
	}

	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}

	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}

	public boolean checkForDominionWard(Player player)
	{
		final ItemInstance item = player.getActiveWeaponInstance();
		if(item != null && item.getAttachment() instanceof TerritoryWardObject)
		{
			showChatWindow(player, "flagman.htm");
			return true;
		}
		return false;
	}

	public void setParameter(String str, Object val)
	{
		if(_parameters == StatsSet.EMPTY)
			_parameters = new StatsSet();

		_parameters.set(str, val);
	}

	public void setParameters(MultiValueSet<String> set)
	{
		if(set.isEmpty())
			return;

		if(_parameters == StatsSet.EMPTY)
			_parameters = new MultiValueSet<String>(set.size());

		_parameters.putAll(set);
	}

	public int getParameter(String str, int val)
	{
		return _parameters.getInteger(str, val);
	}

	public long getParameter(String str, long val)
	{
		return _parameters.getLong(str, val);
	}

	public boolean getParameter(String str, boolean val)
	{
		return _parameters.getBool(str, val);
	}

	public String getParameter(String str, String val)
	{
		return _parameters.getString(str, val);
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}

	public boolean isHasChatWindow()
	{
		return _hasChatWindow;
	}

	public void setHasChatWindow(boolean hasChatWindow)
	{
		_hasChatWindow = hasChatWindow;
	}

	@Override
	public void setTeam(TeamType t)
	{
		super.setTeam(t);
		sendChanges();
	}

	@Override
	public final boolean isRaid()
	{
		return getTemplate().isRaid;
	}

	@Override
	public boolean isFearImmune()
	{
		return getLeader() != null ? getLeader().isFearImmune() : !isMonster() || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return !isMonster() || super.isParalyzeImmune();
	}

	@Override
	public boolean isLethalImmune()
	{
		return _noLethal || super.isLethalImmune();
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		//FIXME [VISTALL] разделить Аргументированые нпц
		return packet == RequestRefine.class || packet == RequestRefineCancel.class;
	}

	public boolean noShiftClick()
	{
		return _noShiftClick;
	}

	public void setItemDropEnabled(boolean enabled)
	{
		_itemDropEnabled = enabled;
	}

	public boolean getItemDropEnabled()
	{
		return _itemDropEnabled;
	}

	/**
	 * 
	 * Возвращает базовый множитель из конфига для дропа предметов, при необходимости умноженный на ПА.
	 * Не используется для адены, спойла и эвентового дропа. Не учитывает разницу уровней.
	 * 
	 */
	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_ITEMS * player.getRateItems();
	}
}