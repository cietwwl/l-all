package org.mmocore.gameserver.model.entity.events.impl;

import java.util.List;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.events.objects.DuelSnapshotObject;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExDuelAskStart;
import org.mmocore.gameserver.network.l2.s2c.ExDuelEnd;
import org.mmocore.gameserver.network.l2.s2c.ExDuelReady;
import org.mmocore.gameserver.network.l2.s2c.SocialAction;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * @author VISTALL
 * @date 3:26/29.06.2011
 */
public class PlayerVsPlayerDuelEvent extends DuelEvent
{
	public PlayerVsPlayerDuelEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected PlayerVsPlayerDuelEvent(int id, String name)
	{
		super(id, name);
	}

	@Override
	public boolean canDuel(Player player, Player target, boolean first)
	{
		IBroadcastPacket sm = canDuel0(player, target, false);
		if(sm != null)
		{
			player.sendPacket(sm);
			return false;
		}

		sm = canDuel0(target, player, false);
		if(sm != null)
		{
			player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}

		return true;
	}

	@Override
	public void askDuel(Player player, Player target, int arenaId)
	{
		Request request = new Request(Request.L2RequestType.DUEL, player, target).setTimeout(10000L);
		request.set("duelType", 0);
		player.setRequest(request);
		target.setRequest(request);

		player.sendPacket(new SystemMessage(SystemMsg.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addName(target));
		target.sendPacket(new SystemMessage(SystemMsg.C1_HAS_CHALLENGED_YOU_TO_A_DUEL).addName(player), new ExDuelAskStart(player.getName(), 0));
	}

	@Override
	public void createDuel(Player player, Player target, int arenaId)
	{
		PlayerVsPlayerDuelEvent duelEvent = new PlayerVsPlayerDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
		cloneTo(duelEvent);

		duelEvent.addObject(TeamType.BLUE, new DuelSnapshotObject(player, TeamType.BLUE, true));
		player.addEvent(duelEvent);
		duelEvent.addObject(TeamType.RED, new DuelSnapshotObject(target, TeamType.RED, true));
		target.addEvent(duelEvent);
		duelEvent.sendPacket(new ExDuelReady(this));
		duelEvent.reCalcNextTime(false);
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

				sendPacket(new SystemMessage(SystemMsg.C1_HAS_WON_THE_DUEL).addName(winners.get(0).getPlayer()));

				for(DuelSnapshotObject d : lossers)
					d.getPlayer().broadcastPacket(new SocialAction(d.getPlayer().getObjectId(), SocialAction.BOW));
				break;
		}

		removeObjects(TeamType.RED);
		removeObjects(TeamType.BLUE);
	}

	@Override
	public void onDie(Player player)
	{
		TeamType team = player.getTeam();
		if(team == TeamType.NONE || _aborted)
			return;

		playerLost(player);
	}

	@Override
	public int getDuelType()
	{
		return 0;
	}

	@Override
	public void packetSurrender(Player player)
	{
		playerLost(player);
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 5000L;
	}
}
