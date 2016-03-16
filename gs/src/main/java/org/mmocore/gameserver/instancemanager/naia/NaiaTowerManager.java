package org.mmocore.gameserver.instancemanager.naia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.lang.reference.HardReferences;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pchayka
 */
public final class NaiaTowerManager
{
	private static final Logger _log = LoggerFactory.getLogger(NaiaTowerManager.class);

	private Map<Integer, List<HardReference<Player>>> _groupList = new HashMap<Integer, List<HardReference<Player>>>();
	private Map<Integer, List<Integer>> _roomsDone = new HashMap<Integer, List<Integer>>();
	private Map<Integer, Long> _groupTimer = new HashMap<Integer, Long>();
	private Map<Integer, List<NpcInstance>> _roomMobs;
	private long _towerAccessible = 0;
	private int _index = 0;
	public HashMap<Integer, Boolean> lockedRooms;

	private static Location KICK_LOC = new Location(17656, 244328, 11595);

	private static final NaiaTowerManager _instance = new NaiaTowerManager();

	public static final NaiaTowerManager getInstance()
	{
		return _instance;
	}

	private NaiaTowerManager()
	{
		if(lockedRooms == null)
		{
			lockedRooms = new HashMap<Integer, Boolean>();
			for(int i = 18494; i <= 18505; i++)
				lockedRooms.put(i, false);

			_roomMobs = new HashMap<Integer, List<NpcInstance>>();
			for(int i = 18494; i <= 18505; i++)
				_roomMobs.put(i, new ArrayList<NpcInstance>());

			_log.info("Naia Tower Manager: Loaded 12 rooms");
		}
		ThreadPoolManager.getInstance().schedule(new GroupTowerTimer(), 30 * 1000L);
	}

	public void startNaiaTower(Player leader, NpcInstance controller)
	{
		if(leader == null || leader.getParty() == null)
			return;

		if(_towerAccessible > System.currentTimeMillis())
			return;

		_index = _groupList.keySet().size() + 1;
		final Party party = leader.getParty();
		List<HardReference<Player>> members = new ArrayList<HardReference<Player>>(party.getMemberCount());
		for (Player member : party.getPartyMembers())
			if (member.isInRange(controller, 600))
			{
				members.add(member.getRef());
				member.teleToLocation(new Location(-47271, 246098, -9120));				
			}

		_groupList.put(_index, members);
		_groupTimer.put(_index, System.currentTimeMillis() + 5 * 60 * 1000L);

		leader.sendMessage("The Tower of Naia countdown has begun. You have only 5 minutes to pass each room.");

		_towerAccessible += 20 * 60 * 1000L;

		ReflectionUtils.getDoor(18250001).openMe();
	}

	public void updateGroupTimer(Player player)
	{
		for(int i : _groupList.keySet())
			for (Player p :HardReferences.unwrap(_groupList.get(i)))
				if(p.equals(player))
				{
					_groupTimer.put(i, System.currentTimeMillis() + 5 * 60 * 1000L);
					player.sendMessage("Group timer has been updated");
					return;
				}
	}

	public void removeGroupTimer(Player player)
	{
		for(int i : _groupList.keySet())
			for (Player p : HardReferences.unwrap(_groupList.get(i)))
				if(p.equals(player))
				{
					_groupList.remove(i);
					_groupTimer.remove(i);
					return;
				}
	}

	public boolean isLegalGroup(Player player)
	{
		if(_groupList == null || _groupList.isEmpty())
			return false;

		for(int i : _groupList.keySet())
			for (Player p : HardReferences.unwrap(_groupList.get(i)))
				if (p.equals(player))
					return true;

		return false;
	}

	public void lockRoom(int npcId)
	{
		lockedRooms.put(npcId, true);
	}

	public void unlockRoom(int npcId)
	{
		lockedRooms.put(npcId, false);
	}

	public boolean isLockedRoom(int npcId)
	{
		return lockedRooms.get(npcId);
	}

	public void addRoomDone(int roomId, Player player)
	{
		final List<Integer> members;
		final Party party = player.getParty();
		if (party != null)
		{
			members = new ArrayList<Integer>(party.getMemberCount());
			for (Player member : party.getPartyMembers())
				if (member.isInRange(player, 5000))
					members.add(member.getObjectId());
		}
		else
		{
			members = new ArrayList<Integer>(1);
			members.add(player.getObjectId());
		}

		_roomsDone.put(roomId, members);
	}

	public boolean isRoomDone(int roomId, Player player)
	{
		if(_roomsDone == null || _roomsDone.isEmpty())
			return false;

		final List<Integer> group = _roomsDone.get(roomId);
		if(group == null || group.isEmpty())
			return false;

		if(group.contains(player.getObjectId()))
			return true;

		return false;
	}

	public void addMobsToRoom(int roomId, List<NpcInstance> mob)
	{
		_roomMobs.put(roomId, mob);
	}

	public List<NpcInstance> getRoomMobs(int roomId)
	{
		return _roomMobs.get(roomId);
	}

	public void removeRoomMobs(int roomId)
	{
		_roomMobs.get(roomId).clear();
	}

	private class GroupTowerTimer extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Iterator<Map.Entry<Integer, Long>> iterator = _groupTimer.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<Integer, Long> entry = iterator.next();
				if(entry.getValue() < System.currentTimeMillis())
				{
					List<HardReference<Player>> party = _groupList.remove(entry.getKey());
					for(Player pl : HardReferences.unwrap(party))
					{
						pl.teleToLocation(KICK_LOC);
						pl.sendMessage("The time has expired. You cannot stay in Tower of Naia any longer");
						for (List<Integer> doneRoom : _roomsDone.values())
							doneRoom.remove(Integer.valueOf(pl.getObjectId()));
					}

					iterator.remove();
				}
			}

			ThreadPoolManager.getInstance().schedule(this, 30000L);
		}
	}
}