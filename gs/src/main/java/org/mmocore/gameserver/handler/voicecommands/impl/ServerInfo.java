package org.mmocore.gameserver.handler.voicecommands.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mmocore.gameserver.GameServer;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;


public class ServerInfo implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "rev", "ver", "date", "time" };

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("rev") || command.equals("ver"))
		{
			activeChar.sendMessage("Revision: " + GameServer.getInstance().getVersion().getVersionRevision());
			return true;
		}
		else if(command.equals("date") || command.equals("time"))
		{
			activeChar.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}

		return false;
	}
}
