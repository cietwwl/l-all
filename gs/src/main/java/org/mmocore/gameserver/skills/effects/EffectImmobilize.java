package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.stats.Env;

public final class EffectImmobilize extends Effect
{
	public EffectImmobilize(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startImmobilized();
		_effected.stopMove();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopImmobilized();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
