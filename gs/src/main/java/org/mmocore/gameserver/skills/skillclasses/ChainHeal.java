package org.mmocore.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.commons.collections.CollectionUtils;
import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.StatsSet;

public class ChainHeal extends Skill
{
	private final double[] _healPercents;
	private final int _maxTargets;

	public ChainHeal(StatsSet set)
	{
		super(set);
		String[] params = set.getString("healPercents", "").split(";");
		_maxTargets = params.length;
		_healPercents = new double[params.length];
		for(int i = 0; i < params.length; i++)
			_healPercents[i] = Integer.parseInt(params[i]) / 100.;
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayable() && target.isMonster())
			return false;
		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		int curTarget = 0;
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || target.isHealBlocked())
				continue;

			getEffects(skillEntry, activeChar, target, true, false);

			double hp = _healPercents[curTarget] * target.getMaxHp();
			double addToHp = Math.max(0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

			if(addToHp > 0)
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);

			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToHp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(Math.round(addToHp)));

			curTarget++;
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>(_maxTargets);

		// Добавляем, если это возможно, текущую цель
		if (!aimingTarget.isDead() && !aimingTarget.isHealBlocked())
			result.add(aimingTarget);

		List<Creature> targets = aimingTarget.getAroundCharacters(getSkillRadius(), 128);
		if(targets == null || targets.isEmpty())
			return result;

		LazyArrayList<HealTarget> healList = LazyArrayList.newInstance();
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || target.isHealBlocked())
				continue;
			// DS: протестировать всегда ли захватывает кастера
			if (activeChar.getObjectId() != aimingTarget.getObjectId() && target.getObjectId() == activeChar.getObjectId())
				continue;
			if(target.isAutoAttackable(activeChar))
				continue;
			if(target.isInvisible())
				continue;
			healList.add(new HealTarget(target));
		}

		if (healList.isEmpty())
		{
			LazyArrayList.recycle(healList);
			return result;
		}

		CollectionUtils.shellSort(healList);
		final int size = Math.min(_maxTargets - result.size(), healList.size()); // возможно текущая цель уже добавлена
		for (int i = 0; i < size; i++)
			result.add(i, healList.get(i).target); // сдвигаем текущую цель в конец если есть

		LazyArrayList.recycle(healList);
		return result;
	}

	private static final class HealTarget implements Comparable<HealTarget>
	{
		private final double hpPercent;
		public final Creature target;

		public HealTarget(Creature target)
		{
			this.target = target;
			this.hpPercent = target.getCurrentHpPercents();
		}

		@Override
		public int compareTo(HealTarget ht)
		{
			if(hpPercent < ht.hpPercent)
				return -1;
			if(hpPercent > ht.hpPercent)
				return 1;
			return 0;
		}
	}
}