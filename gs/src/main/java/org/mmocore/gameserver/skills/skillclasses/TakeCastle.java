package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.entity.events.impl.CastleSiegeEvent;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.GmListTable;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.Log;


public class TakeCastle extends Skill
{
	public TakeCastle(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first))
			return false;

		if(activeChar == null || !activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
		if(siegeEvent == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		CastleSiegeEvent siegeEvent2 = target.getEvent(CastleSiegeEvent.class);
		if(siegeEvent2 != siegeEvent)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, player.getClan()) == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(!player.isInRangeZ(target, 185) || player.getZDeltaSq(target.getZ()) > 2500)
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first)
		{
			siegeEvent.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, CastleSiegeEvent.DEFENDERS);
			String debug = "TakeCastle: caster: " + activeChar.getName() + ", loc:" + activeChar.getLoc() + ", castle: " + siegeEvent.getName() + ", target: " + target;

			Log.debug(debug);
			GmListTable.broadcastMessageToGMs(debug);
		}

		return true;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isArtefact() || !activeChar.isInRangeZ(target, 185) || activeChar.getZDeltaSq(target.getZ()) > 2500)
					continue;
				Player player = (Player) activeChar;

				CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
				if(siegeEvent != null)
				{
					siegeEvent.broadcastTo(new SystemMessage(SystemMsg.CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT).addString(player.getClan().getName()), CastleSiegeEvent.ATTACKERS, CastleSiegeEvent.DEFENDERS);
					siegeEvent.processStep(player.getClan());
				}
			}
	}
}