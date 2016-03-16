package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class CPDam extends Skill
{
	public CPDam(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss)
			activeChar.unChargeShots(false);

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				target.doCounterAttack(skillEntry, activeChar, false);

				if(target.isCurrentCpZero())
					continue;

				double damage = _power * target.getCurrentCp();

				if(damage < 1)
					damage = 1;

				target.reduceCurrentHp(damage, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);

				getEffects(skillEntry, activeChar, target, true, false);
			}
	}
}