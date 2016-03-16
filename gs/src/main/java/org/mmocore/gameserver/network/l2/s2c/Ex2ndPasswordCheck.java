package org.mmocore.gameserver.network.l2.s2c;

/**
 * Format (ch)dd
 * d: window type
 * d: ban user (1)
 */
public class Ex2ndPasswordCheck extends L2GameServerPacket
{
	public static final L2GameServerPacket PASSWORD_NEW = new Ex2ndPasswordCheck(0x00);
	public static final L2GameServerPacket PASSWORD_PROMPT = new Ex2ndPasswordCheck(0x01);
	public static final L2GameServerPacket PASSWORD_OK = new Ex2ndPasswordCheck(0x02);

	private int _windowType;

	public Ex2ndPasswordCheck(int windowType)
	{
		_windowType = windowType;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xE5);
		writeD(_windowType);
		writeD(0x00);
	}
}
