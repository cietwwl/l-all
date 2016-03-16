package org.mmocore.gameserver.network.l2.c2s;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mmocore.commons.math.SafeMath;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.TradeItem;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.PrivateStoreManageListBuy;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.TradeHelper;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
	private int _count;
	private int[] _items; // item id
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 40 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();

			readH();
			readH();

			_itemQ[i] = readQ();
			_itemP[i] = readQ();

			if(_itemQ[i] < 1 || _itemP[i] < 1)
			{
				_count = 0;
				break;
			}

			// TODO Gracia Final
			readC(); // FE
			readD(); // FF 00 00 00
			readD(); // 00 00 00 00

			readC(); // Unknown 7 bytes
			readC();
			readC();
			readC();
			readC();
			readC();
			readC();
		}
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null || _count == 0)
			return;

		if(!TradeHelper.checksIfCanOpenStore(buyer, Player.STORE_PRIVATE_BUY))
		{
			buyer.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = new CopyOnWriteArrayList<TradeItem>();
		long totalCost = 0;
		int slots = 0;
		long weight = 0;
		try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);

				if(item == null || itemId == ItemTemplate.ITEM_ID_ADENA)
					continue;

				if(item.isStackable())
					for(TradeItem bi : buyList)
						if(bi.getItemId() == itemId)
						{
							bi.setOwnersPrice(price);
							bi.setCount(bi.getCount() + count);
							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							continue loop;
						}

				TradeItem bi = new TradeItem();
				bi.setItemId(itemId);
				bi.setCount(count);
				bi.setOwnersPrice(price);
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getWeight()));
				if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
					slots++;
				buyList.add(bi);
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			buyer.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		if(buyList.size() > buyer.getTradeLimit())
		{
			buyer.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
			return;
		}

		if(totalCost > buyer.getAdena())
		{
			buyer.sendPacket(SystemMsg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
			return;
		}

		if(!buyer.getInventory().validateWeight(weight) || !buyer.getInventory().validateCapacity(slots))
		{
			buyer.sendPacket(SystemMsg.THE_WEIGHT_AND_VOLUME_LIMIT_OF_YOUR_INVENTORY_CANNOT_BE_EXCEEDED);
			buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
			return;
		}

		if(!buyList.isEmpty())
		{
			buyer.setPrivateStoreStartTime(System.currentTimeMillis());
			buyer.setBuyList(buyList);
			buyer.saveTradeList();
			buyer.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
			buyer.broadcastPrivateStoreInfo();
			buyer.sitDown(null);
			buyer.broadcastCharInfo();
		}

		buyer.sendActionFailed();
	}
}