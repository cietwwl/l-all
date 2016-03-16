package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.templates.StatsSet;


public class DestroySummon extends Skill
{
	public DestroySummon(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{

				if(getActivateRate() > 0 && !Formulas.calcSkillSuccess(activeChar, target, skillEntry, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(target).addSkillName(getId(), getLevel()));
					continue;
				}

				if(target.isSummon())
				{
					((Servitor) target).saveEffects();
					((Servitor) target).unSummon(false, false);
					getEffects(skillEntry, activeChar, target, true, false);
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}