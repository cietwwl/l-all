package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.Location;


public class PetSummon extends Skill
{
	public PetSummon(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(player == null)
			return false;

		if(player.getPetControlItem() == null)
			return false;

		int npcId = PetDataTable.getSummonId(player.getPetControlItem());
		if(npcId == 0)
			return false;

		if(player.isInCombat())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return false;
		}

		if(player.isProcessingRequest())
		{
			player.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		if(player.isMounted() || player.getServitor() != null)
		{
			player.sendPacket(SystemMsg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}

		if(player.isInBoat())
		{
			player.sendPacket(SystemMsg.YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION);
			return false;
		}

		if(player.isInFlyingTransform())
			return false;

		if(player.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_MATCH);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME);
			return false;
		}

		for(GameObject o : World.getAroundObjects(player, 120, 200))
			if(o.isDoor())
			{
				player.sendPacket(SystemMsg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();

		activeChar.summonPet(activeChar.getPetControlItem(), Location.findPointToStay(activeChar, 50, 70));

		if(isSSPossible())
			caster.unChargeShots(isMagic());
	}
}