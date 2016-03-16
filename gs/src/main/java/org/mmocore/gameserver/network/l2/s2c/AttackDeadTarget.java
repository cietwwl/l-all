package org.mmocore.gameserver.network.l2.s2c;

public class AttackDeadTarget extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// just trigger - без аргументов
		writeC(0x04);
	}
}