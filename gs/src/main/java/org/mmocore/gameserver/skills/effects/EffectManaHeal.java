package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.stats.Stats;

public class EffectManaHeal extends Effect
{
	private final boolean _ignoreMpEff;

	public EffectManaHeal(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(_effected.isHealBlocked())
			return;

		double mp = calc();
		double newMp = Math.min(mp * 1.7, mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., _effector, getSkill()) : 100.) / 100.);
		double addToMp = Math.max(0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100. - _effected.getCurrentMp()));

		if(_effected.isPlayer())
			if(_effector != _effected)
				_effected.sendPacket(new SystemMessage(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addName(_effector).addNumber(Math.round(addToMp)));
			else
				_effected.sendPacket(new SystemMessage(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addNumber(Math.round(addToMp)));

		if(addToMp > 0)
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}