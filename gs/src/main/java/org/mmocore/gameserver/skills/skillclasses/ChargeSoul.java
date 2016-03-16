package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Formulas.AttackInfo;
import org.mmocore.gameserver.templates.StatsSet;


public class ChargeSoul extends Skill
{
	private int _numSouls;

	public ChargeSoul(StatsSet set)
	{
		super(set);
		_numSouls = set.getInteger("numSouls", getLevel());
	}

	// TODO: DS: вынести в эффект
	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss && getTargetType() != SkillTargetType.TARGET_SELF)
			activeChar.unChargeShots(false);

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				if(getPower() > 0) // Если == 0 значит скилл "отключен"
				{
					AttackInfo info = Formulas.calcPhysDam(activeChar, target, skillEntry, false, false, ss, false);

					if (info.lethal_dmg > 0)
						target.reduceCurrentHp(info.lethal_dmg, activeChar, skillEntry, 0, info.crit, true, true, false, false, false, false, false);

					target.reduceCurrentHp(info.damage, activeChar, skillEntry, 0, info.crit, true, true, false, true, false, false, true);
					target.doCounterAttack(skillEntry, activeChar, false);
				}

				if(target.isPlayable() || target.isNpc())
					activeChar.setConsumedSouls(activeChar.getConsumedSouls() + _numSouls, null);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}