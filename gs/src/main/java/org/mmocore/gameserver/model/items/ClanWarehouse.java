package org.mmocore.gameserver.model.items;

import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.items.ItemInstance.ItemLocation;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}

	@Override
	public ItemLocation getItemLocation()
	{
		return ItemLocation.CLANWH;
	}
}