package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.network.l2.GameClient;

/**
 * format: ddd
 */
public class NetPing extends L2GameClientPacket
{
	private int _time, _unk1, _unk2;

	@Override
	protected void readImpl()
	{
		_time = readD();
		_unk1 = readD();
		_unk2 = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		client.onNetPing(_time);
	}
}