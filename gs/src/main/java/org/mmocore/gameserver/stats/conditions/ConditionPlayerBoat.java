package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerBoat extends Condition
{
	private final boolean _value;

	public ConditionPlayerBoat(boolean val)
	{
		_value = val;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.isInBoat() == _value;
	}
}