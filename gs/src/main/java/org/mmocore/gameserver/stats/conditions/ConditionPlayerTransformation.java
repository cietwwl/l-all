package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerTransformation extends Condition
{
	private final int _transformation;

	public ConditionPlayerTransformation(int val)
	{
		_transformation = val;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return env.character.getPlayer().getTransformation() == _transformation;
	}
}