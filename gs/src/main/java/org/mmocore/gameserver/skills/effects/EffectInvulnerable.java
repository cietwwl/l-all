package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Skill.SkillType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;

public final class EffectInvulnerable extends Effect
{
	private final boolean _blockBuff;
	private final boolean _blockDebuff;

	public EffectInvulnerable(Env env, EffectTemplate template)
	{
		super(env, template);
		_blockBuff = template.getParam().getBool("blockBuff", false);
		_blockDebuff = template.getParam().getBool("blockDebuff", true);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvul())
			return false;
		SkillEntry skill = _effected.getCastingSkill();
		if(skill != null && (skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG))
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startHealBlocked();
		_effected.startDamageBlocked();
		if (_blockBuff)
			_effected.startBuffImmunity();
		if (_blockDebuff)
			_effected.startDebuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopHealBlocked();
		_effected.stopDamageBlocked();
		if (_blockBuff)
			_effected.stopBuffImmunity();
		if (_blockDebuff)
			_effected.stopDebuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}