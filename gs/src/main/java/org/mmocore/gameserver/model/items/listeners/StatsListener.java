package org.mmocore.gameserver.model.items.listeners;

import org.mmocore.gameserver.listener.inventory.OnEquipListener;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.stats.funcs.Func;

public final class StatsListener implements OnEquipListener
{
	private static final StatsListener _instance = new StatsListener();

	public static StatsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		actor.removeStatsByOwner(item);
		actor.updateStats();
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		Func[] funcs = item.getStatFuncs();
		actor.addStatFuncs(funcs);
		actor.updateStats();
	}
}