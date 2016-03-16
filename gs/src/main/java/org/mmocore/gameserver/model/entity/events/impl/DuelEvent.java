package org.mmocore.gameserver.model.entity.events.impl;

import java.util.Iterator;
import java.util.List;

import org.mmocore.commons.collections.JoinedIterator;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.listener.actor.player.OnPlayerExitListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.objects.DuelSnapshotObject;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExDuelStart;
import org.mmocore.gameserver.network.l2.s2c.ExDuelUpdateUserInfo;
import org.mmocore.gameserver.network.l2.s2c.PlaySound;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * @author VISTALL
 * @date 2:10/26.06.2011
 */
public abstract class DuelEvent extends AbstractDuelEvent implements Iterable<DuelSnapshotObject>
{
	private class OnPlayerExitListenerImpl extends OnDeathFromUndyingListenerImpl implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			playerLost(player);
		}
	}

	protected OnPlayerExitListener _playerExitListener = new OnPlayerExitListenerImpl();
	protected TeamType _winner = TeamType.NONE;
	protected boolean _aborted;
	protected boolean _isInProgress;

	public DuelEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected DuelEvent(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void initEvent()
	{
		//
	}

	public abstract void packetSurrender(Player player);

	public abstract void onDie(Player player);

	public abstract int getDuelType();

	@Override
	public void startEvent()
	{
		_isInProgress = true;

		for(DuelSnapshotObject $snapshot : this)
		{
			if(canDuel0($snapshot.getPlayer(), $snapshot.getPlayer(), true) != null)
			{
				abortDuel($snapshot.getPlayer());
				return;
			}
		}

		updatePlayers(true, false);

		sendPackets(new ExDuelStart(this), PlaySound.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN);

		for(DuelSnapshotObject $snapshot : this)
			sendPacket(new ExDuelUpdateUserInfo($snapshot.getPlayer()), $snapshot.getTeam().revert());
	}

	public void sendPacket(IBroadcastPacket packet, TeamType... ar)
	{
		for(TeamType a : ar)
		{
			List<DuelSnapshotObject> objs = getObjects(a);

			for(DuelSnapshotObject obj : objs)
				obj.getPlayer().sendPacket(packet);
		}
	}

	@Override
	public void sendPacket(IBroadcastPacket packet)
	{
		sendPackets(packet);
	}

	@Override
	public void sendPackets(IBroadcastPacket... packet)
	{
		for(DuelSnapshotObject d : this)
			d.getPlayer().sendPacket(packet);
	}

	public void abortDuel(Player player)
	{
		_aborted = true;
		_winner = TeamType.NONE;

		stopEvent();
	}

	protected IBroadcastPacket canDuel0(Player requestor, Player target, boolean secondCheck)
	{
		IBroadcastPacket packet = null;
		if(target.isInCombat() )
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(target);
		else if(target.isDead() || target.isAlikeDead() || target.getCurrentHpPercents() < 50 || target.getCurrentMpPercents() < 50 || target.getCurrentCpPercents() < 50)
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50).addName(target);
		else if(!secondCheck && target.getEvent(DuelEvent.class) != null)
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(target);
		else if(secondCheck && target.getEvent(DuelEvent.class) != this)
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(target);
		else if(target.isInZone(Zone.ZoneType.SIEGE))
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR).addName(target);
		else if(target.isInOlympiadMode())
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addName(target);
		else if(target.isCursedWeaponEquipped() || target.getKarma() > 0 || target.getPvpFlag() > 0)
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE).addName(target);
		else if(target.isInStoreMode())
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE).addName(target);
		else if(target.isMounted() || target.isInBoat())
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER).addName(target);
		else if(target.isFishing())
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING).addName(target);
		else if(target.isInCombatZone() || target.isInPeaceZone() || target.isInWater() || target.isInZone(Zone.ZoneType.no_restart))
			packet = new SystemMessage(SystemMsg.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA).addName(target);
		else if(!requestor.isInRangeZ(target, 1200))
			packet = new SystemMessage(SystemMsg.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY).addName(target);
		else if(target.getTransformation() != 0)
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED).addName(target);
		else if(target.getEvent(SingleMatchEvent.class) != (secondCheck ? this : null))
			packet = new SystemMessage(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(target);
		return packet;
	}

	protected void updatePlayers(boolean start, boolean teleport)
	{
		for(DuelSnapshotObject $snapshot : this)
		{
			if(teleport)
				$snapshot.teleportBack();
			else
			{
				if(start)
				{
					$snapshot.store();
					$snapshot.getPlayer().setUndying(SpecialEffectState.TRUE);
					$snapshot.getPlayer().setTeam($snapshot.getTeam());
				}
				else
				{
					$snapshot.getPlayer().setUndying(SpecialEffectState.FALSE);
					$snapshot.getPlayer().removeEvent(this);
					if(!_aborted)
						$snapshot.restore();

					$snapshot.getPlayer().setTeam(TeamType.NONE);
				}
			}
		}
	}

	@Override
	public void onStatusUpdate(Player player)
	{
		sendPacket(new ExDuelUpdateUserInfo(player), player.getTeam().revert());
	}

	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
			return SystemMsg.INVALID_TARGET;

		DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		if(duelEvent == null || duelEvent != this)
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	@Override
	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
			return false;

		DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		return !(duelEvent == null || duelEvent != this);
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().addListener(_playerExitListener);
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().removeListener(_playerExitListener);
	}

	@Override
	public Iterator<DuelSnapshotObject> iterator()
	{
		List<DuelSnapshotObject> blue = getObjects(TeamType.BLUE);
		List<DuelSnapshotObject> red = getObjects(TeamType.RED);
		return new JoinedIterator<DuelSnapshotObject>(blue.iterator(), red.iterator());
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		registerActions();
	}

	@Override
	public EventType getType()
	{
		return EventType.PVP_EVENT;
	}

	@Override
	public void announce(int i)
	{
		sendPacket(new SystemMessage(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(i));
	}

	public void playerLost(Player player)
	{
		player.setTeam(TeamType.NONE);

		for(DuelSnapshotObject $snapshot : this)
		{
			if($snapshot.getPlayer() == player)
			{
				$snapshot.setDead();
				break;
			}
		}

		checkForWinner();
	}

	protected synchronized void checkForWinner()
	{
		TeamType winnerTeam = null;

		for(TeamType team : TeamType.VALUES)
		{
			List<DuelSnapshotObject> objects = getObjects(team);

			boolean allDead = true;
			for(DuelSnapshotObject d : objects)
			{
				if(!d.isDead())
					allDead = false;
			}

			if(allDead)
			{
				winnerTeam = team.revert();
				break;
			}
		}

		if(winnerTeam != null)
		{
			_winner = winnerTeam;

			stopEvent();
		}
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress;
	}
}
