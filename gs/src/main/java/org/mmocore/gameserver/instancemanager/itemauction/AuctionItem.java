package org.mmocore.gameserver.instancemanager.itemauction;

import org.mmocore.gameserver.model.items.ItemInfo;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author n0nam3
 */
public final class AuctionItem extends ItemInfo
{
	private final int _auctionItemId;
	private final int _auctionLength;
	private final long _auctionInitBid;

	public AuctionItem(final int auctionItemId, final int auctionLength, final long auctionInitBid, final int itemId, final long itemCount, final StatsSet itemExtra)
	{
		_auctionItemId = auctionItemId;
		_auctionLength = auctionLength;
		_auctionInitBid = auctionInitBid;

		setObjectId(itemId);
		setItemId(itemId);
		setCount(itemCount);
		setEnchantLevel(itemExtra.getInteger("enchant_level", 0));
	}

	public final int getAuctionItemId()
	{
		return _auctionItemId;
	}

	public final int getAuctionLength()
	{
		return _auctionLength;
	}

	public final long getAuctionInitBid()
	{
		return _auctionInitBid;
	}

	public final ItemInstance createNewItemInstance()
	{
		final ItemInstance item = ItemFunctions.createItem(getItemId());
		item.setEnchantLevel(getEnchantLevel());
		return item;
	}
}