package org.mmocore.gameserver.handler.admincommands.impl;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;

public class AdminEvents implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_events
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().IsEventGm)
			return false;

		switch(command)
		{
			case admin_events:
				if(wordList.length == 1)
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/events/events.htm"));
				else
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/events/" + wordList[1].trim()));
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