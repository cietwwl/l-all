package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.residences.SiegeFlagInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;


public class Heal extends Skill
{
	private final boolean _ignoreHpEff;
	private final boolean _staticPower;

	public Heal(StatsSet set)
	{
		super(set);
		_ignoreHpEff = set.getBool("ignoreHpEff", false);
		_staticPower = set.getBool("staticPower", isHandler());
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target.isDoor() || target instanceof SiegeFlagInstance)
			return false;

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		double hp = getPower();

		if (!_staticPower)
		{
			final int mAtk = activeChar.getMAtk(null, skillEntry);
			int mAtkMod = 1;
			int staticBonus = 0;

			if (isSSPossible())
			{
				switch (activeChar.getChargedSpiritShot(false))
				{
				case ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					mAtkMod = 4;
					staticBonus = getStaticBonus(mAtk);
					break;
				case ItemInstance.CHARGED_SPIRITSHOT:
					mAtkMod = 2;
					staticBonus = getStaticBonus(mAtk) / 2;
					break;
				}
			}

			hp += Math.sqrt(mAtkMod * mAtk) + staticBonus;

			if(Formulas.calcMCrit(4.5)) // guess
				hp *= 3.; // TODO: DS: apply on all targets ?
		}

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isHealBlocked())
					continue;

				// Player holding a cursed weapon can't be healed and can't heal
				if(target != activeChar)
					if(target.isPlayer() && target.isCursedWeaponEquipped())
						continue;
					else if(activeChar.isPlayer() && activeChar.isCursedWeaponEquipped())
						continue;

				double addToHp;
				if(_staticPower)
					addToHp = _power;
				else
				{
					addToHp = hp;
					if (!isHandler())
					{
						addToHp += activeChar.calcStat(Stats.HEAL_POWER, activeChar, skillEntry); // static first
						addToHp *= (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100., activeChar, skillEntry) : 100.) / 100.; // percent second
					}
				}

				addToHp = Math.max(0, Math.min(addToHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

				if(addToHp > 0)
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				if(target.isPlayer())
					if(getId() == 4051)
						target.sendPacket(SystemMsg.REJUVENATING_HP);
					else if(activeChar == target)
						activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(Math.round(addToHp)));
					else
						target.sendPacket(new SystemMessage(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToHp)));
				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible() && isMagic())
			activeChar.unChargeShots(isMagic());
	}

	private final int getStaticBonus(int mAtk)
	{
		final double power = getPower();
		final double bottom = getPower() / 4.;
		if (mAtk < bottom)
			return 0;

		final double top = getPower() / 3.1;
		if (mAtk > getPower())
			return (int)top;

		mAtk -= bottom;
		return (int)(top * (mAtk / (power - bottom)));
	}
}