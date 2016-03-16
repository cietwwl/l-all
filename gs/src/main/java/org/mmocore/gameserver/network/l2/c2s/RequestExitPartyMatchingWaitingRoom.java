package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.instancemanager.MatchingRoomManager;
import org.mmocore.gameserver.model.Player;

/**
 * Format: (ch)
 */
public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoomManager.getInstance().removeFromWaitingList(player);
	}
}