package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.CustomMessage;

public class Loc implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "loc" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Loc.My").addNumber(activeChar.getX()).addNumber(activeChar.getY()).addNumber(activeChar.getZ()));
		GameObject target = activeChar.getTarget();
		if (target == null)
			return true;

		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Loc.Target").addNumber(target.getX()).addNumber(target.getY()).addNumber(target.getZ()));
		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Loc.Distance").addNumber((int)activeChar.getDistance(target)).addNumber((int)activeChar.getDistance3D(target)));
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
