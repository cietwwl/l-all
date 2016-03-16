package org.mmocore.authserver.network.l2.c2s;

import org.mmocore.authserver.network.l2.L2LoginClient;
import org.mmocore.authserver.network.l2.L2LoginClient.LoginClientState;
import org.mmocore.authserver.network.l2.s2c.GGAuth;
import org.mmocore.authserver.network.l2.s2c.LoginFail;

/**
 * @author -Wooden-
 * Format: ddddd
 *
 */
public class AuthGameGuard extends L2LoginClientPacket
{
	private int _sessionId;
	//private int _data1;
	//private int _data2;
	//private int _data3;
	//private int _data4;

	@Override
	protected void readImpl()
	{
		_sessionId = readD();
		/*_data1 = readD();
		_data2 = readD();
		_data3 = readD();
		_data4 = readD(); */
	}

	@Override
	protected void runImpl()
	{
		L2LoginClient client = getClient();

		if(_sessionId != client.getSessionId())
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		client.setState(LoginClientState.AUTHED_GG);
		client.sendPacket(new GGAuth(client.getSessionId()));
	}
}
