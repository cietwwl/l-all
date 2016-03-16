package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.s2c.RadarControl;
import org.mmocore.gameserver.scripts.Functions;

/**
 * @Author: Abaddon
 */
public class Help implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "help", "exp", "whereis" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("help"))
			return help(command, activeChar, args);
		if(command.equalsIgnoreCase("whereis"))
			return whereis(command, activeChar, args);
		if(command.equalsIgnoreCase("exp"))
			return exp(command, activeChar, args);

		return false;
	}

	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel"));
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft").addNumber(exp));
		}
		return true;
	}

	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = World.getPlayer(args);
		if(friend == null)
			return false;

		if(friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			RadarControl rc = new RadarControl(0, 1, friend.getLoc());
			activeChar.sendPacket(rc);
			return true;
		}

		return false;
	}

	private boolean help(String command, Player activeChar, String args)
	{
		String dialog = HtmCache.getInstance().getHtml("command/help.htm", activeChar);
		Functions.show(dialog, activeChar, null);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}