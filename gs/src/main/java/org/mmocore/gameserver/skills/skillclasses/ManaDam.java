package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.templates.StatsSet;


public class ManaDam extends Skill
{
	public ManaDam(StatsSet set)
	{
		super(set);
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

				double damage = Formulas.calcMagicDam(activeChar, target, skillEntry, sps, true);
				if(damage >= 1)
					target.reduceCurrentMp(damage, activeChar);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible() && isMagic())
			activeChar.unChargeShots(isMagic());
	}
}