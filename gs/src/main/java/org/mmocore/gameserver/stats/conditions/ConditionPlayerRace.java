package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.Race;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerRace extends Condition
{
	private final Race _race;

	public ConditionPlayerRace(String race)
	{
		_race = Race.valueOf(race.toLowerCase());
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return ((Player) env.character).getRace() == _race;
	}
}