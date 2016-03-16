package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class TameControl extends Skill
{
	public static interface TameControlTarget
	{
		void doCastBuffs();

		void free();
	}

	private final int _type;

	public TameControl(StatsSet set)
	{
		super(set);
		_type = set.getInteger("type", 0);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());

		if(!activeChar.isPlayer())
			return;

		Player player = activeChar.getPlayer();
		switch(_type)
		{
			case 0:
				for(Creature target : targets)
					if(target instanceof TameControlTarget)
						((TameControlTarget) target).free();
				break;
			case 1:
				for(NpcInstance npc : player.getTamedBeasts())
					if(npc instanceof TameControlTarget)
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, Config.FOLLOW_RANGE);
				break;
			case 3:
				for(NpcInstance npc : player.getTamedBeasts())
					if(npc instanceof TameControlTarget)
						((TameControlTarget) npc).doCastBuffs();
				break;
			case 4:
				for(NpcInstance npc : player.getTamedBeasts())
					if(npc instanceof TameControlTarget)
						((TameControlTarget) npc).free();
				break;
		}
	}
}