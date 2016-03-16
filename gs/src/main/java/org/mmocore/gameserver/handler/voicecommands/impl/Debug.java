package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.CustomMessage;

public class Debug implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "debug" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if (!Config.ALT_DEBUG_ENABLED)
			return false;

		if (player.isDebug())
		{
			player.setDebug(false);
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Disabled"));
		}
		else
		{
			player.setDebug(true);
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Enabled"));
		}
		return true;
	}
}
