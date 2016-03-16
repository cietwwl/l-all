package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerOlympiad extends Condition
{
	private final boolean _value;

	public ConditionPlayerOlympiad(boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		final Player player = env.character.getPlayer();
		if (player != null)
			return player.isInOlympiadMode() == _value;

		return !_value;
	}
}