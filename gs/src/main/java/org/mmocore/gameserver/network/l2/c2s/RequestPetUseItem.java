package org.mmocore.gameserver.network.l2.c2s;


import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.PetInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.ItemFunctions;

public class RequestPetUseItem extends L2GameClientPacket
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

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
			return;
		}

		activeChar.setActive();

		if(activeChar.getServitor() == null || !activeChar.getServitor().isPet())
			return;

		PetInstance pet = (PetInstance) activeChar.getServitor();
		ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if(item == null || item.getCount() < 1)
			return;

		if(activeChar.isAlikeDead() || activeChar.isFakeDeath() || pet.isDead() || pet.isOutOfControl() || !item.getTemplate().testCondition(pet, item, true))
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return;
		}

		if(item.getTemplate().isPetFood() || ArrayUtils.contains(Config.ALT_ALLOWED_PET_POTIONS, item.getItemId()))
		{
			item.getTemplate().getHandler().useItem(pet, item, false);
			return;
		}

		// [pchayka] Letting uneqip an item without checks since pet inventory is shared for all pets
		IBroadcastPacket sm = null;
		if(item.isEquipped())
			pet.getInventory().unEquipItem(item);
		else
		{
			sm = ItemFunctions.checkIfCanEquip(pet, item);
			if(sm == null)
				pet.getInventory().equipItem(item);
		}
		pet.broadcastCharInfo();
		if(sm != null)
			activeChar.sendPacket(sm);
	}
}