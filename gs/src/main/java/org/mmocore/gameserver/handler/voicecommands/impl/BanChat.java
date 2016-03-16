package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.utils.AdminFunctions;
import org.mmocore.gameserver.utils.Util;

public class BanChat implements IVoicedCommandHandler
{
	private static final int DEFAULT_BAN_TIME = 30; // minutes

	private final String[] _commandList = new String[] { "banchat", "unbanchat", "disablechat", "seeall" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		String[] param = null;
		if (args != null && !args.isEmpty())
			param = args.split(" ");
			
		command = command.intern();
		if (_commandList[0].equalsIgnoreCase(command)) // banchat
		{
			if(!player.getPlayerAccess().CanBanChat)
				return false;

			if (param == null || param.length < 1)
			{
				player.sendMessage("Usage: .banchat nickname [minutes] [reason]");
				return true;
			}

			int time = DEFAULT_BAN_TIME;
			try
			{
				time = Integer.parseInt(param[1]);
			}
			catch (Exception e)
			{
			}

			if (time < 0)
			{
				if (!player.getPlayerAccess().CanDisableChat)
					time = DEFAULT_BAN_TIME;
			}
			else if (time == 0 && !player.getPlayerAccess().CanUnBanChat)
				return false;

			player.sendMessage(AdminFunctions.banChat(player, null, param[0], time, param.length > 2 ? Util.joinStrings(" ", param, 2) : null, false));
		}
		else if (_commandList[1].equalsIgnoreCase(command)) // unbanchat
		{
			if(!player.getPlayerAccess().CanUnBanChat)
				return false;
			
			if (param == null || param.length < 1)
			{
				player.sendMessage("Usage: .unbanchat nickname");
				return true;
			}

			player.sendMessage(AdminFunctions.banChat(player, null, param[0], 0, null, false));
		}
		else if (_commandList[2].equalsIgnoreCase(command)) // disablechat
		{
			if(!player.getPlayerAccess().CanDisableChat)
				return false;
			
			if (param == null || param.length < 1)
			{
				player.sendMessage("Usage: .disablechat nickname [reason]");
				return true;
			}

			player.sendMessage(AdminFunctions.banChat(player, null, param[0], -1, param.length > 2 ? Util.joinStrings(" ", param, 2) : null, true));
		}
		else if (_commandList[3].equalsIgnoreCase(command)) // seeall
		{
			if(!player.getPlayerAccess().CanSeeAllShouts)
				return false;

			if (player.canSeeAllShouts())
			{
				player.setCanSeeAllShouts(false);
				player.sendMessage("Shouts display disabled");
			}
			else
			{
				player.setCanSeeAllShouts(true);
				player.sendMessage("Shouts display enabled");
			}
		}

		return true;
	}
}