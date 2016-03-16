package org.mmocore.gameserver.network.l2.c2s;


import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.utils.BypassStorage.ValidBypass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author n0nam3
 * @date 22/08/2010 15:16
 */

public class RequestLinkHtml extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestLinkHtml.class);

	//Format: cS
	private String _link;

	@Override
	protected void readImpl()
	{
		_link = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_link.contains("..") || !_link.endsWith(".htm"))
		{
			_log.warn("RequestLinkHtml: hack? link contains prohibited characters: '" + _link + "'!");
			return;
		}

		ValidBypass bp = player.getBypassStorage().validate(_link);
		if(bp == null)
		{
			_log.warn(" RequestLinkHtml: Unexpected link : " + _link + "!");
			return;
		}

		HtmlMessage msg = player.getLastNpc() == null ? new HtmlMessage(0) : new HtmlMessage(player.getLastNpc());
		msg.setFile(_link);
		player.sendPacket(msg);
	}
}