package org.mmocore.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.SystemMsg;

public class AdminKill implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_kill,
		admin_damage,
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;

		switch(command)
		{
			case admin_kill:
				if(wordList.length == 1)
					handleKill(activeChar);
				else
					handleKill(activeChar, wordList[1]);
				break;
			case admin_damage:
				handleDamage(activeChar, NumberUtils.toInt(wordList[1], 1));
				break;
		}

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleKill(Player activeChar)
	{
		handleKill(activeChar, null);
	}

	private void handleKill(Player activeChar, String player)
	{
		GameObject obj = activeChar.getTarget();
		if(player != null)
		{
			Player plyr = World.getPlayer(player);
			if(plyr != null)
				obj = plyr;
			else
			{
				int radius = Math.max(Integer.parseInt(player), 100);
				for(Creature cha : activeChar.getAroundCharacters(radius, 200))
					cha.reduceCurrentHp(cha.getMaxHp() + 1, activeChar, null, 0, false, true, true, false, false, false, false, true);

				activeChar.sendMessage("Killed within " + radius + " unit radius.");
				return;
			}
		}

		if(obj != null && obj.isCreature())
		{
			Creature target = (Creature) obj;
			int damageDone;
			if(target.isPlayer())
				damageDone = target.getMaxHp() + target.getMaxCp();
			else
				damageDone = target.getMaxHp();
			target.reduceCurrentHp(damageDone + 1, activeChar, null, 0, false, true, true, false, false, false, false, true);
		}
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void handleDamage(Player activeChar, int damage)
	{
		GameObject obj = activeChar.getTarget();

		if(obj == null)
		{
			activeChar.sendPacket(SystemMsg.SELECT_TARGET);
			return;
		}

		if(!obj.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Creature cha = (Creature) obj;
		cha.reduceCurrentHp(damage, activeChar, null, 0, false, true, true, false, false, false, false, true);
		activeChar.sendMessage("You gave " + damage + " damage to " + cha.getName() + ".");
	}
}