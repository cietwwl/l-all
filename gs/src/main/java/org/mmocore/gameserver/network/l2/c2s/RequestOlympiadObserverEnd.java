package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
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
		if(activeChar.isInOlympiadObserverMode())
			if(activeChar.getObserverMode() == Player.OBSERVER_STARTED)
				activeChar.leaveOlympiadObserverMode(true);
	}
}