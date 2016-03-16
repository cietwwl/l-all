package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	// format: cS

	private String _bypass;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.processQuestEvent(255, _bypass, null);
	}
}