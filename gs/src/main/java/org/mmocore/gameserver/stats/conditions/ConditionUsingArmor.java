package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.templates.item.ArmorTemplate.ArmorType;

public class ConditionUsingArmor extends Condition
{
	private final ArmorType _armor;

	public ConditionUsingArmor(ArmorType armor)
	{
		_armor = armor;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.character.isPlayer() && ((Player) env.character).isWearingArmor(_armor))
			return true;

		return false;
	}
}
