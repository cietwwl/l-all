package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.Warehouse.ItemClassComparator;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final ItemInstance[] _itemList;
	private final String _charName;
	private final long _charAdena;

	public GMViewWarehouseWithdrawList(Player cha)
	{
		_charName = cha.getName();
		_charAdena = cha.getAdena();
		_itemList = cha.getWarehouse().getItems();
		ArrayUtils.eqSort(_itemList, ItemClassComparator.getInstance());
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeS(_charName);
		writeQ(_charAdena);
		writeH(_itemList.length);
		for(ItemInstance temp : _itemList)
		{
			writeItemInfo(temp);
			writeD(temp.getObjectId());
		}
	}
}