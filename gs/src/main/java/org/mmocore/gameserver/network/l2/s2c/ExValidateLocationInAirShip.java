package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.utils.Location;

public class ExValidateLocationInAirShip extends L2GameServerPacket
{
	private int _playerObjectId, _boatObjectId;
	private Location _loc;

	public ExValidateLocationInAirShip(Player cha)
	{
		_playerObjectId = cha.getObjectId();
		_boatObjectId = cha.getBoat().getObjectId();
		_loc = cha.getInBoatPosition();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x6F);

		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}