package org.mmocore.gameserver.model.entity.events.impl;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.dao.SiegeClanDAO;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.objects.SiegeClanObject;
import org.mmocore.gameserver.model.entity.residence.ClanHall;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.PlaySound;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * @author VISTALL
 * @date 15:23/14.02.2011
 */
public class ClanHallSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
	public static final String BOSS = "boss";

	public ClanHallSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		if(getObjects(ATTACKERS).size() == 0)
		{
			broadcastInZone2(new SystemMessage(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			reCalcNextTime(false);
			return;
		}

		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			getResidence().changeOwner(null);

			addObject(ATTACKERS, new SiegeClanObject(ATTACKERS, _oldOwner, 0));
		}

		SiegeClanDAO.getInstance().delete(getResidence());

		updateParticles(true, ATTACKERS);

		broadcastTo(new SystemMessage(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), ATTACKERS);

		super.startEvent();
	}

	@Override
	public void stopEvent(boolean step)
	{
		Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);

			newOwner.incReputation(1700, false, toString());

			broadcastTo(new SystemMessage(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()), ATTACKERS);
			broadcastTo(new SystemMessage(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), ATTACKERS);
		}
		else
			broadcastTo(new SystemMessage(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), ATTACKERS);

		updateParticles(false, ATTACKERS);

		removeObjects(ATTACKERS);

		super.stopEvent(step);

		_oldOwner = null;
	}

	@Override
	public void removeState(int val)
	{
		super.removeState(val);

		if(val == REGISTRATION_STATE)
			broadcastTo(new SystemMessage(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()), ATTACKERS);
	}

	@Override
	public void processStep(Clan clan)
	{
		if(clan != null)
			getResidence().changeOwner(clan);

		stopEvent(true);
	}

	@Override
	public void loadSiegeClans()
	{
		addObjects(ATTACKERS, SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS));
	}

	@Override
	public int getUserRelation(Player thisPlayer, int result)
	{
		return result;
	}

	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		return result;
	}

	@Override
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		boolean playerInZone = checkIfInZone(active);
		boolean targetInZone = checkIfInZone(target);
		// если оба вне зоны - рес разрешен
		// если таргет вне осадный зоны - рес разрешен
		if(!playerInZone && !targetInZone || !targetInZone)
			return true;

		Player resurectPlayer = active.getPlayer();
		Player targetPlayer = target.getPlayer();

		// если оба незареганы - невозможно ресать
		// если таргет незареган - невозможно ресать
		ClanHallSiegeEvent siegeEvent1 = resurectPlayer.getEvent(ClanHallSiegeEvent.class);
		ClanHallSiegeEvent siegeEvent2 = targetPlayer.getEvent(ClanHallSiegeEvent.class);
		if(siegeEvent1 == null && siegeEvent2 == null || siegeEvent2 != this)
		{
			if(!quiet)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
				active.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
			}
			return false;
		}

		SiegeClanObject targetSiegeClan = siegeEvent2.getSiegeClan(ATTACKERS, targetPlayer.getClan());

		// если нету флага - рес запрещен
		if(targetSiegeClan == null || targetSiegeClan.getFlag() == null)
		{
			if(!quiet)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				active.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
			}
			return false;
		}

		if(force)
			return true;
		else
		{
			if(!quiet)
				active.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
	}
}
