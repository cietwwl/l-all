package org.mmocore.gameserver.skills.skillclasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.templates.StatsSet;


public class NegateEffects extends Skill
{
	private Map<EffectType, Integer> _negateEffects = new HashMap<EffectType, Integer>();
	private Map<String, Integer> _negateStackType = new HashMap<String, Integer>();
	private final boolean _onlyPhysical;
	private final boolean _negateDebuffs;
	private final boolean _force; // удалять "неудаляемые" (cancelable = false) эффекты

	public NegateEffects(StatsSet set)
	{
		super(set);

		String[] negateEffectsString = set.getString("negateEffects", "").split(";");
		for(int i = 0; i < negateEffectsString.length; i++)
			if(!negateEffectsString[i].isEmpty())
			{
				String[] entry = negateEffectsString[i].split(":");
				_negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}

		String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
		for(int i = 0; i < negateStackTypeString.length; i++)
			if(!negateStackTypeString[i].isEmpty())
			{
				String[] entry = negateStackTypeString[i].split(":");
				_negateStackType.put(entry[0], entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}

		_onlyPhysical = set.getBool("onlyPhysical", false);
		_negateDebuffs = set.getBool("negateDebuffs", true);
		_force = set.getBool("force", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, skillEntry, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(target).addSkillName(getId(), getLevel()));
					continue;
				}

				if(!_negateEffects.isEmpty())
					for(Map.Entry<EffectType, Integer> e : _negateEffects.entrySet())
						negateEffectAtPower(target, e.getKey(), e.getValue());

				if(!_negateStackType.isEmpty())
					for(Map.Entry<String, Integer> e : _negateStackType.entrySet())
						negateEffectAtPower(target, e.getKey(), e.getValue());

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private void negateEffectAtPower(Creature target, EffectType type, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			SkillEntry skill = e.getSkill();
			if((_onlyPhysical && skill.getTemplate().isMagic()) || (!_force && !skill.getTemplate().isCancelable()) || (skill.getTemplate().isOffensive() && !_negateDebuffs))
				continue;
			// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
			if(!skill.getTemplate().isOffensive() && skill.getTemplate().getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getTemplate().getMagicLevel() - getMagicLevel()))
				continue;
			if(e.getEffectType() == type && e.getStackOrder() <= power)
			{
				e.exit();
			}
		}
	}

	private void negateEffectAtPower(Creature target, String stackType, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			SkillEntry skill = e.getSkill();
			if((_onlyPhysical && skill.getTemplate().isMagic()) || (!_force && !skill.getTemplate().isCancelable()) || (skill.getTemplate().isOffensive() && !_negateDebuffs))
				continue;
			// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
			if(!skill.getTemplate().isOffensive() && skill.getTemplate().getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getTemplate().getMagicLevel() - getMagicLevel()))
				continue;
			if(e.checkStackType(stackType) && e.getStackOrder() <= power)
			{
				e.exit();
				if (target.isPlayer())
					target.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getDisplayId(), e.getSkill().getDisplayLevel()));				
			}
		}
	}
}