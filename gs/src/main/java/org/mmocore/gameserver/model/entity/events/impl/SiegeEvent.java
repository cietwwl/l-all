package org.mmocore.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.lang.reference.HardReferences;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterServitorDAO;
import org.mmocore.gameserver.dao.SiegeClanDAO;
import org.mmocore.gameserver.dao.SummonDAO;
import org.mmocore.gameserver.dao.SummonEffectDAO;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.listener.actor.OnDeathListener;
import org.mmocore.gameserver.listener.actor.OnKillListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.objects.SiegeClanObject;
import org.mmocore.gameserver.model.entity.events.objects.ZoneObject;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.model.instances.SummonInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.RelationChanged;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.templates.DoorTemplate;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.TeleportUtils;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntLongMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 * @date 15:11/14.02.2011
 */
public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends Event
{
	protected class SiegeSummonInfo
	{
		private int _skillId;
		private int _ownerObjectId;

		private HardReference<SummonInstance> _summonRef = HardReferences.emptyRef();

		SiegeSummonInfo(SummonInstance summonInstance)
		{
			_skillId = summonInstance.getCallSkillId();
			_ownerObjectId = summonInstance.getPlayer().getObjectId();
			_summonRef = summonInstance.getRef();
		}

		public int getSkillId()
		{
			return _skillId;
		}

		public int getOwnerObjectId()
		{
			return _ownerObjectId;
		}
	}

	public class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(!isInProgress())
				return;

			DoorInstance door = (DoorInstance)actor;
			if(door.getDoorType() == DoorTemplate.DoorType.WALL)
				return;

			broadcastTo(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED, SiegeEvent.ATTACKERS, SiegeEvent.DEFENDERS);
		}
	}

	public static final String OWNER = "owner";
	public static final String OLD_OWNER = "old_owner";

	public static final String ATTACKERS = "attackers";
	public static final String DEFENDERS = "defenders";
	public static final String SPECTATORS = "spectators";

	public static final String FROM_RESIDENCE_TO_TOWN = "from_residence_to_town";

	public static final String SIEGE_ZONES = "siege_zones";
	public static final String FLAG_ZONES = "flag_zones";

	public static final String DAY_OF_WEEK = "day_of_week";
	public static final String HOUR_OF_DAY = "hour_of_day";

	public static final String REGISTRATION = "registration";

	public static final String DOORS = "doors";

	// states
	public static final int PROGRESS_STATE		=	1 << 0;
	public static final int REGISTRATION_STATE	=	1 << 1;
	// block fame time
	public static final long BLOCK_FAME_TIME	=	5 * 60 * 1000L;

	protected R _residence;

	private int _state;

	protected int _dayOfWeek;
	protected int _hourOfDay;

	protected Clan _oldOwner;

	protected OnKillListener _killListener;
	protected OnDeathListener _doorDeathListener = new DoorDeathListener();
	protected IntObjectMap<SiegeSummonInfo> _siegeSummons = new CHashIntObjectMap<SiegeSummonInfo>();
	protected IntLongMap _blockedFameOnKill = new CHashIntLongMap();

	public SiegeEvent(MultiValueSet<String> set)
	{
		super(set);
		_dayOfWeek = set.getInteger(DAY_OF_WEEK, 0);
		_hourOfDay = set.getInteger(HOUR_OF_DAY, 0);
	}

	//========================================================================================================================================================================
	//                                                                   Start / Stop Siege
	//========================================================================================================================================================================

	@Override
	public void startEvent()
	{
		addState(PROGRESS_STATE);

		super.startEvent();
	}

	@Override
	public final void stopEvent()
	{
		stopEvent(false);
	}

	public void stopEvent(boolean step)
	{
		removeState(PROGRESS_STATE);

		despawnSiegeSummons();
		reCalcNextTime(false);

		super.stopEvent();
	}

	public void processStep(Clan clan)
	{
		//
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		final Calendar startSiegeDate = getResidence().getSiegeDate();
		if(onInit)
		{
			// дата ниже текущей
			if(startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
			{
				startSiegeDate.set(Calendar.DAY_OF_WEEK, _dayOfWeek);
				startSiegeDate.set(Calendar.HOUR_OF_DAY, _hourOfDay);

				validateSiegeDate(startSiegeDate, 2);
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
			}
		}
		else
		{
			startSiegeDate.add(Calendar.WEEK_OF_YEAR, 2);
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
		}

		registerActions();

		getResidence().update();
	}

	protected void validateSiegeDate(Calendar calendar, int add)
	{
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		while(calendar.getTimeInMillis() < System.currentTimeMillis())
			calendar.add(Calendar.WEEK_OF_YEAR, add);
	}

	@Override
	protected long startTimeMillis()
	{
		return getResidence().getSiegeDate().getTimeInMillis();
	}
	//========================================================================================================================================================================
	//                                                                   Zones
	//========================================================================================================================================================================

	@Override
	public void teleportPlayers(String t)
	{
		List<Player> players = new ArrayList<Player>();
		Clan ownerClan = getResidence().getOwner();
		if(t.equalsIgnoreCase(OWNER))
		{
			if(ownerClan != null)
				for(Player player : getPlayersInZone())
					if(player.getClan() == ownerClan)
						players.add(player);
		}
		else if(t.equalsIgnoreCase(ATTACKERS))
		{
			for(Player player : getPlayersInZone())
			{
				S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
				if(siegeClan != null && siegeClan.isParticle(player))
					players.add(player);
			}
		}
		else if(t.equalsIgnoreCase(DEFENDERS))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				S siegeClan = getSiegeClan(DEFENDERS, player.getClan());
				if(siegeClan != null && siegeClan.isParticle(player))
					players.add(player);
			}
		}
		else if(t.equalsIgnoreCase(SPECTATORS))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				if(player.getClan() == null || getSiegeClan(ATTACKERS, player.getClan()) == null && getSiegeClan(DEFENDERS, player.getClan()) == null)
					players.add(player);
			}
		}
		// выносих всех с резиденции в город
		else if(t.equalsIgnoreCase(FROM_RESIDENCE_TO_TOWN))
		{
			for(Player player : getResidence().getZone().getInsidePlayers())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				players.add(player);
			}
		}
		else
			players = getPlayersInZone();

		for(Player player : players)
		{
			Location loc = null;
			if(t.equalsIgnoreCase(OWNER) || t.equalsIgnoreCase(DEFENDERS))
				loc = getResidence().getOwnerRestartPoint();
			else if(t.equalsIgnoreCase(FROM_RESIDENCE_TO_TOWN))
				loc = TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE);
			else
				loc = getResidence().getNotOwnerRestartPoint(player);

			player.teleToLocation(loc, ReflectionManager.DEFAULT);
		}
	}

	public List<Player> getPlayersInZone()
	{
		List<ZoneObject> zones = getObjects(SIEGE_ZONES);
		List<Player> result = new LazyArrayList<Player>();
		for(ZoneObject zone : zones)
			result.addAll(zone.getInsidePlayers());
		return result;
	}

	public void broadcastInZone(L2GameServerPacket... packet)
	{
		for(Player player : getPlayersInZone())
			player.sendPacket(packet);
	}

	public void broadcastInZone(IBroadcastPacket... packet)
	{
		for(Player player : getPlayersInZone())
			player.sendPacket(packet);
	}

	public boolean checkIfInZone(Creature character)
	{
		List<ZoneObject> zones = getObjects(SIEGE_ZONES);
			for(ZoneObject zone : zones)
				if(zone.checkIfInZone(character))
					return true;
		return false;
	}

	public void broadcastInZone2(IBroadcastPacket... packet)
	{
		for(Player player : getResidence().getZone().getInsidePlayers())
			player.sendPacket(packet);
	}

	public void broadcastInZone2(L2GameServerPacket... packet)
	{
		for(Player player : getResidence().getZone().getInsidePlayers())
			player.sendPacket(packet);
	}
	//========================================================================================================================================================================
	//                                                                   Siege Clans
	//========================================================================================================================================================================
	public void loadSiegeClans()
	{
		addObjects(ATTACKERS, SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS));
		addObjects(DEFENDERS, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS));
	}

	@SuppressWarnings("unchecked")
	public S newSiegeClan(String type, int clanId, long param, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : (S) new SiegeClanObject(type, clan, param, date);
	}

	public void updateParticles(boolean start, String... arg)
	{
		for(String a : arg)
		{
			List<SiegeClanObject> siegeClans = getObjects(a);
			for(SiegeClanObject s : siegeClans)
				s.setEvent(start, this);
		}
	}

	public S getSiegeClan(String name, Clan clan)
	{
		if(clan == null)
			return null;
		return getSiegeClan(name, clan.getClanId());
	}

	@SuppressWarnings("unchecked")
	public S getSiegeClan(String name, int objectId)
	{
		List<SiegeClanObject> siegeClanList = getObjects(name);
		if(siegeClanList.isEmpty())
			return null;
		for(int i = 0; i < siegeClanList.size(); i++)
		{
			SiegeClanObject siegeClan = siegeClanList.get(i);
			if(siegeClan.getObjectId() == objectId)
				return (S) siegeClan;
		}
		return null;
	}

	public void broadcastTo(IBroadcastPacket packet, String... types)
	{
		for(String type : types)
		{
			List<SiegeClanObject> siegeClans = getObjects(type);
			for(SiegeClanObject siegeClan : siegeClans)
				siegeClan.broadcast(packet);
		}
	}

	public void broadcastTo(L2GameServerPacket packet, String... types)
	{
		for(String type : types)
		{
			List<SiegeClanObject> siegeClans = getObjects(type);
			for(SiegeClanObject siegeClan : siegeClans)
				siegeClan.broadcast(packet);
		}
	}

	//========================================================================================================================================================================
	//                                                         Override Event
	//========================================================================================================================================================================

	@Override
	@SuppressWarnings("unchecked")
	public void initEvent()
	{
		_residence = (R) ResidenceHolder.getInstance().getResidence(getId());

		loadSiegeClans();

		clearActions();

		super.initEvent();
	}

	@Override
	public boolean ifVar(String name)
	{
		if(name.equals(OWNER))
			return getResidence().getOwner() != null;
		if(name.equals(OLD_OWNER))
			return _oldOwner != null;

		return false;
	}

	@Override
	public void findEvent(Player player)
	{
		if(!isInProgress() || player.getClan() == null)
			return;

		if(getSiegeClan(ATTACKERS, player.getClan()) != null || getSiegeClan(DEFENDERS, player.getClan()) != null)
		{
			player.addEvent(this);

			long val = _blockedFameOnKill.get(player.getObjectId());
			if(val > 0)
			{
				long diff = val - System.currentTimeMillis();
				if(diff > 0)
					player.startEnableUserRelationTask(diff, this);
			}
		}
	}

	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		if(getObjects(FLAG_ZONES).isEmpty())
			return;

		S clan = getSiegeClan(ATTACKERS, player.getClan());
		if(clan != null)
			if(clan.getFlag() != null)
				r.put(RestartType.TO_FLAG, Boolean.TRUE);
	}

	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		if (player.getReflection() != ReflectionManager.DEFAULT)
			return null;

		if (type == RestartType.TO_FLAG)
		{
			final S attackerClan = getSiegeClan(ATTACKERS, player.getClan());
			if(!getObjects(FLAG_ZONES).isEmpty() && attackerClan != null && attackerClan.getFlag() != null)
				return Location.findPointToStay(attackerClan.getFlag(), 50, 75);
			else
				player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
		}

		return null;
	}

	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		Clan clan1 = thisPlayer.getClan();
		Clan clan2 = targetPlayer.getClan();
		if(clan1 == null || clan2 == null)
			return result;

		SiegeEvent<?, ?> siegeEvent2 = targetPlayer.getEvent(SiegeEvent.class);
		if(this == siegeEvent2)
		{
			result |= RelationChanged.RELATION_IN_SIEGE;

			SiegeClanObject siegeClan1 = getSiegeClan(SiegeEvent.ATTACKERS, clan1);
			SiegeClanObject siegeClan2 = getSiegeClan(SiegeEvent.ATTACKERS, clan2);

			if(siegeClan1 == null && siegeClan2 == null || siegeClan1 == siegeClan2 || siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
				result |= RelationChanged.RELATION_ALLY;
			else
				result |= RelationChanged.RELATION_ENEMY;
			if(siegeClan1 != null)
				result |= RelationChanged.RELATION_ATTACKER;
		}

		return result;
	}

	@Override
	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		oldRelation |= RelationChanged.USER_RELATION_IN_SIEGE;

		SiegeClanObject siegeClan = getSiegeClan(SiegeEvent.ATTACKERS, thisPlayer.getClan());
		if(siegeClan != null)
			oldRelation |= RelationChanged.USER_RELATION_ATTACKER;

		return oldRelation;
	}

	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(!checkIfInZone(target) || !checkIfInZone(attacker))
			return null;

		SiegeEvent<?, ?> siegeEvent = target.getEvent(SiegeEvent.class);

		// или вообще не учасник, или учасники разных осад
		if(this != siegeEvent)
			return null;

		Player player = target.getPlayer();
		if(player == null)
			return null;

		SiegeClanObject siegeClan1 = getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());
		if(siegeClan1 == null && attacker.isSiegeGuard())
			return SystemMsg.INVALID_TARGET;
		Player playerAttacker = attacker.getPlayer();
		if(playerAttacker == null)
			return SystemMsg.INVALID_TARGET;

		SiegeClanObject siegeClan2 = getSiegeClan(SiegeEvent.ATTACKERS, playerAttacker.getClan());
		// если оба аттакеры, и в осаде, аттакеры в Алли, невозможно бить
		if(siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
			return SystemMsg.FORCE_ATTACK_IS_IMPOSSIBLE_AGAINST_A_TEMPORARY_ALLIED_MEMBER_DURING_A_SIEGE;
		// если нету как Аттакры, это дефендеры, то невозможно бить
		if(siegeClan1 == null && siegeClan2 == null)
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	@Override
	public boolean isInProgress()
	{
		return hasState(PROGRESS_STATE);
	}

	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(REGISTRATION))
		{
			if(start)
				addState(REGISTRATION_STATE);
			else
				removeState(REGISTRATION_STATE);
		}
		else
			super.action(name, start);
	}

	public boolean isAttackersInAlly()
	{
		return false;
	}

	@Override
	public void onAddEvent(GameObject object)
	{
		if(_killListener == null)
			return;

		if(object.isPlayer())
			((Player)object).addListener(_killListener);
	}

	@Override
	public void onRemoveEvent(GameObject object)
	{
		if(_killListener == null)
			return;

		if(object.isPlayer())
			((Player)object).removeListener(_killListener);
	}

	@Override
	public List<Player> broadcastPlayers(int range)
	{
		return itemObtainPlayers();
	}

	@Override
	public EventType getType()
	{
		return EventType.SIEGE_EVENT;
	}

	@Override
	public List<Player> itemObtainPlayers()
	{
		List<Player> playersInZone = getPlayersInZone();

		List<Player> list = new LazyArrayList<Player>(playersInZone.size());
		for(Player player : getPlayersInZone())
		{
			if(player.getEvent(getClass()) == this)
				list.add(player);
		}
		return list;
	}

	@Override
	public void giveItem(Player player, int itemId, long count)
	{
		if (Config.ALT_NO_FAME_FOR_DEAD && itemId == ItemTemplate.ITEM_ID_FAME && player.isDead())
			return;

		super.giveItem(player, itemId, count);
	}

	public Location getEnterLoc(Player player, Zone zone) // DS: в момент вызова игрок еще не вошел в игру и с него нельзя получить список зон, поэтому просто передаем найденную по локации
	{
		S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
		if(siegeClan != null)
		{
			if(siegeClan.getFlag() != null)
				return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
			else
				return getResidence().getNotOwnerRestartPoint(player);
		}
		else
			return getResidence().getOwnerRestartPoint();
	}

	/**
	 * Вызывается для эвента киллера и показывает может ли киллер стать ПК
	 */
	public boolean canPK(Player target, Player killer)
	{
		if (!isInProgress())
			return true; // осада еще не началась

		final SiegeEvent<?, ?> targetEvent = target.getEvent(SiegeEvent.class);
		if (targetEvent != this)
			return true; // либо вообще не участник осад, либо разные осады

		final S targetClan = getSiegeClan(SiegeEvent.ATTACKERS, target.getClan());
		final S killerClan = getSiegeClan(SiegeEvent.ATTACKERS, killer.getClan());
		if (targetClan != null && killerClan != null && isAttackersInAlly()) // оба атакующие и в альянсе
			return true;
		if (targetClan == null && killerClan == null) // оба защитники
			return true;

		return false;
	}

	//========================================================================================================================================================================
	// Getters & Setters
	//========================================================================================================================================================================
	public R getResidence()
	{
		return _residence;
	}

	public void addState(int b)
	{
		_state |= b;
	}

	public void removeState(int b)
	{
		_state &= ~b;
	}

	public boolean hasState(int val)
	{
		return (_state & val) == val;
	}

	public boolean isRegistrationOver()
	{
		return !hasState(REGISTRATION_STATE);
	}

	//========================================================================================================================================================================
	public void addSiegeSummon(Player player, SummonInstance summon)
	{
		_siegeSummons.put(player.getObjectId(), new SiegeSummonInfo(summon));
	}

	public boolean containsSiegeSummon(Servitor cha)
	{
		SiegeSummonInfo siegeSummonInfo = _siegeSummons.get(cha.getPlayer().getObjectId());
		if(siegeSummonInfo == null)
			return false;
		return siegeSummonInfo._summonRef.get() == cha;
	}

	public void removeSiegeSummon(Player player, Servitor cha)
	{
		_siegeSummons.remove(player.getObjectId());
	}

	public void updateSiegeSummon(Player player, SummonInstance summon)
	{
		SiegeSummonInfo siegeSummonInfo = _siegeSummons.get(player.getObjectId());
		if(siegeSummonInfo == null)
			return;

		if(siegeSummonInfo.getSkillId() == summon.getCallSkillId())
		{
			summon.setSiegeSummon(true);
			siegeSummonInfo._summonRef = summon.getRef();
		}
	}

	public void despawnSiegeSummons()
	{
		for(IntObjectPair<SiegeSummonInfo> entry : _siegeSummons.entrySet())
		{
			SiegeSummonInfo summonInfo = entry.getValue();

			SummonInstance summon = summonInfo._summonRef.get();
			if(summon != null)
				summon.unSummon(false, false);
			else
			{
				CharacterServitorDAO.getInstance().delete(entry.getKey(), summonInfo._skillId, Servitor.SUMMON_TYPE);
				SummonDAO.getInstance().delete(entry.getKey(), summonInfo._skillId);
				SummonEffectDAO.getInstance().delete(entry.getKey(), summonInfo._skillId);
			}
		}

		_siegeSummons.clear();
	}

	public void removeBlockFame(Player player)
	{
		_blockedFameOnKill.remove(player.getObjectId());
	}
}
