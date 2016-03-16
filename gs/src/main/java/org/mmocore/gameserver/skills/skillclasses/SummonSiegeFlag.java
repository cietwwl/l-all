package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.entity.events.objects.SiegeClanObject;
import org.mmocore.gameserver.model.entity.events.objects.ZoneObject;
import org.mmocore.gameserver.model.instances.residences.SiegeFlagInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;

public class SummonSiegeFlag extends Skill
{
	public static enum FlagType
	{
		DESTROY,
		NORMAL,
		ADVANCED,
		OUTPOST
	}

	private final FlagType _flagType;

	public SummonSiegeFlag(StatsSet set)
	{
		super(set);
		_flagType = set.getEnum("flagType", FlagType.class);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;
		if(!super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first))
			return false;

		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
			return false;

		switch(_flagType)
		{
			case DESTROY:
				//
				break;
			case OUTPOST:
			case NORMAL:
			case ADVANCED:
				if(player.isInZone(Zone.ZoneType.RESIDENCE))
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
					return false;
				}

				//SiegeEvent<?,?> siegeEvent = activeChar.getEvent(_flagType == FlagType.OUTPOST ? DominionSiegeEvent.class : SiegeEvent.class);
                                SiegeEvent<?,?> siegeEvent = null;
                                if (_flagType == FlagType.OUTPOST) 
                                {
                                    siegeEvent = activeChar.getEvent(DominionSiegeEvent.class);
                                }
                                else
                                {
                                    siegeEvent = activeChar.getEvent(SiegeEvent.class);
                                }
                                
				if(siegeEvent == null || !siegeEvent.isInProgress())
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
					return false;
				}

				boolean inZone = false;
				List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
				for(ZoneObject zone : zones)
				{
					if(player.isInZone(zone.getZone()))
						inZone = true;
				}

				if(!inZone)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
					return false;
				}

				SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? SiegeEvent.DEFENDERS : SiegeEvent.ATTACKERS, player.getClan());
				if(siegeClan == null)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
					return false;
				}

				if(siegeClan.getFlag() != null)
				{
					player.sendPacket(SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skillEntry));
					return false;
				}
				break;
		}
		return true;
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		Player player = (Player) activeChar;

		Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
			return;

		//SiegeEvent<?,?> siegeEvent = activeChar.getEvent(_flagType == FlagType.OUTPOST ? DominionSiegeEvent.class : SiegeEvent.class);
                SiegeEvent<?,?> siegeEvent = null;
                if (_flagType == FlagType.OUTPOST) 
                {
                    siegeEvent = activeChar.getEvent(DominionSiegeEvent.class);
                }
                else
                {
                    siegeEvent = activeChar.getEvent(SiegeEvent.class);
                }
		if(siegeEvent == null || !siegeEvent.isInProgress())
			return;

		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? SiegeEvent.DEFENDERS : SiegeEvent.ATTACKERS, clan);
		if(siegeClan == null)
			return;

		switch(_flagType)
		{
			case DESTROY:
				siegeClan.deleteFlag();
				break;
			default:
				if(siegeClan.getFlag() != null)
					return;

				// 35062/36590
				SiegeFlagInstance flag = (SiegeFlagInstance)NpcHolder.getInstance().getTemplate(_flagType == FlagType.OUTPOST ? 36590 : 35062).getNewInstance();
				flag.setClan(siegeClan);
				flag.addEvent(siegeEvent);

				if(_flagType == FlagType.ADVANCED)
					flag.setAdvanced();

				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
				flag.setHeading(player.getHeading());

				// Ставим флаг перед чаром
				int x = (int) (player.getX() + 100 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
				int y = (int) (player.getY() + 100 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
				flag.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex()));

				siegeClan.setFlag(flag);
				break;
		}
	}
}