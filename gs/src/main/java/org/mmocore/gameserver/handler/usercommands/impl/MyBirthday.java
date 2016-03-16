package org.mmocore.gameserver.handler.usercommands.impl;

import java.util.Calendar;

import org.mmocore.gameserver.handler.usercommands.IUserCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;


/**
 * Support for /mybirthday command
 */
public class MyBirthday implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 126 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(activeChar.getCreateTime() == 0)
			return false;

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(activeChar.getCreateTime());

		activeChar.sendPacket(new SystemMessage(SystemMsg.C1S_BIRTHDAY_IS_S3S4S2).addName(activeChar).addNumber(c.get(Calendar.YEAR)).addNumber(c.get(Calendar.MONTH) + 1).addNumber(c.get(Calendar.DAY_OF_MONTH)));

		if(c.get(Calendar.MONTH) == Calendar.FEBRUARY && c.get(Calendar.DAY_OF_WEEK) == 29)
			activeChar.sendPacket(SystemMsg.A_CHARACTER_BORN_ON_FEBRUARY_29_WILL_RECEIVE_A_GIFT_ON_FEBRUARY_28);
		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}