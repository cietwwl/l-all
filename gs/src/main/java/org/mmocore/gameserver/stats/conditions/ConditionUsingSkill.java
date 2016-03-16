package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 20:57/12.04.2011
 */
public class ConditionUsingSkill extends Condition
{
	private int _id;

	public ConditionUsingSkill(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.skill == null)
			return false;
		else
			return env.skill.getId() == _id;
	}
}
