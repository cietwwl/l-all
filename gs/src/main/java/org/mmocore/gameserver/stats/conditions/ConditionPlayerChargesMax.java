package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerChargesMax extends Condition
{
	private final int _maxCharges;

	public ConditionPlayerChargesMax(int maxCharges)
	{
		_maxCharges = maxCharges;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if (env.character == null || !env.character.isPlayer())
			return false;

		if (((Player)env.character).getIncreasedForce() >= _maxCharges)
		{
			env.character.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
			return false;
		}
		return true;
	}
}