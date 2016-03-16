package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.PcInventory;
import org.mmocore.gameserver.network.l2.s2c.ExPutEnchantSupportItemResult;
import org.mmocore.gameserver.utils.ItemFunctions;

public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private int _itemId;
	private int _catalystId;

	@Override
	protected void readImpl()
	{
		_catalystId = readD();
		_itemId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_itemId);
		ItemInstance catalyst = inventory.getItemByObjectId(_catalystId);

		if(ItemFunctions.checkCatalyst(itemToEnchant, catalyst))
			activeChar.sendPacket(new ExPutEnchantSupportItemResult(1));
		else
			activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
	}
}