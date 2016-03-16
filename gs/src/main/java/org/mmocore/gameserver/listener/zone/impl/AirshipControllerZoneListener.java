package org.mmocore.gameserver.listener.zone.impl;

import org.mmocore.gameserver.listener.zone.OnZoneEnterLeaveListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.boat.ClanAirShip;
import org.mmocore.gameserver.model.instances.ClanAirShipControllerInstance;

public class AirshipControllerZoneListener implements OnZoneEnterLeaveListener
{
	private ClanAirShipControllerInstance _controllerInstance;

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(_controllerInstance == null && actor instanceof ClanAirShipControllerInstance)
			_controllerInstance = (ClanAirShipControllerInstance) actor;
		else if(actor.isClanAirShip())
			_controllerInstance.setDockedShip((ClanAirShip) actor);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		if(actor.isClanAirShip())
			_controllerInstance.setDockedShip(null);
	}
}
