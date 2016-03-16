package org.mmocore.gameserver.network.l2.s2c;

public class AutoAttackStart extends L2GameServerPacket
{
	// dh
	private int _targetId;

	public AutoAttackStart(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x25);
		writeD(_targetId);
	}
}