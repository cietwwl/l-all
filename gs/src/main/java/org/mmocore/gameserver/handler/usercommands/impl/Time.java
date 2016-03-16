package org.mmocore.gameserver.handler.usercommands.impl;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.GameTimeController;
import org.mmocore.gameserver.handler.usercommands.IUserCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;


/**
 * Support for /time command
 */
public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 77 };

	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	private static final SimpleDateFormat sf = new SimpleDateFormat("H:mm");
	static
	{
		df.setMinimumIntegerDigits(2);
	}

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		int h = GameTimeController.getInstance().getGameHour();
		int m = GameTimeController.getInstance().getGameMin();

		SystemMessage sm;
		if(GameTimeController.getInstance().isNowNight())
			sm = new SystemMessage(SystemMsg.THE_CURRENT_TIME_IS_S1S2);
		else
			sm = new SystemMessage(SystemMsg.THE_CURRENT_TIME_IS_S1S2);
		sm.addString(df.format(h)).addString(df.format(m));

		activeChar.sendPacket(sm);

		if (Config.ALT_SHOW_SERVER_TIME)
			activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime").addString(sf.format(new Date(System.currentTimeMillis()))));

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
