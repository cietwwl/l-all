package org.mmocore.gameserver.tables;

import gnu.trove.TIntIntHashMap;

import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.SkillsEngine;
import org.napile.primitive.maps.IntObjectMap;

public class SkillTable
{
	private static final SkillTable _instance = new SkillTable();

	private IntObjectMap<SkillEntry> _skills;
	private TIntIntHashMap _maxLevelsTable;
	private TIntIntHashMap _baseLevelsTable;

	public static SkillTable getInstance()
	{
		return _instance;
	}

	public void load()
	{
		_skills = SkillsEngine.getInstance().loadAllSkills();
		makeLevelsTable();
	}

	public void reload()
	{
		load();
	}

	public SkillEntry getSkillEntry(int skillId, int level)
	{
		return _skills.get(getSkillHashCode(skillId, level));
	}

	public int getMaxLevel(int skillId)
	{
		return _maxLevelsTable.get(skillId);
	}

	public int getBaseLevel(int skillId)
	{
		return _baseLevelsTable.get(skillId);
	}

	public static int getSkillHashCode(Skill skill)
	{
		return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel());
	}

	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return skillId * 1000 + skillLevel;
	}

	private void makeLevelsTable()
	{
		_maxLevelsTable = new TIntIntHashMap();
		_baseLevelsTable = new TIntIntHashMap();
		for(SkillEntry s : _skills.values())
		{
			int skillId = s.getId();
			int level = s.getLevel();
			int maxLevel = _maxLevelsTable.get(skillId);
			if(level > maxLevel)
				_maxLevelsTable.put(skillId, level);
			if(_baseLevelsTable.get(skillId) == 0)
				_baseLevelsTable.put(skillId, s.getTemplate().getBaseLevel());
		}
	}
}