package org.mmocore.gameserver.model.entity.boat;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.ExAirShipInfo;
import org.mmocore.gameserver.network.l2.s2c.ExGetOffAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExGetOnAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExMoveToLocationAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExMoveToLocationInAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExStopMoveAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExStopMoveInAirShip;
import org.mmocore.gameserver.network.l2.s2c.ExValidateLocationInAirShip;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.templates.CharTemplate;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date  17:45/26.12.2010
 */
public class AirShip extends Boat
{
	public AirShip(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new ExAirShipInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new ExMoveToLocationAirShip(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location dst)
	{
		return new ExMoveToLocationInAirShip(player,  this, dst);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new ExStopMoveAirShip(this);
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new ExStopMoveInAirShip(player);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ExValidateLocationInAirShip(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new ExGetOnAirShip(player, this);
	}

	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new ExGetOffAirShip(player, this, location);
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	@Override
	public void oustPlayers()
	{
		for(Player player : _players)
		{
			oustPlayer(player, getReturnLoc(), true);
		}
	}
}
