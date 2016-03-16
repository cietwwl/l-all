package org.mmocore.gameserver.model.entity.boat;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.GetOffVehicle;
import org.mmocore.gameserver.network.l2.s2c.GetOnVehicle;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.MoveToLocationInVehicle;
import org.mmocore.gameserver.network.l2.s2c.StopMove;
import org.mmocore.gameserver.network.l2.s2c.StopMoveToLocationInVehicle;
import org.mmocore.gameserver.network.l2.s2c.ValidateLocationInVehicle;
import org.mmocore.gameserver.network.l2.s2c.VehicleCheckLocation;
import org.mmocore.gameserver.network.l2.s2c.VehicleDeparture;
import org.mmocore.gameserver.network.l2.s2c.VehicleInfo;
import org.mmocore.gameserver.network.l2.s2c.VehicleStart;
import org.mmocore.gameserver.templates.CharTemplate;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date  17:46/26.12.2010
 */
public class Vehicle extends Boat
{
	public Vehicle(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return new VehicleStart(this);
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ValidateLocationInVehicle(player);
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return new VehicleCheckLocation(this);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new VehicleInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new VehicleDeparture(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new MoveToLocationInVehicle(player, this, src, desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new StopMoveToLocationInVehicle(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new GetOnVehicle(player, this, location);
	}

	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new GetOffVehicle(player, this, location);
	}

	@Override
	public void oustPlayers()
	{
		//
	}

	@Override
	public boolean isVehicle()
	{
		return true;
	}
}
