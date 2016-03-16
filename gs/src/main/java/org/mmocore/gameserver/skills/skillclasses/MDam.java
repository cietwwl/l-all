package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.SummonInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.templates.StatsSet;

public class MDam extends Skill
{
	public MDam(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if (_targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE)
			if(target == null || !target.isDead() || !(target.isNpc() || target.isSummon()))
			{
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		int sps = 0;

		if (isSSPossible())
		{
			switch (activeChar.getChargedSpiritShot(false))
			{
			case ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
				sps = 2;
				break;
			case ItemInstance.CHARGED_SPIRITSHOT:
				sps = 1;
				break;
			}
		}

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				double damage = Formulas.calcMagicDam(activeChar, target, skillEntry, sps, false);
				if(damage >= 1)
					target.reduceCurrentHp(damage, activeChar, skillEntry, 0, false, true, true, false, true, false, false, true);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSuicideAttack())
			activeChar.doDie(null);
		else if(isSSPossible() && isMagic())
			activeChar.unChargeShots(isMagic());

		if (_targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE && targets.size() > 0) // TODO: DS: переделать
		{
			Creature corpse = targets.get(0);
			if (corpse != null && corpse.isDead())
			{
				if (corpse.isNpc())
					((NpcInstance) corpse).endDecayTask();
				else if (corpse.isSummon())
					((SummonInstance) corpse).endDecayTask();
				activeChar.getAI().setAttackTarget(null);
			}
		}
	}
}