package org.mmocore.gameserver.listener.actor;

import org.mmocore.gameserver.listener.CharListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.skills.SkillEntry;

public interface OnMagicUseListener extends CharListener
{
	public void onMagicUse(Creature actor, SkillEntry skill, Creature target, boolean alt);
}
