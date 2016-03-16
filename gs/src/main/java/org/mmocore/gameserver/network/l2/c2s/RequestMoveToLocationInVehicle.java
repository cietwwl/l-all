package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.data.BoatHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.utils.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
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
		if (player == null)
			return;

		final Boat boat = BoatHolder.getInstance().getBoat(_boatObjectId); // этот же пакет используется для подбегания к кораблю
		if (boat == null || !player.isInRange(boat, 1000)) // сам вход на корабль в RequestGetOnVehicle
		{
			player.sendActionFailed();
			return;
		}

		if (!boat.isVehicle() || _pos.distance3D(0, 0, 0) > 1000 || _originPos.distance3D(0, 0, 0) > 1000)
		{
			player.sendActionFailed();
			return;
		}

		boat.moveInBoat(player, _originPos, _pos);
	}
}