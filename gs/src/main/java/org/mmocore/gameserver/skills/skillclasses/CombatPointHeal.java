package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;


public class CombatPointHeal extends Skill
{
	private final boolean _ignoreCpEff;

	public CombatPointHeal(StatsSet set)
	{
		super(set);
		_ignoreCpEff = set.getBool("ignoreCpEff", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked())
					continue;
				double maxNewCp = _power * (!_ignoreCpEff ? target.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100., activeChar, skillEntry) : 100.) / 100.;
				double addToCp = Math.max(0, Math.min(maxNewCp, target.calcStat(Stats.CP_LIMIT, null, null) * target.getMaxCp() / 100. - target.getCurrentCp()));
				if(addToCp > 0)
					target.setCurrentCp(addToCp + target.getCurrentCp());
				if (activeChar == target)
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addNumber(Math.round(addToCp)));
				else
					target.sendPacket(new SystemMessage(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToCp)));
				getEffects(skillEntry, activeChar, target, true, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
