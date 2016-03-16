package org.mmocore.gameserver.stats.conditions;

import java.lang.reflect.Field;

import org.mmocore.gameserver.stats.Env;

public class ConditionConfigBoolean extends Condition
{
	private final Field _config;
	private final boolean _value;

	public ConditionConfigBoolean(Field field, boolean value)
	{
		_config = field;
		_value = value;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		try
		{
			return _config.getBoolean(null) == _value;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}