package org.mmocore.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTreeTable;


/**
 * format   d (dddc)
 */
public class SkillList extends L2GameServerPacket
{
	private List<SkillEntry> _skills;
	private boolean canEnchant;

	public SkillList(Player p)
	{
		_skills = new ArrayList<SkillEntry>(p.getAllSkills());
		canEnchant = p.getTransformation() == 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x5f);
		writeD(_skills.size());

		for(SkillEntry temp : _skills)
		{
			writeD(temp.getTemplate().isActive() || temp.getTemplate().isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			writeD(temp.getDisplayLevel());
			writeD(temp.getDisplayId());
			writeC(temp.isDisabled() ? 0x01 : 0x00); // иконка скилла серая если не 0
			writeC(canEnchant ? SkillTreeTable.isEnchantable(temp) : 0); // для заточки: если 1 скилл можно точить
		}
	}
}