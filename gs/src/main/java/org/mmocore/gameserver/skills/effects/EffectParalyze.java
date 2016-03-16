package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.stats.Env;

public final class EffectParalyze extends Effect
{
	private final boolean _postEffectOnly; // псевдопаралич для Anchor-подобных скиллов, не парализует, но может быть снят Purify-ем.

	public EffectParalyze(Env env, EffectTemplate template)
	{
		super(env, template);
		_postEffectOnly = template.getParam().getBool("postEffectOnly", false);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (!_postEffectOnly)
		{
			_effected.startParalyzed();
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			if (_effected.isMoving)
				_effected.stopMove();
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if (!_postEffectOnly)
			_effected.stopParalyzed();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}