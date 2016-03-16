package org.mmocore.gameserver.network.authcomm.as2gs;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.authcomm.ReceivablePacket;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.ServerClose;

public class KickPlayer extends ReceivablePacket
{
	String account;

	@Override
	public void readImpl()
	{
		account = readS();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			client = AuthServerCommunication.getInstance().removeAuthedClient(account);
		if(client == null)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar != null)
			activeChar.kick();
		else
			client.close(ServerClose.STATIC);
	}
}