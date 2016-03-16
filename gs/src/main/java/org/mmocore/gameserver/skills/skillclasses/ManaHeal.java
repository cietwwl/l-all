package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;

public class ManaHeal extends Skill
{
	private final boolean _ignoreMpEff;

	public ManaHeal(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target.isHealBlocked())
				continue;

			double mp = _power;
			if (activeChar != target && !_ignoreMpEff)
				mp = Math.min(mp * 1.7, mp + target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 0, activeChar, skillEntry));

			if(getMagicLevel() > 0 && activeChar != target)
			{
				int diff = target.getLevel() - getMagicLevel() - 5;
				if(diff > 0)
					if(diff < 10)
						mp -= 0.1* mp * diff;
					else
						mp = 0;
			}

			double addToMp = Math.max(0, Math.min(mp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

			if(addToMp > 0)
				target.setCurrentMp(addToMp + target.getCurrentMp());

			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToMp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addNumber(Math.round(addToMp)));

			getEffects(skillEntry, activeChar, target, true, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}