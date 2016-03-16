package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.utils.Location;

public class RequestExMoveToLocationInAirShip extends L2GameClientPacket
{
	private Location _pos = new Location();
	private Location _originPos = new Location();
	private int _boatObjectId;

	@Override
	protected void readImpl()
	{
		_boatObjectId = readD();
		_pos.x = readD();
		_pos.y = readD();
		_pos.z = readD();
		_originPos.x = readD();
		_originPos.y = readD();
		_originPos.z = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		final Boat boat = player.getBoat();
		if(boat == null || !boat.isAirShip() || boat.getObjectId() != _boatObjectId || _pos.distance3D(0, 0, 0) > 1000 || _originPos.distance3D(0, 0, 0) > 1000)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isClanAirShipDriver())
		{
			player.sendActionFailed();
			return;
		}

		boat.moveInBoat(player, _originPos, _pos);
	}
}