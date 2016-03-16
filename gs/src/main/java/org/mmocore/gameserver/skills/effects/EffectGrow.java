package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.stats.Env;

public final class EffectGrow extends Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCollisionHeight(npc.getCollisionHeight() * 1.24);
			npc.setCollisionRadius(npc.getCollisionRadius() * 1.19);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCollisionHeight(npc.getTemplate().collisionHeight);
			npc.setCollisionRadius(npc.getTemplate().collisionRadius);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}