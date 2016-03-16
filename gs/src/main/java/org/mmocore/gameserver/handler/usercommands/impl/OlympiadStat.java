package org.mmocore.gameserver.handler.usercommands.impl;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.usercommands.IUserCommandHandler;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * Support for /olympiadstat command
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 109 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		GameObject objectTarget = activeChar.getTarget() == null || Config.OLYMPIAD_OLDSTYLE_STAT ? activeChar : activeChar.getTarget();
		if(!objectTarget.isPlayer() || !objectTarget.getPlayer().isNoble())
		{
			activeChar.sendPacket(SystemMsg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
			return true;
		}

		Player playerTarget = objectTarget.getPlayer();

		SystemMessage sm = new SystemMessage(SystemMsg.FOR_THE_CURRENT_GRAND_OLYMPIAD_YOU_HAVE_PARTICIPATED_IN_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_CURRENTLY_HAVE_S4_OLYMPIAD_POINTS);
		sm.addNumber(Olympiad.getCompetitionDone(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getCompetitionWin(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getCompetitionLoose(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getNoblePoints(playerTarget.getObjectId()));

		activeChar.sendPacket(sm);

		int[] ar = Olympiad.getWeekGameCounts(playerTarget.getObjectId());
		sm = new SystemMessage(SystemMsg.YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTICIPATE_IN_THIS_WEEK_S2_1_VS_1_CLASS_MATCHES_S3_1_VS_1_MATCHES__S4_3_VS_3_TEAM_MATCHES);
		sm.addNumber(ar[0]);
		sm.addNumber(ar[1]);
		sm.addNumber(ar[2]);
		sm.addNumber(ar[3]);
		activeChar.sendPacket(sm);
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}