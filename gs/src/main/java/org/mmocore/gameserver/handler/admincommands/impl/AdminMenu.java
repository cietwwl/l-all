package org.mmocore.gameserver.handler.admincommands.impl;

import java.util.StringTokenizer;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.utils.AdminFunctions;
import org.mmocore.gameserver.utils.Location;

public class AdminMenu implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_char_manage,
		admin_teleport_character_to_menu,
		admin_recall_char_menu,
		admin_goto_char_menu,
		admin_kick_menu,
		admin_kill_menu,
		admin_ban_menu,
		admin_unban_menu
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		if(!activeChar.getPlayerAccess().Menu)
			return false;

		if(fullString.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = fullString.split(" ");
			if(data.length == 5)
			{
				String playerName = data[1];
				Player player = World.getPlayer(playerName);
				if(player != null)
					teleportCharacter(player, new Location(Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])), activeChar);
			}
		}
		else if(fullString.startsWith("admin_recall_char_menu"))
			try
		{
				String targetName = fullString.substring(23);
				Player player = World.getPlayer(targetName);
				teleportCharacter(player, activeChar.getLoc(), activeChar);
		}
		catch(StringIndexOutOfBoundsException e)
		{}
		else if(fullString.startsWith("admin_goto_char_menu"))
			try
		{
				String targetName = fullString.substring(21);
				Player player = World.getPlayer(targetName);
				teleportToCharacter(activeChar, player);
		}
		catch(StringIndexOutOfBoundsException e)
		{}
		else if(fullString.equals("admin_kill_menu"))
		{
			GameObject obj = activeChar.getTarget();
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				Player plyr = World.getPlayer(player);
				if(plyr == null)
					activeChar.sendMessage("Player " + player + " not found in game.");
				obj = plyr;
			}
			if(obj != null && obj.isCreature())
			{
				Creature target = (Creature) obj;
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null, 0, false, true, true, true, false, false, false, true);
			}
			else
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
		}
		else if(fullString.startsWith("admin_kick_menu"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				if (AdminFunctions.kick(player, "kick"))
					activeChar.sendMessage("Player kicked.");
			}
		}

		activeChar.sendPacket(new HtmlMessage(5).setFile("admin/charmanage.htm"));
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void teleportCharacter(Player player, Location loc, Player activeChar)
	{
		if(player != null)
		{
			player.sendMessage("Admin is teleporting you.");
			player.teleToLocation(loc);
		}
	}

	private void teleportToCharacter(Player activeChar, GameObject target)
	{
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		if(player.getObjectId() == activeChar.getObjectId())
			activeChar.sendMessage("You cannot self teleport.");
		else
		{
			activeChar.teleToLocation(player.getLoc());
			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
	}
}