package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;

public class RequestPledgeExtendedInfo extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isGM())
			activeChar.sendMessage("RequestPledgeExtendedInfo");

		// TODO this
	}
}