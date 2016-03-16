package org.mmocore.gameserver.stats;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.skills.SkillEntry;

/**
 *
 * An Env object is just a class to pass parameters to a calculator such as L2Player,
 * L2ItemInstance, Initial value.
 *
 */
public final class Env
{
	public Creature character;
	public Creature target;
	public ItemInstance item;
	public SkillEntry skill;
	public double value;

	public Env()
	{}

	public Env(Creature cha, Creature tar, SkillEntry sk)
	{
		character = cha;
		target = tar;
		skill = sk;
	}
}
