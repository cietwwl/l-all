package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.JoinParty;

public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Request request = activeChar.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.PARTY))
			return;

		if(!request.isInProgress())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			activeChar.sendActionFailed();
			return;
		}

		if(requestor.getRequest() != request)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		// отказ(0) или автоматический отказ(-1)
		if(_response <= 0)
		{
			request.cancel();
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}

		if(requestor.isInOlympiadMode())
		{
			request.cancel();
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}

		Party party = requestor.getParty();

		if(party != null && party.getMemberCount() >= Party.MAX_SIZE)
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
			requestor.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}

		IBroadcastPacket problem = activeChar.canJoinParty(requestor);
		if(problem != null)
		{
			request.cancel();
			activeChar.sendPacket(problem, ActionFail.STATIC);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}

		if(party == null)
		{
			int itemDistribution = request.getInteger("itemDistribution");
			requestor.setParty(party = new Party(requestor, itemDistribution));
		}

		try
		{
			activeChar.joinParty(party);
			activeChar.sendPacket(new JoinParty(2, activeChar.getServitor() != null));
			requestor.sendPacket(new JoinParty(1, activeChar.getServitor() != null));
		}
		finally
		{
			request.done();
		}
	}
}