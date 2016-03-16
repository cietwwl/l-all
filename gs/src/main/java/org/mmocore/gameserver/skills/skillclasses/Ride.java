package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class Ride extends Skill
{
	public Ride(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(getNpcId() != 0)
		{
			if(player.isInDuel() || player.isSitting() || player.isInCombat() || player.isFishing() || player.isCursedWeaponEquipped() || player.getTransformation() != 0 || player.getServitor() != null || player.isMounted() || player.isInBoat())
				return false;
		}
		else if(getNpcId() == 0 && !player.isMounted())
			return false;

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature caster, List<Creature> targets)
	{
		if(!caster.isPlayer())
			return;

		Player activeChar = (Player) caster;
		activeChar.setMount(getNpcId(), 0, 0, -1);
	}
}