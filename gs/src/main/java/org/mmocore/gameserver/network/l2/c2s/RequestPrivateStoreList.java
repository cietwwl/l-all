package org.mmocore.gameserver.network.l2.c2s;

public class RequestPrivateStoreList extends L2GameClientPacket
{

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}