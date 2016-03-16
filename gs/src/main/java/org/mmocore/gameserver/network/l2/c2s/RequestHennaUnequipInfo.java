package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.xml.holder.HennaHolder;
import org.mmocore.gameserver.templates.Henna;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.HennaUnequipInfo;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * format: d
	 */
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
			player.sendPacket(new HennaUnequipInfo(henna, player));
	}
}