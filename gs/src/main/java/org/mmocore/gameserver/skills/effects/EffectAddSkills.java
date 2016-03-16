package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Skill.AddedSkill;
import org.mmocore.gameserver.stats.Env;

public class EffectAddSkills extends Effect
{
	public EffectAddSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		for(AddedSkill as : getSkill().getTemplate().getAddedSkills())
			getEffected().addSkill(as.getSkill());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		for(AddedSkill as : getSkill().getTemplate().getAddedSkills())
			getEffected().removeSkill(as.getSkill());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}