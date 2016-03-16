package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerPercentMp extends Condition
{
	private final double _mp;

	public ConditionPlayerPercentMp(int mp)
	{
		_mp = mp / 100.;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentMpRatio() <= _mp;
	}
}