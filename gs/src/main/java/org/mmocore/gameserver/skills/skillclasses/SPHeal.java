package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class SPHeal extends Skill
{
	public SPHeal(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				target.getPlayer().addExpAndSp(0, (long) _power);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
