package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerChargesMin extends Condition
{
	private final int _minCharges;

	public ConditionPlayerChargesMin(int minCharges)
	{
		_minCharges = minCharges;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if (env.character == null || !env.character.isPlayer())
			return false;

		return ((Player)env.character).getIncreasedForce() >= _minCharges;
	}
}