package org.mmocore.gameserver.templates;

import gnu.trove.TIntIntHashMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mmocore.gameserver.skills.SkillEntry;


/**
 * @author VISTALL
 * @date  15:20/22.12.2010
 */
public class CubicTemplate
{
	public static class SkillInfo
	{
		private final SkillEntry _skill;
		private final int _chance;
		private final ActionType _actionType;
		private final boolean _canAttackDoor;
		private final int _minHp;
		private final int _minHpPercent;
		private final int _maxCount;
		private final TIntIntHashMap _chanceList;

		public SkillInfo(SkillEntry skill, int chance, ActionType actionType, boolean canAttackDoor, int minHp, int minHpPercent, int maxCount, TIntIntHashMap set)
		{
			_skill = skill;
			_chance = chance;
			_actionType = actionType;
			_canAttackDoor = canAttackDoor;
			_minHp = minHp;
			_minHpPercent = minHpPercent;
			_maxCount = maxCount;
			_chanceList = set;
		}

		public int getChance()
		{
			return _chance;
		}

		public ActionType getActionType()
		{
			return _actionType;
		}

		public SkillEntry getSkill()
		{
			return _skill;
		}

		public boolean isCanAttackDoor()
		{
			return _canAttackDoor;
		}

		public int getMinHp()
		{
			return _minHp;
		}

		public int getMinHpPercent()
		{
			return _minHpPercent;
		}

		public int getMaxCount()
		{
			return _maxCount;
		}

		public int getChance(int a)
		{
			return _chanceList.get(a);
		}
	}

	public static enum ActionType
	{
		ATTACK,
		DEBUFF,
		CANCEL,
		HEAL
	}

	private final int _id;
	private final int _level;
	private final int _delay;

	private List<Map.Entry<Integer, List<SkillInfo>>> _skills = new ArrayList<Map.Entry<Integer, List<SkillInfo>>>(3);

	public CubicTemplate(int id, int level, int delay)
	{
		_id = id;
		_level = level;
		_delay = delay;
	}

	public void putSkills(int chance, List<SkillInfo> skill)
	{
		_skills.add(new AbstractMap.SimpleImmutableEntry<Integer, List<SkillInfo>>(chance, skill));
	}

	public Iterable<Map.Entry<Integer, List<SkillInfo>>> getSkills()
	{
		return _skills;
	}

	public int getDelay()
	{
		return _delay;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}
}
