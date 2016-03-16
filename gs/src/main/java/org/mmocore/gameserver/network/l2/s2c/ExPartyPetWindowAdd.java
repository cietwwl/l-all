package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Servitor;

public class ExPartyPetWindowAdd extends L2GameServerPacket
{
	private final int ownerId, npcId, type, curHp, maxHp, curMp, maxMp, level;
	private final int summonId;
	private final String name;

	public ExPartyPetWindowAdd(Servitor servitor)
	{
		summonId = servitor.getObjectId();
		ownerId = servitor.getPlayer().getObjectId();
		npcId = servitor.getTemplate().npcId + 1000000;
		type = servitor.getServitorType();
		name = servitor.getName();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		level = servitor.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x18);
		writeD(summonId);
		writeD(npcId);
		writeD(type);
		writeD(ownerId);
		writeS(name);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
	}
}