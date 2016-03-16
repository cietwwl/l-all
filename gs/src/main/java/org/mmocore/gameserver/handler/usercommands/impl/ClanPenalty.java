package org.mmocore.gameserver.handler.usercommands.impl;

import org.mmocore.gameserver.handler.usercommands.IUserCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.utils.TimeUtils;


/**
 * Support for command: /clanpenalty
 */
public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 100, 114 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		HtmlMessage msg = new HtmlMessage(0);
		msg.setFile("pledge_penalty.htm");

		if(activeChar.getLeaveClanTime() != 0)
			msg.addVar("UNABLE_TO_JOIN_PLEDGE", TimeUtils.toSimpleFormat(activeChar.getLeaveClanTime() + Clan.JOIN_PLEDGE_PENALTY));
		if(activeChar.getDeleteClanTime() != 0)
			msg.addVar("UNABLE_TO_DISMISS_PLEDGE", TimeUtils.toSimpleFormat(activeChar.getDeleteClanTime() + Clan.CREATE_PLEDGE_PENALTY));
		if(activeChar.getClan() != null)
		{
			final long expelledTime = activeChar.getClan().getExpelledMemberTime() + Clan.EXPELLED_MEMBER_PENALTY;
			if(expelledTime > System.currentTimeMillis())
				msg.addVar("UNABLE_TO_ACCEPT_NEW_PLEDGE_MEMBER", TimeUtils.toSimpleFormat(expelledTime));
			if(activeChar.getClan().getDisbandPenaltyTime() > System.currentTimeMillis())
				msg.addVar("UNABLE_TO_DISMISS_PLEDGE", TimeUtils.toSimpleFormat(activeChar.getClan().getDisbandPenaltyTime()));
		}

		activeChar.sendPacket(msg);
		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}