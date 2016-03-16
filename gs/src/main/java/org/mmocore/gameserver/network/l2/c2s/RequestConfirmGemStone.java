package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExPutCommissionResultForVariationMake;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;
import org.napile.primitive.maps.IntObjectMap;

public class RequestConfirmGemStone extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private long _gemstoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readQ();
	}

	@Override
	protected void runImpl()
	{
		if(_gemstoneCount <= 0)
			return;

		Player activeChar = getClient().getActiveChar();
		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null || targetItem.getTemplate().getAugmentationInfos().isEmpty())
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		IntObjectMap<AugmentationInfo> augmentationInfos = targetItem.getTemplate().getAugmentationInfos();

		AugmentationInfo augmentationInfo = null;
		if((augmentationInfo = augmentationInfos.get(refinerItem.getItemId())) == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(augmentationInfo.getFeeItemId() != gemstoneItem.getItemId() || gemstoneItem.getCount() < augmentationInfo.getFeeItemCount() || augmentationInfo.getFeeItemCount() != _gemstoneCount)
		{
			activeChar.sendPacket(SystemMsg.GEMSTONE_QUANTITY_IS_INCORRECT);
			return;
		}

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_gemstoneItemObjId, _gemstoneCount), SystemMsg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
					}
}