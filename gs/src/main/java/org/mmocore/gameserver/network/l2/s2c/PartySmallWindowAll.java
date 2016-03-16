package org.mmocore.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;


/**
 * format   ddd+[dSddddddddddddd{ddSddddd}]
 */
public class PartySmallWindowAll extends L2GameServerPacket
{
	private int leaderId, loot;
	private List<PartySmallWindowMemberInfo> members = new ArrayList<PartySmallWindowMemberInfo>();

	public PartySmallWindowAll(Party party, Player exclude)
	{
		leaderId = party.getPartyLeader().getObjectId();
		loot = party.getLootDistribution();

		for(Player member : party.getPartyMembers())
			if(member != exclude)
				members.add(new PartySmallWindowMemberInfo(member));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4E);
		writeD(leaderId);
		writeD(loot);
		writeD(members.size());
		for(PartySmallWindowMemberInfo member : members)
		{
			writeD(member._id);
			writeS(member._name);
			writeD(member.curCp);
			writeD(member.maxCp);
			writeD(member.curHp);
			writeD(member.maxHp);
			writeD(member.curMp);
			writeD(member.maxMp);
			writeD(member.level);
			writeD(member.class_id);
			writeD(0);//sex
			writeD(member.race_id);
			writeD(member._isDisguised);
			writeD(member._dominionId);

			writeD(member.pet_id);
			if(member.pet_id != 0)
			{
				writeD(member.pet_NpcId);
				writeD(member.pet_type);
				writeS(member.pet_Name);
				writeD(member.pet_curHp);
				writeD(member.pet_maxHp);
				writeD(member.pet_curMp);
				writeD(member.pet_maxMp);
				writeD(member.pet_level);
			}
		}
	}

	public static class PartySmallWindowMemberInfo
	{
		public String _name, pet_Name;
		public boolean _isDisguised;
		public int _id, curCp, maxCp, curHp, maxHp, curMp, maxMp, level, class_id, race_id, _dominionId;
		public int pet_id, pet_type, pet_NpcId, pet_curHp, pet_maxHp, pet_curMp, pet_maxMp, pet_level;

		public PartySmallWindowMemberInfo(Player member)
		{
			_name = member.getName();
			_id = member.getObjectId();
			curCp = (int) member.getCurrentCp();
			maxCp = member.getMaxCp();
			curHp = (int) member.getCurrentHp();
			maxHp = member.getMaxHp();
			curMp = (int) member.getCurrentMp();
			maxMp = member.getMaxMp();
			level = member.getLevel();
			class_id = member.getClassId().getId();
			race_id = member.getRace().ordinal();

			DominionSiegeEvent siegeEvent = member.getEvent(DominionSiegeEvent.class);
			if(siegeEvent != null)
			{
				_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(_id);
				_dominionId = _isDisguised ? siegeEvent.getId() : 0 ;
			}

			Servitor pet = member.getServitor();
			if(pet != null)
			{
				pet_id = pet.getObjectId();
				pet_type = pet.getServitorType();
				pet_NpcId = pet.getNpcId() + 1000000;
				pet_Name = pet.getName();
				pet_curHp = (int) pet.getCurrentHp();
				pet_maxHp = pet.getMaxHp();
				pet_curMp = (int) pet.getCurrentMp();
				pet_maxMp = pet.getMaxMp();
				pet_level = pet.getLevel();
			}
		}
	}
}