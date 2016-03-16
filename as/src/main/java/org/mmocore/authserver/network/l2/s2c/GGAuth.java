package org.mmocore.authserver.network.l2.s2c;


/**
 * Fromat: d
 * d: response
 */
public final class GGAuth extends L2LoginServerPacket
{
	public static int SKIP_GG_AUTH_REQUEST = 0x0b;

	private int _response;

	public GGAuth(int response)
	{
		_response = response;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x0b);
		writeD(_response);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}
}
