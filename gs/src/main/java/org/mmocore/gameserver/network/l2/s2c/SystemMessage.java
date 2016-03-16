package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;

/**
 * @author VISTALL
 * @date 14:45/08.03.2011
 */
public class SystemMessage extends SysMsgContainer<SystemMessage>
{
	public SystemMessage(SystemMsg message)
	{
		super(message);
	}

	public static SystemMessage obtainItems(int itemId, long count, int enchantLevel)
	{
		if(itemId == 57)
			return new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1_ADENA).addNumber(count);
		if(count > 1)
			return new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S2_S1S).addItemName(itemId).addNumber(count);
		if(enchantLevel > 0)
			return new SystemMessage(SystemMsg.YOU_HAVE_OBTAINED_A_S1_S2).addNumber(enchantLevel).addItemName(itemId);
		return new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1).addItemName(itemId);
	}

	public static SystemMessage obtainItems(ItemInstance item)
	{
		return obtainItems(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0);
	}

	public static SystemMessage obtainItemsBy(int itemId, long count, int enchantLevel, Creature target)
	{
		if(count > 1)
			return new SystemMessage(SystemMsg.C1_HAS_OBTAINED_S3_S2).addName(target).addItemName(itemId).addNumber(count);
		if(enchantLevel > 0)
			return new SystemMessage(SystemMsg.C1_HAS_OBTAINED_S2S3).addName(target).addNumber(enchantLevel).addItemName(itemId);
		return new SystemMessage(SystemMsg.C1_HAS_OBTAINED_S2).addName(target).addItemName(itemId);
	}

	public static SystemMessage obtainItemsBy(ItemInstance item, Creature target)
	{
		return obtainItemsBy(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0, target);
	}

	public static SystemMessage removeItems(int itemId, long count)
	{
		if(itemId == 57)
			return new SystemMessage(SystemMsg.S1_ADENA_DISAPPEARED).addNumber(count);
		if(count > 1)
			return new SystemMessage(SystemMsg.S2_S1_HAS_DISAPPEARED).addItemName(itemId).addNumber(count);
		return new SystemMessage(SystemMsg.S1_HAS_DISAPPEARED).addItemName(itemId);
	}

	public static SystemMessage removeItems(ItemInstance item)
	{
		return removeItems(item.getItemId(), item.getCount());
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x62);
		writeElements();
	}
}
