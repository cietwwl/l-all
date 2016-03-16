package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.data.xml.holder.ProductHolder;
import org.mmocore.gameserver.model.ProductItem;
import org.mmocore.gameserver.model.ProductItemComponent;

public class ExBR_ProductInfo extends L2GameServerPacket
{
	private ProductItem _productId;

	public ExBR_ProductInfo(int id)
	{
		_productId = ProductHolder.getInstance().getProduct(id);
	}  	

	@Override
	protected void writeImpl()
	{
		if(_productId == null)
			return;

		writeEx(0xD7);

		writeD(_productId.getProductId());  //product id
		writeD(_productId.getPoints());	  // points
		writeD(_productId.getComponents().size());	   //size

		for(ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getItemId());   //item id
			writeD(com.getCount());  //quality
			writeD(com.getWeight()); //weight
			writeD(com.isDropable() ? 1 : 0); //0 - dont drop/trade
		}
	}
}