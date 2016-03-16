package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.templates.Henna;

public class RequestHennaUnequip extends L2GameClientPacket
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

		for(int i = 1; i <= 3; i ++)
		{
			Henna henna = player.getHenna(i);
			if(henna == null)
				continue;

			if(henna.getSymbolId() == _symbolId)
			{
				long price = henna.getPrice() / 5;
				if(!player.reduceAdena(price))
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}

				player.removeHenna(i);

				player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
				return;
			}
		}
	}
}