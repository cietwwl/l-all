package org.mmocore.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.effects.EffectTemplate;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.EffectsComparator;

/**
 * @author pchayka
 */

public class StealBuff extends Skill
{
	private final int _stealCount;

	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("stealCount", 1);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isPlayer())
					continue;

				double baseChance = 50;
				int buffCount = 0;
				int stealCount = 0;
				int decrement = getId() == 1440 ? 5 : 50;
				List<Effect> effectsList = new ArrayList<Effect>(target.getEffectList().getAllEffects());
				Collections.sort(effectsList, EffectsComparator.getReverseInstance()); // ToFix: Comparator to HF
				for(Effect e : effectsList)
				{
					if(!canSteal(e))
						continue;

					if (Rnd.chance(baseChance))
					{
						Effect stolenEffect = cloneEffect(activeChar, e);
						if(stolenEffect != null)
							activeChar.getEffectList().addEffect(stolenEffect);
						e.exit();
						target.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getDisplayId(), e.getSkill().getDisplayLevel()));

						stealCount++;
						if (stealCount >= _stealCount)
							break;
					}

					buffCount++;
					if (buffCount >= _stealCount)
					{
						baseChance /= decrement;
						if (baseChance < 1)
							break;
					}
				}

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private static boolean canSteal(Effect e)
	{
		return e != null && e.isInUse() && e.isCancelable() && !e.getSkill().getTemplate().isToggle() && !e.getSkill().getTemplate().isPassive() && !e.getSkill().getTemplate().isOffensive() && e.getEffectType() != EffectType.Vitality && !e.getTemplate()._applyOnCaster;
	}

/*	private static boolean calcStealChance(Creature effected, Creature effector)
	{
		double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_RESIST, 1, null, null);
		int dml = effector.getLevel() - effected.getLevel();   // to check: magicLevel or player level? Since it's magic skill setting player level as default
		double prelimChance = (dml + 50) * (1 - cancel_res_multiplier * .01);   // 50 is random reasonable constant which gives ~50% chance of steal success while else is equal
		return Rnd.chance(prelimChance);
	}
*/
	private static Effect cloneEffect(Creature cha, Effect eff)
	{
		SkillEntry skill = eff.getSkill();

		for(EffectTemplate et : skill.getTemplate().getEffectTemplates())
		{
			Effect effect = et.getEffect(new Env(cha, cha, skill));
			if(effect != null)
			{
				effect.setCount(eff.getCount());
				effect.setPeriod(eff.getCount() == 1 ? eff.getPeriod() - eff.getTime() : eff.getPeriod());
				return effect;
			}
		}
		return null;
	}
}