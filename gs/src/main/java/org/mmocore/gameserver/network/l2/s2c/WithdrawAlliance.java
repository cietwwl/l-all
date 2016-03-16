package org.mmocore.gameserver.network.l2.s2c;

public class WithdrawAlliance extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xAB);
		//TODO d
	}
}