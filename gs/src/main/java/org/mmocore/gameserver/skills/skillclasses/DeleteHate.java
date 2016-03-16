package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class DeleteHate extends Skill
{
	public DeleteHate(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{

				if(target.isRaid())
					continue;

				if(getActivateRate() > 0)
				{
					if(activeChar.isPlayer() && ((Player) activeChar).isGM())
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance").addString(getName()).addNumber(getActivateRate()));

					if(!Rnd.chance(getActivateRate()))
						return;
				}

				if(target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().clear(false);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}

				getEffects(skillEntry, activeChar, target, false, false);
			}
	}
}
