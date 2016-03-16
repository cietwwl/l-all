package org.mmocore.gameserver.listener.actor;

import org.mmocore.gameserver.listener.CharListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.skills.SkillEntry;

public interface OnMagicHitListener extends CharListener
{
	public void onMagicHit(Creature actor, SkillEntry skill, Creature caster);
}
