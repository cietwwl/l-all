package org.mmocore.gameserver.network.l2.s2c;

import java.util.Collection;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;


public class GMViewSkillInfo extends L2GameServerPacket
{
	private String _charName;
	private Collection<SkillEntry> _skills;

	public GMViewSkillInfo(Player cha)
	{
		_charName = cha.getName();
		_skills = cha.getAllSkills();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeS(_charName);
		writeD(_skills.size());
		for(SkillEntry skill : _skills)
		{
			writeD(skill.getTemplate().isPassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(skill.isDisabled() ? 0x01 : 0x00);
			writeC(SkillTable.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
	}
}