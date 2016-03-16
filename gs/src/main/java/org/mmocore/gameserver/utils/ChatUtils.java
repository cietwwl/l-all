package org.mmocore.gameserver.utils;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.s2c.NpcSay;
import org.mmocore.gameserver.network.l2.s2c.Say2;

public class ChatUtils
{
	private ChatUtils()
	{}

	private static void say(Player activeChar, GameObject activeObject, Iterable<Player> players, int range, Say2 cs)
	{
		for(Player player : players)
		{
			if (player.isBlockAll())
				continue;

			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			//Персонаж находится рядом с наблюдателем или точкой наблюдения
			if(activeObject.isInRangeZ(obj, range))
				if(!player.isInBlockList(activeChar) && activeChar.canTalkWith(player))
					player.sendPacket(cs);
		}
	}

	public static void say(Player activeChar, Say2 cs)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if (activeObject == null)
			activeObject = activeChar;

		say(activeChar, activeObject, World.getAroundObservers(activeObject), Config.CHAT_RANGE, cs);
	}

	public static void say(Player activeChar, Iterable<Player> players, Say2 cs)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if (activeObject == null)
			activeObject = activeChar;

		say(activeChar, activeObject, players, Config.CHAT_RANGE, cs);
	}

	public static void say(Player activeChar, int range, Say2 cs)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if (activeObject == null)
			activeObject = activeChar;

		say(activeChar, activeObject, World.getAroundObservers(activeObject), range, cs);
	}

	public static void shout(Player activeChar, Say2 cs)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if (activeObject == null)
			activeObject = activeChar;

		int rx = MapUtils.regionX(activeObject);
		int ry = MapUtils.regionY(activeObject);

		for(Player player : GameObjectsStorage.getPlayers())
		{
			if(player == activeChar || player.isBlockAll())
				continue;

			if (player.canSeeAllShouts() && !player.isInBlockList(activeChar) && activeChar.canTalkWith(player))
			{
				player.sendPacket(cs);
				continue;
			}

			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			if(activeObject.getReflection() != obj.getReflection())
				continue;

			int tx = MapUtils.regionX(obj) - rx;
			int ty = MapUtils.regionY(obj) - ry;

			if (tx*tx + ty*ty <= Config.SHOUT_SQUARE_OFFSET || activeObject.isInRangeZ(obj, Config.CHAT_RANGE))
				if (!player.isInBlockList(activeChar) && activeChar.canTalkWith(player))
					player.sendPacket(cs);
		}
	}

	public static void announce(Player activeChar, Say2 cs)
	{
		for(Player player : GameObjectsStorage.getPlayers())
		{
			if(player == activeChar || player.isBlockAll())
				continue;

			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			if (!player.isInBlockList(activeChar) && activeChar.canTalkWith(player))
				player.sendPacket(cs);
		}
	}

	public static void chat(NpcInstance activeChar, ChatType type, NpcString npcString, String... params)
	{
		switch (type)
		{
			case ALL:
			case NPC_ALL:
				say(activeChar, npcString, params);
				break;
			case SHOUT:
			case NPC_SHOUT:
				shout(activeChar, npcString, params);
				break;
		}
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, int range, NpcSay cs)
	{
		for(Player player : players)
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			//Персонаж находится рядом с наблюдателем или точкой наблюдения
			if(activeChar.isInRangeZ(obj, range))
				player.sendPacket(cs);
		}
	}

	public static void say(NpcInstance activeChar, NpcSay cs)
	{
		say(activeChar, World.getAroundObservers(activeChar), Config.CHAT_RANGE, cs);
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, NpcSay cs)
	{
		say(activeChar, players, Config.CHAT_RANGE, cs);
	}

	public static void say(NpcInstance activeChar, int range, NpcSay cs)
	{
		say(activeChar, World.getAroundObservers(activeChar), range, cs);
	}

	public static void say(NpcInstance activeChar, int range, NpcString npcString, String... params)
	{
		say(activeChar, range, new NpcSay(activeChar, ChatType.NPC_ALL, npcString, params));
	}

	public static void say(NpcInstance npc, NpcString npcString, String... params)
	{
		say(npc, Config.CHAT_RANGE, npcString, params);
	}

	public static void shout(NpcInstance activeChar, NpcSay cs)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);

		for(Player player : GameObjectsStorage.getPlayers())
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			if(activeChar.getReflection() != obj.getReflection())
				continue;

			int tx = MapUtils.regionX(obj) - rx;
			int ty = MapUtils.regionY(obj) - ry;

			if (tx*tx + ty*ty <= Config.SHOUT_SQUARE_OFFSET || activeChar.isInRangeZ(obj, Config.CHAT_RANGE))
				player.sendPacket(cs);
		}
	}

	public static void shout(NpcInstance activeChar, NpcString npcString, String... params)
	{
		shout(activeChar, new NpcSay(activeChar, ChatType.NPC_SHOUT, npcString, params));
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, int range, CustomMessage cm)
	{
		for(Player player : players)
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			//Персонаж находится рядом с наблюдателем или точкой наблюдения
			if(activeChar.isInRangeZ(obj, range))
				player.sendPacket(new NpcSay(activeChar, ChatType.NPC_SHOUT, cm.toString(player)));
		}
	}

	public static void say(NpcInstance activeChar, CustomMessage cm)
	{
		say(activeChar, World.getAroundObservers(activeChar), Config.CHAT_RANGE, cm);
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, CustomMessage cm)
	{
		say(activeChar, players, Config.CHAT_RANGE, cm);
	}

	public static void say(NpcInstance activeChar, int range, CustomMessage cm)
	{
		say(activeChar, World.getAroundObservers(activeChar), range, cm);
	}

	public static void shout(NpcInstance activeChar, CustomMessage cm)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);

		for(Player player : GameObjectsStorage.getPlayers())
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			if(activeChar.getReflection() != obj.getReflection())
				continue;

			int tx = MapUtils.regionX(obj) - rx;
			int ty = MapUtils.regionY(obj) - ry;

			if (tx*tx + ty*ty <= Config.SHOUT_SQUARE_OFFSET || activeChar.isInRangeZ(obj, Config.CHAT_RANGE))
				player.sendPacket(new NpcSay(activeChar, ChatType.NPC_SHOUT, cm.toString(player)));
		}
	}
}
