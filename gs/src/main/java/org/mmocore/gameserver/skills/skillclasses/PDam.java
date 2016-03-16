package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Formulas.AttackInfo;
import org.mmocore.gameserver.templates.StatsSet;


public class PDam extends Skill
{
	private final boolean _onCrit;
	private final boolean _directHp;
	private final boolean _blow;

	public PDam(StatsSet set)
	{
		super(set);
		_onCrit = set.getBool("onCrit", false);
		_directHp = set.getBool("directHp", false);
		_blow = set.getBool("blow", false);
	}

	@Override
	public boolean isBlowSkill()
	{
		return _blow;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();

		boolean isHit = false;

		for(Creature target : targets)
			if(target != null && !target.isDead())
			{
				AttackInfo info = Formulas.calcPhysDam(activeChar, target, skillEntry, false, _blow, ss, _onCrit);
				isHit |= !info.miss;

				if (info.lethal_dmg > 0)
					target.reduceCurrentHp(info.lethal_dmg, activeChar, skillEntry, 0, info.crit, true, true, false, false, false, false, false);

				if(!info.miss || info.damage >= 1)
					target.reduceCurrentHp(info.damage, activeChar, skillEntry, 0, info.crit, true, true, !info.lethal && _directHp, true, false, false, getPower(target) != 0);
				target.doCounterAttack(skillEntry, activeChar, _blow);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSuicideAttack())
			activeChar.doDie(null);
		else if(isHit && isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}