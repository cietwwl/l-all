package org.mmocore.gameserver.network.l2.s2c;

public class SunRise extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x12);
	}
}