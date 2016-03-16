package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExVariationResult;
import org.mmocore.gameserver.network.l2.s2c.InventoryUpdate;
import org.mmocore.gameserver.network.l2.s2c.ShortCutRegister;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;
import org.mmocore.gameserver.utils.NpcUtils;
import org.napile.primitive.maps.IntObjectMap;

public final class RequestRefine extends L2GameClientPacket
{
	private int _targetItemObjId, _refinerItemObjId, _gemstoneItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		readQ();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(NpcUtils.canPassPacket(player, this) == null)
		{
			player.sendPacket(ExVariationResult.CLOSE);
			return;
		}

		if(player.isActionsDisabled())
		{
			player.sendPacket(ExVariationResult.CLOSE);
			return;
		}

		if(player.isInStoreMode())
		{
			player.sendPacket(ExVariationResult.CLOSE);
			return;
		}

		if(player.isInTrade())
		{
			player.sendPacket(ExVariationResult.CLOSE);
			return;
		}

		ItemInstance targetItem = player.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = player.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance gemstoneItem = player.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null || player.getLevel() < 46 || targetItem.getTemplate().getAugmentationInfos().isEmpty())
		{
			player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		IntObjectMap<AugmentationInfo> augmentationInfos = targetItem.getTemplate().getAugmentationInfos();

		AugmentationInfo augmentationInfo = augmentationInfos.get(refinerItem.getItemId());
		if(augmentationInfo == null || gemstoneItem.getCount() < augmentationInfo.getFeeItemCount() || gemstoneItem.getItemId() != augmentationInfo.getFeeItemId())
	{
			player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
			}

		int[] options = augmentationInfo.randomOption(targetItem.getTemplate());
		if(options == null)
		{
			player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		if(!player.getInventory().destroyItemByObjectId(_gemstoneItemObjId, augmentationInfo.getFeeItemCount()))
			return;

		if(!player.getInventory().destroyItemByObjectId(_refinerItemObjId, 1L))
			return;

		boolean equipped = false;
		if(equipped = targetItem.isEquipped())
			player.getInventory().unEquipItem(targetItem);

		targetItem.setAugmentation(augmentationInfo.getMineralId(), options);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();

		if(equipped)
			player.getInventory().equipItem(targetItem);

		player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));

		for(ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == targetItem.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM)
				player.sendPacket(new ShortCutRegister(player, sc));
		player.sendChanges();

		player.sendPacket(new ExVariationResult(options), SystemMsg.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
	}
}