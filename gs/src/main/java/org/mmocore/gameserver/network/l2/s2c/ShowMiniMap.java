package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private int _mapId, _period;

	public ShowMiniMap(Player player, int mapId)
	{
		_mapId = mapId;
		_period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xa3);
		writeD(_mapId);
		writeC(_period);
	}
}