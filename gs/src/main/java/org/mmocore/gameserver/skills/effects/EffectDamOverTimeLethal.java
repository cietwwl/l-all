package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.stats.Stats;

public class EffectDamOverTimeLethal extends Effect
{
	public EffectDamOverTimeLethal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		double damage = calc();

		if(getSkill().getTemplate().isOffensive())
			damage *= 2;

		damage = _effector.calcStat(getSkill().getTemplate().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());

		_effected.reduceCurrentHp(damage, _effector, getSkill(), 0, false, !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);

		return true;
	}
}