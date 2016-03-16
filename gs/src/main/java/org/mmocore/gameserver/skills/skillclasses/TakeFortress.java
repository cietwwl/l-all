package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.entity.events.impl.CastleSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.FortressSiegeEvent;
import org.mmocore.gameserver.model.entity.events.objects.FortressCombatFlagObject;
import org.mmocore.gameserver.model.entity.events.objects.StaticObjectObject;
import org.mmocore.gameserver.model.instances.StaticObjectInstance;
import org.mmocore.gameserver.model.items.attachment.ItemAttachment;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class TakeFortress extends Skill
{
	public TakeFortress(StatsSet set)
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

		GameObject flagPole = activeChar.getTarget();
		if(!(flagPole instanceof StaticObjectInstance) || ((StaticObjectInstance) flagPole).getType() != 3)
		{
			activeChar.sendPacket(SystemMsg.THE_TARGET_IS_NOT_A_FLAGPOLE_SO_A_FLAG_CANNOT_BE_DISPLAYED);
			return false;
		}

		if(first)
		{
			List<Creature> around = World.getAroundCharacters(flagPole, getSkillRadius() * 2, 100);
			for(Creature ch : around)
			{
				if(ch.isCastingNow() && ch.getCastingSkill() == skillEntry) // проверяел ли ктото возле нас кастует накойже скил
				{
					activeChar.sendPacket(SystemMsg.A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED);
					return false;
				}
			}
		}

		Player player = (Player) activeChar;
		if(player.getClan() == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		FortressSiegeEvent siegeEvent = player.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		ItemAttachment attach = player.getActiveWeaponFlagAttachment();
		if(!(attach instanceof FortressCombatFlagObject) || ((FortressCombatFlagObject) attach).getEvent() != siegeEvent)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(!player.isInRangeZ(flagPole, getCastRange()))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first)
			siegeEvent.broadcastTo(new SystemMessage(SystemMsg.S1_CLAN_IS_TRYING_TO_DISPLAY_A_FLAG).addString(player.getClan().getName()), CastleSiegeEvent.DEFENDERS);

		return true;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		GameObject flagPole = activeChar.getTarget();
		if(!(flagPole instanceof StaticObjectInstance) || ((StaticObjectInstance) flagPole).getType() != 3)
			return;
		Player player = (Player) activeChar;
		FortressSiegeEvent siegeEvent = player.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
			return;

		StaticObjectObject object = siegeEvent.getFirstObject(FortressSiegeEvent.FLAG_POLE);
		if(((StaticObjectInstance) flagPole).getUId() != object.getUId())
			return;

		siegeEvent.processStep(player.getClan());
	}
}