package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import org.mmocore.gameserver.model.entity.events.objects.TerritoryWardObject;
import org.mmocore.gameserver.model.entity.residence.Dominion;
import org.mmocore.gameserver.model.instances.residences.SiegeFlagInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class TakeFlag extends Skill
{
	public TakeFlag(StatsSet set)
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

		if(player.getClan() == null)
			return false;

		DominionSiegeEvent siegeEvent1 = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent1 == null)
			return false;

		if(!(player.getActiveWeaponFlagAttachment() instanceof TerritoryWardObject))
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		if(!(target instanceof SiegeFlagInstance) || target.getNpcId() != 36590 || target.getClan() != player.getClan())
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		DominionSiegeEvent siegeEvent2 = target.getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null || siegeEvent1 != siegeEvent2)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
			return false;
		}

		return true;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				Player player = (Player) activeChar;
				DominionSiegeEvent siegeEvent1 = player.getEvent(DominionSiegeEvent.class);
				if(siegeEvent1 == null)
					continue;
				if(!(target instanceof SiegeFlagInstance) || target.getNpcId() != 36590 || target.getClan() != player.getClan())
					continue;
				if(!(player.getActiveWeaponFlagAttachment() instanceof TerritoryWardObject))
					continue;
				DominionSiegeEvent siegeEvent2 = target.getEvent(DominionSiegeEvent.class);
				if(siegeEvent2 == null || siegeEvent1 != siegeEvent2)
					continue;

				// текущая територия, к которой пойдет Вард
				Dominion dominion = siegeEvent1.getResidence();
				// вард с вражеской територии
				TerritoryWardObject wardObject = (TerritoryWardObject)player.getActiveWeaponFlagAttachment();
				// територия с которой уйдет Вард
				DominionSiegeEvent siegeEvent3 = wardObject.getEvent();
				Dominion dominion3 = siegeEvent3.getResidence();
				// айди територии к которой относится Вард
				int wardDominionId = wardObject.getDominionId();

				// удаляем с инвентарями вард, и освободжаем ресурсы
				wardObject.despawnObject(siegeEvent3);
				// удаляем Вард
				dominion3.removeFlag(wardDominionId);
				// добавляем Вард
				dominion.addFlag(wardDominionId);
				// позиции вардов с текущей територии
				// спавним Варда, уже в новой територии
				siegeEvent1.spawnAction("ward_" + wardDominionId, true);

				DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
				runnerEvent.broadcastTo(new SystemMessage(SystemMsg.CLAN_S1_HAS_SUCCEEDED_IN_CAPTURING_S2S_TERRITORY_WARD).addString(dominion.getOwner().getName()).addResidenceName(wardDominionId));
			}
	}
}