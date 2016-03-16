package org.mmocore.gameserver.handler.admincommands.impl;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.SystemMsg;

public class AdminCancel implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_cancel
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;

		switch(command)
		{
			case admin_cancel:
				handleCancel(activeChar, wordList.length > 1 ? wordList[1] : null);
				break;
		}

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleCancel(Player activeChar, String targetName)
	{
		GameObject obj = activeChar.getTarget();
		if(targetName != null)
		{
			Player plyr = World.getPlayer(targetName);
			if(plyr != null)
				obj = plyr;
			else
				try
			{
					int radius = Math.max(Integer.parseInt(targetName), 100);
					for(Creature character : activeChar.getAroundCharacters(radius, 200))
						character.getEffectList().stopAllEffects();
					activeChar.sendMessage("Apply Cancel within " + radius + " unit radius.");
					return;
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("Enter valid player name or radius");
				return;
			}
		}

		if(obj == null)
			obj = activeChar;
		if(obj.isCreature())
			((Creature) obj).getEffectList().stopAllEffects();
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}
}