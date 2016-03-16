package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.BoatHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.utils.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
	private int _objectId;
	private Location _loc = new Location();

	/**
	 * packet type id 0x53
	 * format:      cdddd
	 */
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null || player.isInBoat())
			return;

		final Boat boat = BoatHolder.getInstance().getBoat(_objectId);
		if(boat == null || !boat.isVehicle() || boat.isMoving || !boat.isInRange(player, 600) || _loc.distance3D(0, 0, 0) > 1000)
			return;

		player._stablePoint = boat.getCurrentWay().getReturnLoc();
		boat.addPlayer(player, _loc);
	}
}