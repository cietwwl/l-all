package org.mmocore.gameserver.stats.conditions;

import gnu.trove.TIntHashSet;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.stats.Env;

public class ConditionTargetForbiddenClassId extends Condition
{
	private TIntHashSet _classIds = new TIntHashSet();

	public ConditionTargetForbiddenClassId(String[] ids)
	{
		for(String id : ids)
			_classIds.add(Integer.parseInt(id));
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(!target.isPlayable())
			return false;
		return !target.isPlayer() || !_classIds.contains(target.getPlayer().getActiveClassId());
	}
}