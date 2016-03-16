package org.mmocore.gameserver.listener.zone.impl;

import org.mmocore.gameserver.listener.zone.OnZoneEnterLeaveListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.events.objects.TerritoryWardObject;
import org.mmocore.gameserver.model.items.attachment.FlagItemAttachment;

/**
 * @author VISTALL
 * @date 12:48/18.09.2011
 */
public class DominionWardEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new DominionWardEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Player player = actor.getPlayer();
		FlagItemAttachment flag = player.getActiveWeaponFlagAttachment();
		if(flag instanceof TerritoryWardObject)
		{
			flag.onLogout(player);

			player.sendDisarmMessage(((TerritoryWardObject) flag).getWardItemInstance());
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		//
	}
}
