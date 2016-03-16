package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.bbs.BbsHandlerHolder;
import org.mmocore.gameserver.handler.bbs.IBbsHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.utils.BypassStorage.ValidBypass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Format SSSSSS
 */
public class RequestBBSwrite extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBBSwrite.class);

	private String _url;
	private String _arg1;
	private String _arg2;
	private String _arg3;
	private String _arg4;
	private String _arg5;

	@Override
	public void readImpl()
	{
		_url = readS();
		_arg1 = readS();
		_arg2 = readS();
		_arg3 = readS();
		_arg4 = readS();
		_arg5 = readS();
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		ValidBypass bp = activeChar.getBypassStorage().validate(_url);
		if(bp == null)
		{
			_log.warn("RequestBBSwrite: Unexpected bypass : " + _url + " client : " + getClient() + "!");
			return;
		}

		if(!Config.COMMUNITYBOARD_ENABLED)
		{
			activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(_url);
		if(handler != null)
			handler.onWriteCommand(activeChar, _url, _arg1, _arg2, _arg3, _arg4, _arg5);
	}
}