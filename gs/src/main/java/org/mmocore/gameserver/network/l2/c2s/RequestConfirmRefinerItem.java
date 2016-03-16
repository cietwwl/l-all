package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExPutIntensiveResultForVariationMake;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;
import org.napile.primitive.maps.IntObjectMap;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);

		if(targetItem == null || refinerItem == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		IntObjectMap<AugmentationInfo> augmentationInfos = targetItem.getTemplate().getAugmentationInfos();

		int refinerItemId = refinerItem.getTemplate().getItemId();

		AugmentationInfo augmentationInfo = null;
		if(augmentationInfos.isEmpty() || (augmentationInfo = augmentationInfos.get(refinerItemId)) == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		SystemMessage sm = new SystemMessage(SystemMsg.REQUIRES_S2_S1).addNumber(augmentationInfo.getFeeItemCount()).addItemName(augmentationInfo.getFeeItemId());
		activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, augmentationInfo.getFeeItemId(), augmentationInfo.getFeeItemCount()), sm);
		}
}