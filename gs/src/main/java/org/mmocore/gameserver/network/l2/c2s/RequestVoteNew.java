package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

public class RequestVoteNew extends L2GameClientPacket
{
	private int _targetObjectId;

	@Override
	protected void readImpl()
	{
		_targetObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || target.getObjectId() != _targetObjectId)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		if(target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}

		Player targetPlayer = (Player)target;

		if(activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_OUT_OF_RECOMMENDATIONS);
			return;
		}

		if(targetPlayer.getRecomHave() >= 255)
		{
			activeChar.sendPacket(SystemMsg.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}

		activeChar.giveRecom(targetPlayer);
		SystemMessage sm = new SystemMessage(SystemMsg.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addName(target);
		sm.addNumber(activeChar.getRecomLeft());
		activeChar.sendPacket(sm);

		sm = new SystemMessage(SystemMsg.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addName(activeChar);
		targetPlayer.sendPacket(sm);
	}
}