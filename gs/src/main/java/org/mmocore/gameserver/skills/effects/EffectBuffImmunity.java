package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.stats.Env;

public final class EffectBuffImmunity extends Effect
{
	public EffectBuffImmunity(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startBuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopBuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
