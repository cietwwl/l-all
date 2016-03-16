package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.xml.holder.HennaHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.templates.Henna;

public class RequestHennaEquip extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * packet type id 0x6F
	 * format:		cd
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

		Henna temp = HennaHolder.getInstance().getHenna(_symbolId);
		if(temp == null || !temp.isForThisClass(player))
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}

		long countDye = player.getInventory().getCountOf(temp.getDyeId());
		if(countDye < temp.getDrawCount())
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}

		if(!player.reduceAdena(temp.getPrice(), true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(player.consumeItem(temp.getDyeId(), temp.getDrawCount()))
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_ADDED);
			player.addHenna(temp);
		}
	}
}