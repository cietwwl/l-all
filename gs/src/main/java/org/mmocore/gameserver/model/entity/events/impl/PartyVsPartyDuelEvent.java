package org.mmocore.gameserver.model.entity.events.impl;

import java.util.Iterator;
import java.util.List;

import org.mmocore.commons.collections.CollectionUtils;
import org.mmocore.commons.collections.JoinedIterator;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.data.xml.holder.InstantZoneHolder;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.events.objects.DuelSnapshotObject;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExDuelAskStart;
import org.mmocore.gameserver.network.l2.s2c.ExDuelEnd;
import org.mmocore.gameserver.network.l2.s2c.ExDuelReady;
import org.mmocore.gameserver.network.l2.s2c.SocialAction;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.InstantZone;

/**
 * @author VISTALL
 * @date 3:22/29.06.2011
 */
public class PartyVsPartyDuelEvent extends DuelEvent
{
	public PartyVsPartyDuelEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected PartyVsPartyDuelEvent(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void stopEvent()
	{
		if(!_isInProgress)
			return;
		_isInProgress = false;

		clearActions();

		updatePlayers(false, false);

		for(DuelSnapshotObject d : this)
		{
			d.blockUnblock();

			d.getPlayer().sendPacket(new ExDuelEnd(this));
			GameObject target = d.getPlayer().getTarget();
			if(target != null)
				d.getPlayer().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);
		}

		switch(_winner)
		{
			case NONE:
				sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
			case RED:
			case BLUE:
				List<DuelSnapshotObject> winners = getObjects(_winner);
				List<DuelSnapshotObject> lossers = getObjects(_winner.revert());

				DuelSnapshotObject winner = CollectionUtils.safeGet(winners, 0);
				if(winner != null)
				{
					sendPacket(new SystemMessage(SystemMsg.C1S_PARTY_HAS_WON_THE_DUEL).addName(winners.get(0).getPlayer()));

					for(DuelSnapshotObject d : lossers)
						d.getPlayer().broadcastPacket(new SocialAction(d.getPlayer().getObjectId(), SocialAction.BOW));
				}
				else
					sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
		}

		updatePlayers(false, true);
		removeObjects(TeamType.RED);
		removeObjects(TeamType.BLUE);
	}

	@Override
	public void teleportPlayers(String name)
	{
		InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(1);

		Reflection reflection = new Reflection();
		reflection.init(instantZone);

		List<DuelSnapshotObject> team = getObjects(TeamType.BLUE);

		for(int i = 0; i < team.size(); i++)
		{
			DuelSnapshotObject $member = team.get(i);

			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(i), reflection);
		}

		team = getObjects(TeamType.RED);

		for(int i = 0; i < team.size(); i++)
		{
			DuelSnapshotObject $member = team.get(i);

			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(9 + i), reflection);
		}
	}

	@Override
	public boolean canDuel(Player player, Player target, boolean first)
	{
		if(player.getParty() == null)
		{
			player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}

		if(target.getParty() == null)
		{
			player.sendPacket(SystemMsg.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
			return false;
		}

		Party party1 = player.getParty();
		Party party2 = target.getParty();
		if(player != party1.getPartyLeader() || target != party2.getPartyLeader())
		{
			player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}

		Iterator<Player> iterator = new JoinedIterator<Player>(party1.iterator(), party2.iterator());
		while(iterator.hasNext())
		{
			Player $member = iterator.next();

			IBroadcastPacket packet = null;
			if((packet = canDuel0(player, $member, false)) != null)
			{
				player.sendPacket(packet);
				target.sendPacket(packet);
				return false;
			}
		}
		return true;
	}

	@Override
	public void askDuel(Player player, Player target, int arenaId)
	{
		Request request = new Request(Request.L2RequestType.DUEL, player, target).setTimeout(10000L);
		request.set("duelType", 1);
		player.setRequest(request);
		target.setRequest(request);

		player.sendPacket(new SystemMessage(SystemMsg.C1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL).addName(target));
		target.sendPacket(new SystemMessage(SystemMsg.C1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL).addName(player), new ExDuelAskStart(player.getName(), 1));
	}

	@Override
	public void createDuel(Player player, Player target, int arenaId)
	{
		PartyVsPartyDuelEvent duelEvent = new PartyVsPartyDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
		cloneTo(duelEvent);

		for(Player $member : player.getParty())
		{
			duelEvent.addObject(TeamType.BLUE, new DuelSnapshotObject($member, TeamType.BLUE, true));
			$member.addEvent(duelEvent);
		}

		for(Player $member : target.getParty())
		{
			duelEvent.addObject(TeamType.RED, new DuelSnapshotObject($member, TeamType.RED, true));
			$member.addEvent(duelEvent);			
		}

		duelEvent.sendPacket(new ExDuelReady(this));
		duelEvent.reCalcNextTime(false);
	}

	@Override
	public void packetSurrender(Player player)
	{
		//
	}

	@Override
	public void onDie(Player player)
	{
		TeamType team = player.getTeam();
		if(team == TeamType.NONE || _aborted)
			return;

		sendPacket(SystemMsg.THE_OTHER_PARTY_IS_FROZEN, team.revert());

		player.stopAttackStanceTask();
		player.startFrozen();
		player.setTeam(TeamType.NONE);

		for(Player $player : World.getAroundPlayers(player))
		{
			$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player);
			if(player.getServitor() != null)
				$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player.getServitor());
		}
		player.sendChanges();

		playerLost(player);
	}

	@Override
	public int getDuelType()
	{
		return 1;
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}
