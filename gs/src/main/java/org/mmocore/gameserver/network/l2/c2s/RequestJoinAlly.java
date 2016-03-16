package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.model.pledge.Alliance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.AskJoinAlliance;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

public class RequestJoinAlly extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getClan() == null || activeChar.getAlliance() == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(activeChar.getAlliance().getMembersCount() >= Config.ALT_MAX_ALLY_SIZE)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
			return;
		}

		GameObject obj = activeChar.getVisibleObject(_objectId);
		if(obj == null || !obj.isPlayer() || obj == activeChar)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		Player target = (Player)obj;

		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(SystemMsg.THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS);
			return;
		}

		if(!target.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(target));
			return;
		}

		Clan targetClan = target.getClan();
		Alliance targetAlliance = targetClan.getAlliance();
		if(targetAlliance != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE).addString(targetClan.getName()).addString(targetAlliance.getAllyName()));
			return;
		}

		if(activeChar.isAtWarWith(targetClan.getClanId()))
		{
			activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_CURRENTLY_AT_WAR_WITH);
			return;
		}

		if(!targetClan.canJoinAlly())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_ALLIANCE).addString(targetClan.getName()));
			return;
		}

		if(!activeChar.getAlliance().canInvite())
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
			return;
		}

		if(target.isBusy())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return;
		}

		new Request(L2RequestType.ALLY, activeChar, target).setTimeout(10000L);
		target.sendPacket(new SystemMessage(SystemMsg.S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE).addString(activeChar.getAlliance().getAllyName()).addName(activeChar));
		target.sendPacket(new AskJoinAlliance(activeChar.getObjectId(), activeChar.getName(), activeChar.getAlliance().getAllyName()));
	}
}