package org.mmocore.gameserver.listener.zone.impl;

import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.listener.zone.OnZoneEnterLeaveListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.tables.PetDataTable;

public class NoLandingZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new NoLandingZoneListener();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		Player player = actor.getPlayer();
		if(player != null)
			if(player.isFlying() && player.getMountNpcId() == PetDataTable.WYVERN_ID)
			{
				Residence residence = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence", 0));
				if(residence != null && player.getClan() != null && residence.getOwner() == player.getClan())
				{
					//
				}
				else
				{
					player.stopMove();
					player.sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
					player.dismount();
				}
			}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature cha)
	{}
}
