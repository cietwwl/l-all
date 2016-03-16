package org.mmocore.gameserver.skills.effects;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.stats.Env;

public class EffectInterrupt extends Effect
{
	public EffectInterrupt(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.isRaid() && Rnd.chance(_template._value))
			_effected.abortCast(true, true);
		else
			_effector.sendPacket(new SystemMessage(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(_effected).addSkillName(getSkill()));
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}