package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Servitor;

public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType;

	public PetStatusShow(Servitor servitor)
	{
		_summonType = servitor.getServitorType();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb1);
		writeD(_summonType);
	}
}