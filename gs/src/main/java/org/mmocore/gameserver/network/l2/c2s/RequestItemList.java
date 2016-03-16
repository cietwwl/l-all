package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.StatusUpdate;

public class RequestItemList extends L2GameClientPacket
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

		if(activeChar.isBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendItemList(true);
		activeChar.sendStatusUpdate(false, false, StatusUpdate.CUR_LOAD);
	}
}