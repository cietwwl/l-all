package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExVariationCancelResult;
import org.mmocore.gameserver.network.l2.s2c.InventoryUpdate;
import org.mmocore.gameserver.network.l2.s2c.ShortCutRegister;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;
import org.mmocore.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestRefineCancel extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestRefineCancel.class);

	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if(NpcUtils.canPassPacket(activeChar, this) == null)
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

		// cannot remove augmentation from a not augmented item
		if(targetItem == null || !targetItem.isAugmented())
		{
			activeChar.sendPacket(ExVariationCancelResult.FAIL, SystemMsg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		final int mineralId = targetItem.getAugmentationMineralId();
		if (mineralId > 0) // DS: генератор аугментации создает предмет с mineralId = -1
		{
			final AugmentationInfo augmentationInfo = targetItem.getTemplate().getAugmentationInfos().get(mineralId);
			if(augmentationInfo == null)
				_log.warn("Player: " + activeChar  + ", cancel item with mineral: " + mineralId + " item: " + targetItem);
			else if(!activeChar.reduceAdena(augmentationInfo.getCancelFee(), true))
			{
				activeChar.sendPacket(ExVariationCancelResult.FAIL, SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}

		boolean equipped = false;
		if(equipped = targetItem.isEquipped())
			activeChar.getInventory().unEquipItem(targetItem);

		// remove the augmentation
		targetItem.setAugmentation(0, ItemInstance.EMPTY_AUGMENTATIONS);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();

		if(equipped)
			activeChar.getInventory().equipItem(targetItem);

		activeChar.sendPacket(ExVariationCancelResult.SUCCESS, new InventoryUpdate().addModifiedItem(targetItem), new SystemMessage(SystemMsg.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1).addItemName(targetItem.getItemId()));

		for(ShortCut sc : activeChar.getAllShortCuts())
			if(sc.getId() == targetItem.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM)
				activeChar.sendPacket(new ShortCutRegister(activeChar, sc));
		activeChar.sendChanges();
	}
}