package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.cache.CrestCache;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.PledgeCrest;

public class RequestPledgeCrest extends L2GameClientPacket
{
	// format: cd

	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_crestId == 0)
			return;
		byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
		if(data != null)
		{
			PledgeCrest pc = new PledgeCrest(_crestId, data);
			sendPacket(pc);
		}
	}
}