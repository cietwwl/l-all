package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.network.l2.GameClient;

public class CharacterSelected extends L2GameClientPacket
{
	private int _index;

	@Override
	protected void readImpl()
	{
		_index = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		client.playerSelected(_index);
	}
}