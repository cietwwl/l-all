package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.StartRotating;

/**
 * packet type id 0x5b
 * format:		cdd
 */
public class StartRotatingC extends L2GameClientPacket
{
	private int _degree;
	private int _side;

	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setHeading(_degree);
		activeChar.broadcastPacket(new StartRotating(activeChar, _degree, _side, 0));
	}
}