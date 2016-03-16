package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.actor.instances.player.TpBookMark;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;

/**
 * @author VISTALL
 * @date 17:03/08.08.2011
 */
public class BookMarkTeleport extends Skill
{
	public BookMarkTeleport(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer() || !super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first))
			return false;

		Player player = activeChar.getPlayer();

		TpBookMark loc = (TpBookMark)player.getVars().get(Player.TELEPORT_BOOKMARK);
		if(loc == null)
		{
			player.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}
		if(player.isActionBlocked(Zone.BLOCKED_ACTION_USE_BOOKMARK))
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			return false;
		}
		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}
		if(player.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		if(player.getReflection() != ReflectionManager.DEFAULT)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return false;
		}
		if(player.isInDuel())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		if(player.isOnSiegeField() || player.isInZoneBattle())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGESCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE_FORTRESS_SIEGE_OR_HIDEOUT_SIEGE);
			return false;
		}
		if(player.isFlying())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		if(player.isInWater() || player.isInBoat())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}

		if(first && !player.consumeItem(13016, 1))
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
			return false;
		}
		return true;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		Player player = activeChar.getPlayer();

		TpBookMark loc = (TpBookMark)player.getVars().remove(Player.TELEPORT_BOOKMARK);
		if(loc == null)
			return;

		player.teleToLocation(loc, ReflectionManager.DEFAULT);
	}
}
