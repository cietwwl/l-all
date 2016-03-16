package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.PcInventory;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.ExAttributeEnchantResult;
import org.mmocore.gameserver.network.l2.s2c.InventoryUpdate;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author SYS
 * Format: d
 */
public class RequestEnchantItemAttribute extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objectId == -1)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(SystemMsg.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, ActionFail.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance stone = activeChar.getEnchantScroll();
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || stone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemTemplate item = itemToEnchant.getTemplate();

		if(!itemToEnchant.canBeEnchanted(true) || item.getCrystalType().cry < ItemTemplate.CRYSTAL_S)
		{
			activeChar.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		Element element = ItemFunctions.getEnchantAttributeStoneElement(stone.getItemId(), itemToEnchant.isArmor());

		if(itemToEnchant.isArmor())
		{
			if(itemToEnchant.getAttributeElementValue(Element.getReverseElement(element), false) != 0)
			{
				activeChar.sendPacket(SystemMsg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
				return;
			}
		}
		else if(itemToEnchant.isWeapon())
		{
			if(itemToEnchant.getAttributeElement() != Element.NONE && itemToEnchant.getAttributeElement() != element)
			{
				activeChar.sendPacket(SystemMsg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
				return;
			}
		}
		else
		{
			activeChar.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || !item.isAttributable())
		{
			activeChar.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		int maxValue = itemToEnchant.isWeapon() ? Config.MAX_VALUE_ATTRIBUTE_WEAPON : Config.MAX_VALUE_ATTRIBUTE_ARMOR;

		final boolean isCrystal = stone.getTemplate().isAttributeCrystal();
		if(isCrystal)
			maxValue += itemToEnchant.isWeapon() ? Config.MAX_VALUE_ATTRIBUTE_WEAPON : Config.MAX_VALUE_ATTRIBUTE_ARMOR;

		if(itemToEnchant.getAttributeElementValue(element, false) >= maxValue)
		{
			activeChar.sendPacket(SystemMsg.ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED, ActionFail.STATIC);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(!inventory.destroyItem(stone, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}             
		int chance;
		if(itemToEnchant.getTemplate().isArmor())
			chance = isCrystal ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_ARMOR : Config.ENCHANT_ATTRIBUTE_ARMOR;
		else if(itemToEnchant.getTemplate().isMagicWeapon())
			chance = isCrystal ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_MAGIC_WEAPON : Config.ENCHANT_ATTRIBUTE_MAGIC_WEAPON;
		else
			chance = isCrystal ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_FIGHTER_WEAPON : Config.ENCHANT_ATTRIBUTE_FIGHTER_WEAPON;

		if (Config.ALT_DEBUG_ENCHANT_CHANCE_ENABLED && activeChar.isDebug())
			activeChar.sendMessage("Enchant chance : " + chance);

		if(Rnd.chance(chance))
		{
			if(itemToEnchant.getEnchantLevel() == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMsg.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
				sm.addItemName(itemToEnchant.getItemId());
				sm.addItemName(stone.getItemId());
				activeChar.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMsg.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1_S2);
				sm.addNumber(itemToEnchant.getEnchantLevel());
				sm.addItemName(itemToEnchant.getItemId());
				sm.addItemName(stone.getItemId());
				activeChar.sendPacket(sm);
			}

			int value = itemToEnchant.isWeapon() ? Config.VALUE_ATTRIBUTE_WEAPON : Config.VALUE_ATTRIBUTE_ARMOR;
			
			if(itemToEnchant.getAttributeElementValue(element, false) == 0 && itemToEnchant.isWeapon())
				value = Config.VALUE_FIRST_ENCHANT_ATTRIBUTE;

			boolean equipped = false;
			if(equipped = itemToEnchant.isEquipped())
			{
				activeChar.getInventory().isRefresh = true;
				activeChar.getInventory().unEquipItem(itemToEnchant);
			}

			itemToEnchant.setAttributeElement(element, itemToEnchant.getAttributeElementValue(element, false) + value);
			itemToEnchant.setJdbcState(JdbcEntityState.UPDATED);
			itemToEnchant.update();

			if(equipped)
			{
				activeChar.getInventory().equipItem(itemToEnchant);
				activeChar.getInventory().isRefresh = false;
			}

			activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
			activeChar.sendPacket(new ExAttributeEnchantResult(value));
		}
		else
			activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);

		activeChar.setEnchantScroll(null);
		activeChar.updateStats();
	}
}