package org.mmocore.authserver.network.l2.c2s;

import javax.crypto.Cipher;

import org.mmocore.authserver.Config;
import org.mmocore.authserver.GameServerManager;
import org.mmocore.authserver.IpBanManager;
import org.mmocore.authserver.accounts.Account;
import org.mmocore.authserver.accounts.SessionManager;
import org.mmocore.authserver.accounts.SessionManager.Session;
import org.mmocore.authserver.crypt.PasswordHash;
import org.mmocore.authserver.network.gamecomm.GameServer;
import org.mmocore.authserver.network.gamecomm.as2gs.GetAccountInfo;
import org.mmocore.authserver.network.l2.L2LoginClient;
import org.mmocore.authserver.network.l2.L2LoginClient.LoginClientState;
import org.mmocore.authserver.network.l2.s2c.LoginFail;
import org.mmocore.authserver.network.l2.s2c.LoginFail.LoginFailReason;
import org.mmocore.authserver.network.l2.s2c.LoginOk;
import org.mmocore.authserver.utils.Log;

/**
 * Format: b[128]ddddddhc
 * b[128]: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private byte[] _raw = new byte[128];
	private int _sessionId;

	@Override
	protected void readImpl()
	{
		readB(_raw);
		_sessionId = readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		readH();
		readC();
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2LoginClient client = getClient();

		if(_sessionId != client.getSessionId())
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		byte[] decrypted;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch(Exception e)
		{
			client.closeNow(true);
			return;
		}

		String user = new String(decrypted, 0x5E, 14).trim();
		String password = new String(decrypted, 0x6C, 16).trim();
		int ncotp = ((decrypted[0x7f] & 0xFF) << 24) | ((decrypted[0x7e] & 0xFF) << 16) | ((decrypted[0x7d] & 0xFF) << 8) | ((decrypted[0x7c] & 0xFF) << 0);

		int currentTime = (int) (System.currentTimeMillis() / 1000L);

		user = user.toLowerCase();
		Account account = new Account(user);
		account.restore();

		String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);

		if(account.getPasswordHash() == null)
			if(Config.AUTO_CREATE_ACCOUNTS && user.matches(Config.ANAME_TEMPLATE) && password.matches(Config.APASSWD_TEMPLATE))
			{
				account.setPasswordHash(passwordHash);
				account.save();
			}
			else
			{
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				return;
			}

		boolean passwordCorrect = account.getPasswordHash().equals(passwordHash);

		if(!passwordCorrect)
		{
			// проверяем не зашифрован ли пароль одним из устаревших но поддерживаемых алгоритмов
			for(PasswordHash c : Config.LEGACY_CRYPT)
				if(c.compare(password, account.getPasswordHash()))
				{
					passwordCorrect = true;
					account.setPasswordHash(passwordHash);
					break;
				}
		}

		if(!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect))
		{
			client.closeNow(false);
			return;
		}

		if(!passwordCorrect)
		{
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}

		if(account.getAccessLevel() < 0)
		{
			client.close(LoginFailReason.REASON_ACCOUNT_SUSPENDED);
			return;
		}

		if(account.getBanExpire() > currentTime)
		{
			client.close(LoginFailReason.REASON_ACCOUNT_SUSPENDED);
			return;
		}

		if(!account.isAllowedIP(client.getIpAddress()))
		{
			client.close(LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
			return;
		}

		for(GameServer gs : GameServerManager.getInstance().getGameServers())
			if(gs.getProtocol() >= 2 && gs.isAuthed())
				gs.sendPacket(new GetAccountInfo(user));

		account.setLastAccess(currentTime);
		account.setLastIP(client.getIpAddress());

		Log.LogAccount(account);

		Session session = SessionManager.getInstance().openSession(account);

		client.setAuthed(true);
		client.setLogin(user);
		client.setAccount(account);
		client.setSessionKey(session.getSessionKey());
		client.setState(LoginClientState.AUTHED);

		client.sendPacket(new LoginOk(client.getSessionKey()));
	}
}