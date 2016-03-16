package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.stats.Env;

public class ConditionTargetCastleDoor extends Condition
{
	private final boolean _isCastleDoor;

	public ConditionTargetCastleDoor(boolean isCastleDoor)
	{
		_isCastleDoor = isCastleDoor;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.target instanceof DoorInstance == _isCastleDoor;
	}
}
