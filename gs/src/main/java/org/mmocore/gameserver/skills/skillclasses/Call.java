package org.mmocore.gameserver.skills.skillclasses;

import static org.mmocore.gameserver.model.Zone.ZoneType.no_restart;
import static org.mmocore.gameserver.model.Zone.ZoneType.no_summon;

import java.util.List;

import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class Call extends Skill
{
	final boolean _party;
	final int _targetItemConsumeId;
	final int _targetItemConsumeCount;

	public Call(StatsSet set)
	{
		super(set);
		_party = set.getBool("party", false);
		_targetItemConsumeId = set.getInteger("targetItemConsumeId", 0);
		_targetItemConsumeCount = set.getInteger("targetItemConsumeCount", 0);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayer())
		{
			if(_party && ((Player) activeChar).getParty() == null)
				return false;

			IBroadcastPacket msg = canSummonHere((Player)activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}

			// Эта проверка только для одиночной цели
			if(!_party)
			{
				if(activeChar == target)
					return false;

				msg = canBeSummoned(target);
				if(msg != null)
				{
					activeChar.sendPacket(msg);
					return false;
				}
			}
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		IBroadcastPacket msg = canSummonHere((Player)activeChar);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return;
		}

		if(_party)
		{
			if(((Player) activeChar).getParty() != null)
				for(Player target : ((Player) activeChar).getParty().getPartyMembers())
					if(!target.equals(activeChar) && canBeSummoned(target) == null && !target.isTerritoryFlagEquipped())
					{
						target.stopMove();
						target.teleToLocation(activeChar.getLoc(), activeChar.getGeoIndex());
						getEffects(skillEntry, activeChar, target, true, false);
					}

			if(isSSPossible())
				activeChar.unChargeShots(isMagic());
			return;
		}

		for(Creature target : targets)
			if(target != null)
			{
				if(canBeSummoned(target) != null)
					continue;

				((Player) target).summonCharacterRequest((Player)activeChar, _targetItemConsumeId, _targetItemConsumeCount);

				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	/**
	 * Может ли призывающий в данный момент использовать призыв
	 */
	public static IBroadcastPacket canSummonHere(Player activeChar)
	{
		if (activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.isOutOfControl() || activeChar.isFlying() || activeChar.isFestivalParticipant())
			return SystemMsg.NOTHING_HAPPENED;

		// "Нельзя вызывать персонажей в/из зоны свободного PvP"
		// "в зоны осад"
		// "на Олимпийский стадион"
		// "в зоны определенных рейд-боссов и эпик-боссов"
		if(activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(no_restart) || activeChar.isInZone(no_summon) || activeChar.isInBoat() || activeChar.getReflection() != ReflectionManager.DEFAULT)
			return SystemMsg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;

		//if(activeChar.isInCombat())
		//return SystemMsg.YOU_CANNOT_SUMMON_DURING_COMBAT;

		if(activeChar.isInStoreMode() || activeChar.isInTrade())
			return SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE;

		return null;
	}

	/**
	 * Может ли цель ответить на призыв
	 */
	public static IBroadcastPacket canBeSummoned(Creature target)
	{
		if(target == null || !target.isPlayer() || target.getPlayer().isTerritoryFlagEquipped() || target.isFlying() || target.isOutOfControl() || target.getPlayer().isFestivalParticipant() || target.getPlayer().isTeleportBlocked())
			return SystemMsg.INVALID_TARGET;

		if(((Player)target).isInOlympiadMode())
			return SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;

		if(target.isInZoneBattle() || target.isInZone(Zone.ZoneType.SIEGE) || target.isInZone(no_restart) || target.isInZone(no_summon) || target.getReflection() != ReflectionManager.DEFAULT || target.isInBoat())
			return SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;

		// Нельзя призывать мертвых персонажей
		if(target.isAlikeDead())
			return new SystemMessage(SystemMsg.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addName(target);

		// Нельзя призывать персонажей, которые находятся в Combat Mode
		if(target.isInCombat())
			return new SystemMessage(SystemMsg.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addName(target);

		Player pTarget = (Player) target;

		// Нельзя призывать торгующих персонажей
		if(pTarget.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || pTarget.isProcessingRequest())
			return new SystemMessage(SystemMsg.C1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addName(target);

		return null;
	}
}