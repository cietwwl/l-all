package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;

/**
 * @author VISTALL
 */
public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
	private int _itemObjectId;
	private int _itemId;
	private int[] _augmentations = ItemInstance.EMPTY_AUGMENTATIONS;
	private long _price;

	public ExPutItemResultForVariationCancel(ItemInstance item)
	{
		_itemObjectId = item.getObjectId();
		_itemId = item.getItemId();
		_augmentations = item.getAugmentations();
		AugmentationInfo augmentationInfo = item.getTemplate().getAugmentationInfos().get(item.getAugmentationMineralId());
		if(augmentationInfo != null)
			_price = augmentationInfo.getCancelFee();
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x57);
		writeD(_itemObjectId);
		writeD(_itemId);
		writeD(_augmentations[0]);
		writeD(_augmentations[1]);
		writeQ(_price);
		writeD(0x01);
	}
}