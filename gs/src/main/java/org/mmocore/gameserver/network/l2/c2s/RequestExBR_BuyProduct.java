package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.data.xml.holder.ProductHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.ProductItem;
import org.mmocore.gameserver.model.ProductItemComponent;
import org.mmocore.gameserver.network.l2.s2c.ExBR_BuyProduct;
import org.mmocore.gameserver.network.l2.s2c.ExBR_GamePoint;
import org.mmocore.gameserver.templates.item.ItemTemplate;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_count > 99 || _count < 0)
			return;

		ProductItem product = ProductHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		if((System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_SALE_PERIOD_ENDED));
			return;
		}

		int totalPoints = product.getPoints() * _count;

		if(totalPoints < 0)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		final long gamePointSize = activeChar.getPremiumPoints();

		if(totalPoints > gamePointSize)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		int totalWeight = 0;
		for(ProductItemComponent com : product.getComponents())
			totalWeight += com.getWeight();

		totalWeight *= _count; //увеличиваем вес согласно количеству

		int totalCount = 0;

		for(ProductItemComponent com : product.getComponents())
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getItemId());
			if(item == null)
			{
				activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
				return; //what
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}

		if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_INVENTORY_FULL));
			return;
		}

		activeChar.reducePremiumPoints(totalPoints);

		for(ProductItemComponent comp : product.getComponents())
			activeChar.getInventory().addItem(comp.getItemId(), comp.getCount() * _count);

		activeChar.sendPacket(new ExBR_GamePoint(activeChar));
		activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_OK));
		activeChar.sendChanges();
	}
}