package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;

public class Ping implements IVoicedCommandHandler
{

	private final String[] _commandList = new String[] { "ping" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("ping"))
		{
			activeChar.sendMessage("Ping: " + activeChar.getNetConnection().getPing() + " ms");
			return true;
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

}
