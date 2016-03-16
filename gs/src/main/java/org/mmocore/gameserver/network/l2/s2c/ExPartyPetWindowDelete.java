package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Servitor;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private int _summonObjectId;
	private int _ownerObjectId;
	private String _summonName;

	public ExPartyPetWindowDelete(Servitor servitor)
	{
		_summonObjectId = servitor.getObjectId();
		_summonName = servitor.getName();
		_ownerObjectId = servitor.getPlayer().getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x6a);
		writeD(_summonObjectId);
		writeD(_ownerObjectId);
		writeS(_summonName);
	}
}