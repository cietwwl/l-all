package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.xml.holder.HennaHolder;
import org.mmocore.gameserver.templates.Henna;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.HennaItemInfo;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	// format  cd
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
		if(henna != null)
			player.sendPacket(new HennaItemInfo(henna, player));
	}
}