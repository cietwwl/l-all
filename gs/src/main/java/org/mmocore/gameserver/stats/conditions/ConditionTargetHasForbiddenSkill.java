package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.stats.Env;

public final class ConditionTargetHasForbiddenSkill extends Condition
{
	private final int _skillId;

	public ConditionTargetHasForbiddenSkill(int skillId)
	{
		_skillId = skillId;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(!target.isPlayable())
			return false;
		return !(target.getSkillLevel(_skillId) > 0);
	}
}
