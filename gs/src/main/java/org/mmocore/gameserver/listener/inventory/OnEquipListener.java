package org.mmocore.gameserver.listener.inventory;

import org.mmocore.commons.listener.Listener;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	public void onEquip(int slot, ItemInstance item, Playable actor);

	public void onUnequip(int slot, ItemInstance item, Playable actor);
}
