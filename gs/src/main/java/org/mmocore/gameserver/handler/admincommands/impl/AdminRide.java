package org.mmocore.gameserver.handler.admincommands.impl;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.tables.PetDataTable;

public class AdminRide implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_ride,
		admin_ride_wyvern,
		admin_ride_strider,
		admin_unride,
		admin_wr,
		admin_sr,
		admin_ur
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Rider)
			return false;

		switch(command)
		{
			case admin_ride:
				if(activeChar.isMounted() || activeChar.getServitor() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Incorrect id.");
					return false;
				}
				activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85, -1);
				break;
			case admin_ride_wyvern:
			case admin_wr:
				if(activeChar.isMounted() || activeChar.getServitor() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				activeChar.setMount(PetDataTable.WYVERN_ID, 0, 85, -1);
				break;
			case admin_ride_strider:
			case admin_sr:
				if(activeChar.isMounted() || activeChar.getServitor() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				activeChar.setMount(PetDataTable.STRIDER_WIND_ID, 0, 85, -1);
				break;
			case admin_unride:
			case admin_ur:
				activeChar.dismount();
				break;
		}

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}