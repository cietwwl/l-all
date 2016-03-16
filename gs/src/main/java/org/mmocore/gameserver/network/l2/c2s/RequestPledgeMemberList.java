package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.Player;

public class RequestPledgeMemberList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Clan clan = activeChar.getClan();
		if(clan != null)
		{
			activeChar.sendPacket(clan.listAll());
		}
	}
}