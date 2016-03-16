package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.bbs.BbsHandlerHolder;
import org.mmocore.gameserver.handler.bbs.IBbsHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;

public class RequestShowBoard extends L2GameClientPacket
{
	private int _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readD();
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.COMMUNITYBOARD_ENABLED)
		{
			activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(Config.BBS_DEFAULT);
		if(handler != null)
			handler.onBypassCommand(activeChar, Config.BBS_DEFAULT);
	}
}
