package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.events.impl.CastleSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 18:37/22.05.2011
 */
public class ConditionPlayerSummonSiegeGolem extends Condition
{
	public ConditionPlayerSummonSiegeGolem()
	{
		//
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Player player = env.character.getPlayer();
		if(player == null)
			return false;
		Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
		if(zone != null)
			return false;
		zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
			return false;
		SiegeEvent event = player.getEvent(SiegeEvent.class);
		if(event == null)
			return false;
		if(event instanceof CastleSiegeEvent)
		{
			if(zone.getParams().getInteger("residence") != event.getId())
				return false;
			if(event.getSiegeClan(CastleSiegeEvent.ATTACKERS, player.getClan()) == null)
				return false;
		}
		else
		{
			boolean asClan = event.getSiegeClan(DominionSiegeEvent.DEFENDERS, player.getClan()) != null;
			boolean asPlayer = event.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS).contains(player.getObjectId());
			if(!asClan && !asPlayer)
				return false;
		}

		return true;
	}
}
