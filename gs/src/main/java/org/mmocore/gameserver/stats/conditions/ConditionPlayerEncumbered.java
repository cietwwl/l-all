package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerEncumbered extends Condition
{
	private final int _maxWeightPercent;
	private final int _maxLoadPercent;

	public ConditionPlayerEncumbered(int remainingWeightPercent, int remainingLoadPercent)
	{
		_maxWeightPercent = 100 - remainingWeightPercent;
		_maxLoadPercent = 100 - remainingLoadPercent;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if (env.character == null || !env.character.isPlayer())
			return false;

		if (((Player)env.character).getWeightPercents() >= _maxWeightPercent || ((Player)env.character).getUsedInventoryPercents() >= _maxLoadPercent)
		{
			env.character.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return false;
		}

		return true;
	}
}