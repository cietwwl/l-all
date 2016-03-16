package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;


public class Continuous extends Skill
{
	private final int _lethal1;
	private final int _lethal2;

	public Continuous(StatsSet set)
	{
		super(set);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				// Player holding a cursed weapon can't be buffed and can't buff
				if(getSkillType() == Skill.SkillType.BUFF && target != activeChar)
					if(target.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped())
						continue;

				double mult = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, activeChar, skillEntry);
				double lethal1 = _lethal1 * mult;
				double lethal2 = _lethal2 * mult;

				if(lethal1 > 0 && Rnd.chance(lethal1))
				{
					if(target.isPlayer())
					{
						target.reduceCurrentHp(target.getCurrentCp(), activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(target.isNpc() && !target.isLethalImmune())
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
				}
				else if(lethal2 > 0 && Rnd.chance(lethal2))
					if(target.isPlayer())
					{
						target.reduceCurrentHp(target.getCurrentHp() + target.getCurrentCp() - 1, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(target.isNpc() && !target.isLethalImmune())
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
			{
				SkillEntry castingSkill = activeChar.getCastingSkill();
				if (castingSkill != null && castingSkill.getTemplate() == this) // не разряжаем шоты для альтернативного каста
					activeChar.unChargeShots(isMagic());
			}
	}
}