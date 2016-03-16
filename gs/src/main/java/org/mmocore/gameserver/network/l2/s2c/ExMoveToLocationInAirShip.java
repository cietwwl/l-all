package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.utils.Location;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private int char_id, boat_id;
	private Location _destination;

	public ExMoveToLocationInAirShip(Player cha, Boat boat, Location destination)
	{
		char_id = cha.getObjectId();
		boat_id = boat.getObjectId();
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x6D);
		writeD(char_id);
		writeD(boat_id);

		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_destination.h);
	}
}