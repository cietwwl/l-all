package org.mmocore.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.DominionRewardDAO;
import org.mmocore.gameserver.dao.SiegeClanDAO;
import org.mmocore.gameserver.dao.SiegePlayerDAO;
import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.instancemanager.QuestManager;
import org.mmocore.gameserver.listener.actor.OnDeathListener;
import org.mmocore.gameserver.listener.actor.OnKillListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.objects.DoorObject;
import org.mmocore.gameserver.model.entity.events.objects.SiegeClanObject;
import org.mmocore.gameserver.model.entity.events.objects.ZoneObject;
import org.mmocore.gameserver.model.entity.residence.Dominion;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import org.mmocore.gameserver.network.l2.s2c.RelationChanged;
import org.mmocore.gameserver.templates.DoorTemplate;
import org.mmocore.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 * @date 15:14/14.02.2011
 */
public class DominionSiegeEvent extends SiegeEvent<Dominion, SiegeClanObject>
{
	public class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(!isInProgress())
				return;

			DoorInstance door = (DoorInstance) actor;
			if(door.getDoorType() == DoorTemplate.DoorType.WALL)
				return;

			Player player = killer.getPlayer();
			if(player != null)
				player.sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);

			Clan owner = getResidence().getOwner();
			if(owner != null && owner.getLeader().isOnline())
				owner.getLeader().getPlayer().sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);
		}
	}

	public class KillListener implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			if(!isInProgress())
				return;

			Player winner = actor.getPlayer();

			if(winner == null || !victim.isPlayer() || winner == victim)
				return;

			List<Player> players = new ArrayList<Player>();
			for(Player temp : (winner.getParty() == null ? winner : winner.getParty()))
			{
				DominionSiegeEvent event = temp.getEvent(DominionSiegeEvent.class);
				if(event == null || event == victim.getEvent(DominionSiegeEvent.class) || !temp.isInRange(winner, Config.ALT_PARTY_DISTRIBUTION_RANGE) || temp.getLevel() < 40)
					continue;

				players.add(temp);
			}

			if (players.isEmpty())
				return;

			if(victim.getLevel() >= 61)
			{
				Quest q = _runnerEvent.getClassQuest(((Player)victim).getClassId());
				if(q != null)
					for(Player temp : players)
					{
						addReward(temp, KILL_REWARD, 1);
	
						QuestState questState = temp.getQuestState(q);
						if(questState == null)
						{
							questState = q.newQuestState(temp, Quest.CREATED);
	
							q.onKill((Player)victim, questState);
						}
						else if(questState.getState() == Quest.COMPLETED && questState.getLong(DATE) != getResidence().getSiegeDate().getTimeInMillis())
						{
							questState.setState(Quest.CREATED);
							questState.addPlayerOnKillListener();
	
							q.onKill((Player)victim, questState);
						}
				}
			}

			if(!((Player)victim).isUserRelationActive())
				return;

			Zone zone = victim.getZone(Zone.ZoneType.SIEGE);
			if(zone == null)
				return;

			DominionSiegeEvent d2 = victim.getEvent(DominionSiegeEvent.class);
			if(d2 == null)
				return;

			double bonus = Config.ALT_PARTY_BONUS[players.size() - 1];
			int value = (int)(Math.round(BASE_SIEGE_FAME * bonus) / players.size());
			for(Player temp : players)
				temp.setFame(temp.getFame() + value, temp.getEvent(DominionSiegeEvent.class).toString());

			((Player)victim).startEnableUserRelationTask(BLOCK_FAME_TIME, DominionSiegeEvent.this);
			d2._blockedFameOnKill.put(victim.getObjectId(), System.currentTimeMillis() + BLOCK_FAME_TIME);
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}

	public static final int BASE_SIEGE_FAME = 72;

	public static final int KILL_REWARD = 0;
	public static final int ONLINE_REWARD = 1;
	public static final int STATIC_BADGES = 2;
	public static final int REWARD_MAX = 3;
	// states
	public static final int BATTLEFIELD_CHAT_STATE	=	1 << 2;
	// object name
	public static final String ATTACKER_PLAYERS = "attacker_players";
	public static final String DEFENDER_PLAYERS = "defender_players";
	public static final String DISGUISE_PLAYERS = "disguise_players";
	public static final String TERRITORY_NPC = "territory_npc";
	public static final String CATAPULT = "catapult";
	public static final String CATAPULT_DOORS = "catapult_doors";
	public static final String DATE = "date";

	private DominionSiegeRunnerEvent _runnerEvent;
	private Quest _forSakeQuest;

	private IntObjectMap<int[]> _playersRewards = new CHashIntObjectMap<int[]>();

	public DominionSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
		_killListener = new KillListener();
		_doorDeathListener = new DoorDeathListener();
	}

	@Override
	public void initEvent()
	{
		_runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);

		super.initEvent();


		SiegeEvent<?, ?> castleSiegeEvent = getResidence().getCastle().getSiegeEvent();

		addObjects("mass_gatekeeper", castleSiegeEvent.getObjects("mass_gatekeeper"));
		addObjects(CastleSiegeEvent.CONTROL_TOWERS, castleSiegeEvent.getObjects(CastleSiegeEvent.CONTROL_TOWERS));

		List<DoorObject> doorObjects = getObjects(DOORS);
		for(DoorObject doorObject : doorObjects)
			doorObject.getDoor().addListener(_doorDeathListener);
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		//
	}

	@Override
	public void startEvent()
	{
		int[] flags = getResidence().getFlags();
		if(flags.length > 0)
		{
			getResidence().removeSkills();
			getResidence().getOwner().broadcastToOnlineMembers(SystemMsg.THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING);
		}

		SiegeClanDAO.getInstance().delete(getResidence());
		SiegePlayerDAO.getInstance().delete(getResidence());

		for(int i : flags)
			spawnAction("ward_" + i, true);

		super.startEvent();

		updateParticles(true);
	}

	@Override
	public void stopEvent(boolean t)
	{
		int[] flags = getResidence().getFlags();
		for(int i : flags)
			spawnAction("ward_" + i, false);

		getResidence().rewardSkills();
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();

		List<SiegeClanObject> defenders = getObjects(DEFENDERS);
		for(SiegeClanObject clan : defenders)
			clan.deleteFlag();

		super.stopEvent(t);

		_blockedFameOnKill.clear();

		updateParticles(false);

		DominionRewardDAO.getInstance().insert(getResidence());
	}

	@Override
	public void loadSiegeClans()
	{
		addObjects(DEFENDERS, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS));
		addObjects(DEFENDER_PLAYERS, SiegePlayerDAO.getInstance().select(getResidence(), 0));

		DominionRewardDAO.getInstance().select(getResidence());
	}

	@Override
	public void updateParticles(boolean start, String... arg)
	{
		List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
		for(SiegeClanObject s : siegeClans)
		{
			s.getClan().setWarDominion(start ? getId() : 0);

			PledgeShowInfoUpdate packet = new PledgeShowInfoUpdate(s.getClan());
			for(Player player : s.getClan().getOnlineMembers(0))
			{
				player.sendPacket(packet);

				updatePlayer(player, start);
			}
		}

		List<Integer> players = getObjects(DEFENDER_PLAYERS);
		for(int i : players)
		{
			Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
				updatePlayer(player, start);
		}
	}

	public void updatePlayer(Player player, boolean start)
	{
		if(start)
		{
			// за старт ТВ по 5
			addReward(player, STATIC_BADGES, 5);
		}
		else
		{
			// за конец ТВ по 5
			addReward(player, STATIC_BADGES, 5);

			player.stopEnableUserRelationTask();
			player.getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
			player.addExpAndSp(270000, 27000);
		}

		player.broadcastCharInfo();

		questUpdate(player, start);
	}

	public void questUpdate(Player player, boolean start)
	{
		if(start)
		{
			QuestState sakeQuestState = _forSakeQuest.newQuestState(player, Quest.CREATED);
			sakeQuestState.setState(Quest.STARTED);
			sakeQuestState.setCond(1);

			Quest protectCatapultQuest = QuestManager.getQuest(729);
			if(protectCatapultQuest == null)
				return;

			QuestState questState = protectCatapultQuest.newQuestStateAndNotSave(player, Quest.CREATED);
			questState.setCond(1, false);
			questState.setStateAndNotSave(Quest.STARTED);
		}
		else
		{
			for(Quest q : _runnerEvent.getBreakQuests())
			{
				QuestState questState = player.getQuestState(q);
				if(questState != null)
					questState.abortQuest();
			}
		}
	}

	public List<Player> getOnlinePlayers()
	{
		List<Player> players = new LazyArrayList<Player>(50);

		List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
		for(SiegeClanObject s : siegeClans)
			players.addAll(s.getClan().getOnlineMembers(0));

		List<Integer> siegePlayers = getObjects(DEFENDER_PLAYERS);
		for(int i : siegePlayers)
		{
			Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
				players.add(player);
		}

		return players;
	}

	@Override
	public void findEvent(Player player)
	{
		// BATTLEFIELD_CHAT_FLAG, 20 мин до старта, и 10 после, и 2 часа во время - действует флаг
		if(hasState(BATTLEFIELD_CHAT_STATE))
		{
			if(getSiegeClan(DEFENDERS, player.getClan()) != null || getObjects(DEFENDER_PLAYERS).contains(player.getObjectId()))
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
	}

	//========================================================================================================================================================================
	//                                                                   Overrides Event
	//========================================================================================================================================================================
	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		if(!isInProgress())
			return result;

		DominionSiegeEvent event2 = targetPlayer.getEvent(DominionSiegeEvent.class);
		if(event2 == null)
			return result;

		result |= RelationChanged.RELATION_IN_DOMINION_WAR;
		return result;
	}

	@Override
	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		if(!isInProgress())
			return oldRelation;

		oldRelation |= RelationChanged.USER_RELATION_IN_DOMINION_WAR;
		return oldRelation;
	}

	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(!isInProgress())
			return null;

		DominionSiegeEvent dominionSiegeEvent = target.getEvent(DominionSiegeEvent.class);

		// своих во время ТВ бить нельзя
		if(this == dominionSiegeEvent)
			return /*skill == null ? SystemMsg.THIS_TYPE_OF_ATTACK_IS_PROHIBITED_WHEN_ALLIED_TROOPS_ARE_THE_TARGET : */SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;

		return null;
	}

	@Override
	public boolean canPK(Player target, Player killer)
	{
		if (!isInProgress())
			return true; // ТВ еще не начато

		final DominionSiegeEvent targetEvent = target.getEvent(DominionSiegeEvent.class);
		if (targetEvent == null) // убит не участник ТВ
			return true;

		return targetEvent == this; // не ПК если эвенты разные (за разные города)
	}

	@Override
	public void broadcastTo(IBroadcastPacket packet, String... types)
	{
		List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
		for(SiegeClanObject siegeClan : siegeClans)
			siegeClan.broadcast(packet);

		List<Integer> players = getObjects(DEFENDER_PLAYERS);
		for(int i : players)
		{
			Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
				player.sendPacket(packet);
		}
	}

	@Override
	public void broadcastTo(L2GameServerPacket packet, String... types)
	{
		List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
		for(SiegeClanObject siegeClan : siegeClans)
			siegeClan.broadcast(packet);

		List<Integer> players = getObjects(DEFENDER_PLAYERS);
		for(int i : players)
		{
			Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
				player.sendPacket(packet);
		}
	}

	@Override
	public void giveItem(Player player, int itemId, long count)
	{
		Zone zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
			count = 0;
		else
		{
			int id = zone.getParams().getInteger("residence");
			if(id < 100)
				count = 125;
			else
				count = 31;
		}

		addReward(player, ONLINE_REWARD, 1);
		super.giveItem(player, itemId, count);
	}

	@Override
	public List<Player> itemObtainPlayers()
	{
		List<Player> playersInZone = getPlayersInZone();

		List<Player> list = new LazyArrayList<Player>(playersInZone.size());
		for(Player player : getPlayersInZone())
		{
			if(player.getEvent(DominionSiegeEvent.class) != null)
				list.add(player);
		}
		return list;
	}

	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		if(!isInProgress())
			return;

		if(getObjects(FLAG_ZONES).isEmpty())
			return;

		SiegeClanObject clan = getSiegeClan(DEFENDERS, player.getClan());
		if(clan != null && clan.getFlag() != null)
			r.put(RestartType.TO_FLAG, Boolean.TRUE);
	}

	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		if(type == RestartType.TO_FLAG)
		{
			SiegeClanObject defenderClan = getSiegeClan(DEFENDERS, player.getClan());

			if(defenderClan != null && defenderClan.getFlag() != null)
				return Location.findPointToStay(defenderClan.getFlag(), 50, 75);
			else
				player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);

			return null;
		}

		return super.getRestartLoc(player, type);
	}

	@Override
	public Location getEnterLoc(Player player, Zone zone)
	{
		if(!isInProgress())
			return null;

		SiegeClanObject siegeClan = getSiegeClan(DEFENDERS, player.getClan());
		if(siegeClan != null)
		{
			if(siegeClan.getFlag() != null)
				return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
		}

		Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
		if(r == null)
		{
			error(toString(), new Exception("Not find residence: " + zone.getParams().getInteger("residence")));
			return player.getLoc();
		}
		return r.getNotOwnerRestartPoint(player);
	}

	@Override
	public void teleportPlayers(String t)
	{
		List<ZoneObject> zones = getObjects(SIEGE_ZONES);
		for(ZoneObject zone : zones)
		{
			Residence r = ResidenceHolder.getInstance().getResidence(zone.getZone().getParams().getInteger("residence"));

			r.banishForeigner();
		}
	}

	@Override
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		if(!isInProgress())
			return true;

		boolean playerInZone = checkIfInZone(active);
		boolean targetInZone = checkIfInZone(target);
		// если оба вне зоны - рес разрешен
		// если таргет вне осадный зоны - рес разрешен
		if(!playerInZone && !targetInZone || !targetInZone)
			return true;

		Player resurectPlayer = active.getPlayer();
		Player targetPlayer = target.getPlayer();

		// если оба незареганы - невозможно ресать
		// если таргет незареган - невозможно ресать
		DominionSiegeEvent siegeEvent1 = resurectPlayer.getEvent(DominionSiegeEvent.class);
		DominionSiegeEvent siegeEvent2 = targetPlayer.getEvent(DominionSiegeEvent.class);
		if(siegeEvent1 == null && siegeEvent2 == null || siegeEvent2 == null)
		{
			if(!quiet)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
				active.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
			}
			return false;
		}

		SiegeClanObject targetSiegeClan = siegeEvent2.getSiegeClan(DEFENDERS, targetPlayer.getClan());

		// если нету флага - рес запрещен
		if(targetSiegeClan == null || targetSiegeClan.getFlag() == null)
		{
			if(!quiet)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				active.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
			}
			return false;
		}

		if(force)
			return true;
		else
		{
			if(!quiet)
				active.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
	}
		//========================================================================================================================================================================
	//                                                                   Rewards
	//========================================================================================================================================================================
	public void setReward(int objectId, int type, int v)
	{
		int val[] = _playersRewards.get(objectId);
		if(val == null)
			_playersRewards.put(objectId, val = new int[REWARD_MAX]);

		val[type] = v;
	}

	public void addReward(Player player, int type, int v)
	{
		int val[] = _playersRewards.get(player.getObjectId());
		if(val == null)
			_playersRewards.put(player.getObjectId(), val = new int[REWARD_MAX]);

		val[type] += v;
	}

	public int getReward(Player player, int type)
	{
		int val[] = _playersRewards.get(player.getObjectId());
		if(val == null)
			return 0;
		else
			return val[type];
	}

	public void clearReward(int objectId)
	{
		if(_playersRewards.containsKey(objectId))
		{
			_playersRewards.remove(objectId);
			DominionRewardDAO.getInstance().delete(getResidence(), objectId);
		}
	}

	public Collection<IntObjectPair<int[]>> getRewards()
	{
		return _playersRewards.entrySet();
	}

	public int[] calculateReward(Player player)
	{
		int rewards[] = _playersRewards.get(player.getObjectId());
		if(rewards == null)
			return null;

		int[] out = new int[3];
		// статичные (старт, стоп, квесты, прочее)
		out[0] += rewards[STATIC_BADGES];
		// если онлайн ревард больше 14(70 мин в зоне) это 7 макс
		out[0] += rewards[ONLINE_REWARD] >= 14 ? 7 : rewards[ONLINE_REWARD] / 2;

		// насчитаем за убийство
		if(rewards[KILL_REWARD] < 50)
			out[0] += rewards[KILL_REWARD] * 0.1;
		else if(rewards[KILL_REWARD] < 120)
			out[0] += (5 + (rewards[KILL_REWARD] - 50) / 14);
		else
			out[0] += 10;

		//TODO [VISTALL] неверно, фейм дается и ниже, нету выдачи адены
		if(out[0] > 90)
		{
			out[0] = 90; // badges
			out[1] = 0; //TODO [VISTALL] adena count
			out[2] = 450; // fame
		}

		return out;
	}
	//========================================================================================================================================================================
	//                                                                   Getters/Setters
	//========================================================================================================================================================================

	public void setForSakeQuest(Quest forSakeQuest)
	{
		_forSakeQuest = forSakeQuest;
	}
}
