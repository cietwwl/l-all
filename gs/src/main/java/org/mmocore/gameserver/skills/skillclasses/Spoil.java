package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.MonsterInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Formulas.AttackInfo;
import org.mmocore.gameserver.templates.StatsSet;


public class Spoil extends Skill
{
	private final boolean _onCrit;
	private final boolean _blow;

	public Spoil(StatsSet set)
	{
		super(set);
		_onCrit = set.getBool("onCrit", false);
		_blow = set.getBool("blow", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		int ss = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot(false) : activeChar.getChargedSoulShot() ? 2 : 0) : 0;
		if(ss > 0 && getPower() > 0)
			activeChar.unChargeShots(false);

		for(Creature target : targets)
			if(target != null && !target.isDead())
			{
				// TODO: DS: вынести в эффект
				if(target.isMonster())
					if(((MonsterInstance) target).isSpoiled())
						activeChar.sendPacket(SystemMsg.IT_HAS_ALREADY_BEEN_SPOILED);
					else
					{
						MonsterInstance monster = (MonsterInstance) target;
						boolean success;

						int monsterLevel = monster.getLevel();
						int modifier = monsterLevel - getMagicLevel();
						double rateOfSpoil = Math.max(getActivateRate(), 80);

						if(modifier > 8)
							rateOfSpoil -= rateOfSpoil * (modifier - 8) * 9 / 100;

						rateOfSpoil *= (double) getMagicLevel() / monsterLevel;
						rateOfSpoil = Math.max(Config.MINIMUM_SPOIL_RATE, Math.min(rateOfSpoil, 99));

						success = Rnd.chance(rateOfSpoil);
						if(success && monster.setSpoiled((Player) activeChar))
							activeChar.sendPacket(SystemMsg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
					}

				if(getPower() > 0)
				{
					double damage;
					if(isMagic())
					{
						damage = Formulas.calcMagicDam(activeChar, target, skillEntry, ss, false);
						if (damage >= 1)
							target.reduceCurrentHp(damage, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);						
					}
					else
					{
						AttackInfo info = Formulas.calcPhysDam(activeChar, target, skillEntry, false, _blow, ss > 0, _onCrit);
						damage = info.damage;

						if(info.lethal_dmg > 0)
							target.reduceCurrentHp(info.lethal_dmg, activeChar, skillEntry, 0, info.crit, true, true, false, false, false, false, false);
						if(!info.miss || info.damage >= 1)
							target.reduceCurrentHp(damage, activeChar, skillEntry, 0, info.crit, true, true, false, true, false, false, true);
						target.doCounterAttack(skillEntry, activeChar, _blow);
					}
				}

				getEffects(skillEntry, activeChar, target, false, false); // DS: почему calcChance false ?

				target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Math.max(_effectPoint, 1));
			}
	}
}