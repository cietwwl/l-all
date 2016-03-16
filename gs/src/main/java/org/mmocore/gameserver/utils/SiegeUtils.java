package org.mmocore.gameserver.utils;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.tables.SkillTable;

/**
 * @author VISTALL
 * @date 12:23/21.02.2011
 */
public class SiegeUtils
{
	public static final int MIN_CLAN_SIEGE_LEVEL = 4;

	public static void addSiegeSkills(Player character)
	{
		character.addSkill(SkillTable.getInstance().getSkillEntry(246, 1), false);
		character.addSkill(SkillTable.getInstance().getSkillEntry(247, 1), false);
		if(character.isNoble())
			character.addSkill(SkillTable.getInstance().getSkillEntry(326, 1), false);

		character.addSkill(SkillTable.getInstance().getSkillEntry(844, 1), false);
		character.addSkill(SkillTable.getInstance().getSkillEntry(845, 1), false);
	}

	public static void removeSiegeSkills(Player character)
	{
		character.removeSkill(SkillTable.getInstance().getSkillEntry(246, 1), false);
		character.removeSkill(SkillTable.getInstance().getSkillEntry(247, 1), false);
		character.removeSkill(SkillTable.getInstance().getSkillEntry(326, 1), false);
		character.removeSkill(SkillTable.getInstance().getSkillEntry(844, 1), false);
		character.removeSkill(SkillTable.getInstance().getSkillEntry(845, 1), false);
	}
}
