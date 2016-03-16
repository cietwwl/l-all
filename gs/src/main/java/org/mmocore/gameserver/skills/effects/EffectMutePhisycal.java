package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;

public class EffectMutePhisycal extends Effect
{
	public EffectMutePhisycal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.startPMuted())
		{
			SkillEntry castingSkill = _effected.getCastingSkill();
			if(castingSkill != null && !castingSkill.getTemplate().isMagic())
				_effected.abortCast(true, true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopPMuted();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}