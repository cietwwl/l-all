package org.mmocore.gameserver.listener.zone.impl;

import org.mmocore.gameserver.listener.zone.OnZoneEnterLeaveListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.events.impl.DuelEvent;

/**
 * @author VISTALL
 * @date 15:07/28.08.2011
 */
public class DuelZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new DuelZoneEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayable())
			return;

		Player player = actor.getPlayer();

		DuelEvent duelEvent = player.getEvent(DuelEvent.class);
		if(duelEvent != null)
			duelEvent.playerLost(player);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{

	}
}
