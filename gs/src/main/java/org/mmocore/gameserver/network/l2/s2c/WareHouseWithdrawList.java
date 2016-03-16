package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.Warehouse.ItemClassComparator;
import org.mmocore.gameserver.model.items.Warehouse.WarehouseType;
import org.mmocore.gameserver.templates.item.ItemTemplate.ItemClass;


public class WareHouseWithdrawList extends L2GameServerPacket
{
	private final long _adena;
	private final ItemInstance[] _itemList;
	private final int _type;

	public WareHouseWithdrawList(Player player, WarehouseType type, ItemClass clss)
	{
		_adena = player.getAdena();
		_type = type.ordinal();

		switch(type)
		{
			case PRIVATE:
				_itemList = player.getWarehouse().getItems(clss);
				break;
			case FREIGHT:
				_itemList = player.getFreight().getItems(clss);
				break;
			case CLAN:
			case CASTLE:
				_itemList = player.getClan().getWarehouse().getItems(clss);
				break;
			default:
				_itemList = new ItemInstance[0];;
				return;
		}

		ArrayUtils.eqSort(_itemList, ItemClassComparator.getInstance());
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		writeH(_type);
		writeQ(_adena);
		writeH(_itemList.length);
		for(ItemInstance item : _itemList)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}