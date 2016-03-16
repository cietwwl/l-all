package org.mmocore.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 */
public class GameObjectsStorage
{
	private static IntObjectMap<GameObject> _objects = new CHashIntObjectMap<GameObject>(60000 * Config.RATE_MOB_SPAWN + Config.MAXIMUM_ONLINE_USERS);
	private static IntObjectMap<NpcInstance> _npcs = new CHashIntObjectMap<NpcInstance>(60000 * Config.RATE_MOB_SPAWN);
	private static IntObjectMap<Player> _players = new CHashIntObjectMap<Player>(Config.MAXIMUM_ONLINE_USERS);

	public static Player getPlayer(String name)
	{
		for(Player player : _players.values())
			if(player.getName().equalsIgnoreCase(name))
				return player;
		return null;
	}

	public static Player getPlayer(int objId)
	{
		return _players.get(objId);
	}

	public static Collection<Player> getPlayers()
	{
		return _players.values();
	}

	public static GameObject findObject(int objId)
	{
		return _objects.get(objId);
	}

	public static Iterable<NpcInstance> getNpcs()
	{
		return _npcs.values();
	}

	public static NpcInstance getByNpcId(int npcId)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getNpcs())
			if(temp.getNpcId() == npcId)
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	public static List<NpcInstance> getAllByNpcId(int npcId, boolean justAlive)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getNpcs())
			if(temp.getNpcId() == npcId && (!justAlive || !temp.isDead()))
				result.add(temp);
		return result;
	}

	public static List<NpcInstance> getAllByNpcId(int[] npcIds, boolean justAlive)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getNpcs())
			if(!justAlive || !temp.isDead())
				if(ArrayUtils.contains(npcIds, temp.getNpcId()))
					result.add(temp);
		return result;
	}

	public static NpcInstance getNpc(int objId)
	{
		return _npcs.get(objId);
	}

	public static <T extends GameObject> void put(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.put(o.getObjectId(), o);

		_objects.put(o.getObjectId(), o);
	}

	public static <T extends GameObject> void remove(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.remove(o.getObjectId());

		_objects.remove(o.getObjectId());
	}

	@SuppressWarnings("unchecked")
	private static <T extends GameObject> IntObjectMap<T> getMapForObject(T o)
	{
		if(o.isNpc())
			return (IntObjectMap<T>) _npcs;

		if(o.isPlayer())
			return (IntObjectMap<T>) _players;

		return null;
	}
}