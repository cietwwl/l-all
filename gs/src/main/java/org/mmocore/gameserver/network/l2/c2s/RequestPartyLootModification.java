package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;

public class RequestPartyLootModification extends L2GameClientPacket
{
	private byte _mode;

	@Override
	protected void readImpl()
	{
		_mode = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_mode < 0 || _mode > Party.ITEM_ORDER_SPOIL)
			return;

		Party party = activeChar.getParty();
		if(party == null || _mode == party.getLootDistribution() || party.getPartyLeader() != activeChar)
			return;

		party.requestLootChange(_mode);
	}
}
