package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Servitor;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private final int _maxFed;
	private final int _curFed;

	public SetSummonRemainTime(Servitor servitor)
	{
		_curFed = servitor.getCurrentFed();
		_maxFed = servitor.getMaxFed();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xD1);
		writeD(_maxFed);
		writeD(_curFed);
	}
}