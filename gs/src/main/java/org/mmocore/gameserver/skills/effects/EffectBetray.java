package org.mmocore.gameserver.skills.effects;

import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.stats.Env;

public class EffectBetray extends Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected != null && _effected.isSummon())
		{
			Servitor servitor = (Servitor) _effected;
			servitor.setDepressed(true);
			servitor.getAI().Attack(servitor.getPlayer(), true, false);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected != null && _effected.isSummon())
		{
			Servitor servitor = (Servitor) _effected;
			servitor.setDepressed(false);
			servitor.getAI().setIntention(AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}