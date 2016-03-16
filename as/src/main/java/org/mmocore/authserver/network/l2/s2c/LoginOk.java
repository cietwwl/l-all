package org.mmocore.authserver.network.l2.s2c;

import org.mmocore.authserver.network.l2.SessionKey;

/**
 * Format: dddddddd
 * f: the session key
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * b: 16 bytes - unknown
 */
public final class LoginOk extends L2LoginServerPacket
{
	private int _loginOk1, _loginOk2;

	public LoginOk(SessionKey sessionKey)
	{
		_loginOk1 = sessionKey.loginOkID1;
		_loginOk2 = sessionKey.loginOkID2;
	}

	/**
	 * @see org.mmocore.commons.net.nio.impl.SendablePacket#write()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x03);
		writeD(_loginOk1);
		writeD(_loginOk2);
		writeD(0x00);
		writeD(0x00);
		writeD(0x000003ea);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeB(new byte[16]);
	}
}
