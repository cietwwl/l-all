package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.stats.Env;

public class EffectLDManaDamOverTime extends Effect
{
	private double _manaDam = 0;

	public EffectLDManaDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		_manaDam = calc() / 2.5;

		super.onStart();
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		double manaDam = _manaDam * _effected.getLevel();

		if(manaDam > _effected.getCurrentMp() && getSkill().getTemplate().isToggle())
		{
			_effected.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			_effected.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}