package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Shutdown;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.authcomm.SessionKey;
import org.mmocore.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.LoginFail;
import org.mmocore.gameserver.network.l2.s2c.ServerClose;
import org.mmocore.gameserver.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(AuthLogin.class);

	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private Language _language;

	@Override
	protected void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_language = Language.valueOf(readD());
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		client.setLanguage(_language);
		if(_language == null)
		{
			_log.error("Trying to auth with unknown lang. " + client.toString());
			client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
			return;
		}

		if(Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 30)
		{
			client.closeNow(false);
			return;
		}

		if(AuthServerCommunication.getInstance().isShutdown())
		{
			client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
			return;
		}

		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);

		GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
		if(oldClient != null)
			oldClient.close(ServerClose.STATIC);

		AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
	}
}