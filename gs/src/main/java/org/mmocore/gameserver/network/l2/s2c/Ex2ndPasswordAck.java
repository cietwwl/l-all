package org.mmocore.gameserver.network.l2.s2c;

public class Ex2ndPasswordAck extends L2GameServerPacket
{
	public static final int SUCCESS = 0x00;
	public static final int WRONG_PATTERN = 0x01;

	private int _response;
	private int _type;

	public Ex2ndPasswordAck(int type, int response)
	{
		_response = response;
		_type = type;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xE7);
		writeC(_type);
		writeD(_response);
		writeD(0x00);
	}
}
