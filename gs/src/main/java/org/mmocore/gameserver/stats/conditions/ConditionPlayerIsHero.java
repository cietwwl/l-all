package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerIsHero extends Condition
{
	private final boolean _value;

	public ConditionPlayerIsHero(boolean value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;

		return ((Player)env.character).isHero() == _value;
	}
}
