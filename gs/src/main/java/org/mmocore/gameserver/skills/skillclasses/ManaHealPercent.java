package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;


public class ManaHealPercent extends Skill
{
	private final boolean _ignoreMpEff;

	public ManaHealPercent(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", true);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked())
					continue;

				getEffects(skillEntry, activeChar, target, true, false);

				double mp = _power * target.getMaxMp() / 100.;
				double newMp = mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., activeChar, skillEntry) : 100.) / 100.;
				double addToMp = Math.max(0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

				if(addToMp > 0)
					target.setCurrentMp(target.getCurrentMp() + addToMp);
				if(target.isPlayer())
					if(activeChar != target)
						target.sendPacket(new SystemMessage(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToMp)));
					else
						activeChar.sendPacket(new SystemMessage(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addNumber(Math.round(addToMp)));
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}