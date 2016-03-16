package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	private int _objectId, _lootType;
	private final PartySmallWindowAll.PartySmallWindowMemberInfo _member;

	public PartySmallWindowAdd(Party party, Player member)
	{
		_objectId = party.getPartyLeader().getObjectId();
		_lootType = party.getLootDistribution();
		_member = new PartySmallWindowAll.PartySmallWindowMemberInfo(member);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4F);
		writeD(_objectId); // c3
		writeD(_lootType);
		writeD(_member._id);
		writeS(_member._name);
		writeD(_member.curCp);
		writeD(_member.maxCp);
		writeD(_member.curHp);
		writeD(_member.maxHp);
		writeD(_member.curMp);
		writeD(_member.maxMp);
		writeD(_member.level);
		writeD(_member.class_id);
		writeD(0);//sex
		writeD(_member.race_id);
		writeD(_member._isDisguised);
		writeD(_member._dominionId);
	}
}