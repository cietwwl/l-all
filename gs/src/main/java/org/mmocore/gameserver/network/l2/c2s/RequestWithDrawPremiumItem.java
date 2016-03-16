package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.PremiumItem;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExGetPremiumItemList;
import org.mmocore.gameserver.utils.ItemFunctions;

//FIXME [G1ta0] item-API
public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemcount;

	@Override
	protected void readImpl()
	{
		_itemNum = readD();
		_charId = readD();
		_itemcount = readQ();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;
		if(_itemcount <= 0)
			return;

		if(activeChar.getObjectId() != _charId)
			// audit
			return;
		if(activeChar.getPremiumItemList().isEmpty())
			// audit
			return;
		if(activeChar.getWeightPenalty() >= 3 || activeChar.getInventoryLimit() * 0.8 <= activeChar.getInventory().getSize())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RECEIVE_THE_DIMENSIONAL_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHTQUANTITY_LIMIT);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RECEIVE_A_DIMENSIONAL_ITEM_DURING_AN_EXCHANGE);
			return;
		}

		PremiumItem item = activeChar.getPremiumItemList().get(_itemNum);
		if(item == null || item.getCount() < _itemcount)
			return;

		ItemFunctions.addItem(activeChar, item.getItemId(), _itemcount);

		if(_itemcount < item.getCount())
		{
			activeChar.getPremiumItemList().get(_itemNum).updateCount(item.getCount() - _itemcount);
			activeChar.updatePremiumItem(_itemNum, item.getCount() - _itemcount);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}

		if(activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(SystemMsg.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
		else
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
	}
}