package org.mmocore.gameserver.network.l2.s2c;

public class ClientAction extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0x8F);
		//TODO d
	}
}