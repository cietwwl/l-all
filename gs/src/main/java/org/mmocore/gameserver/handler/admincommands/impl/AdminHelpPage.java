package org.mmocore.gameserver.handler.admincommands.impl;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;

public class AdminHelpPage implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_showhtml
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		switch(command)
		{
			case admin_showhtml:
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Usage: //showhtml <file>");
					return false;
				}
				activeChar.sendPacket(new HtmlMessage(5).setFile("admin/" + wordList[1]));
				break;
		}

		return true;
	}

	public static void showHelpHtml(Player activeChar, String content)
	{
		HtmlMessage adminReply = new HtmlMessage(5);
		adminReply.setHtml(content);
		activeChar.sendPacket(adminReply);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}