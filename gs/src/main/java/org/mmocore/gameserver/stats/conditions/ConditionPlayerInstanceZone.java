package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 */
public class ConditionPlayerInstanceZone extends Condition
{
	private final int _id;

	public ConditionPlayerInstanceZone(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Reflection ref = env.character.getReflection();

		return ref.getInstancedZoneId() == _id;
	}
}