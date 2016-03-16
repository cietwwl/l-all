package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

/**
 * @author VISTALL
 */
public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	private int _points;

	public ExPVPMatchCCMyRecord(KrateisCubePlayerObject player)
	{
		_points = player.getPoints();
	}

	@Override
	public void writeImpl()
	{
		writeEx(0x8A);
		writeD(_points);
	}
}