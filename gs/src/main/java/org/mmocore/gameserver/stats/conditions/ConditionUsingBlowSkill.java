package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.stats.Env;

public class ConditionUsingBlowSkill extends Condition
{
	private final boolean _flag;

	public ConditionUsingBlowSkill(boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.skill == null)
			return !_flag;
		else
			return env.skill.getTemplate().isBlowSkill() == _flag;
	}
}
