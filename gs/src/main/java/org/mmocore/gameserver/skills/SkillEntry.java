package org.mmocore.gameserver.skills;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.stats.funcs.Func;

/**
 * @author VISTALL
 * @date 0:15/03.06.2011
 */
public class SkillEntry
{
	public static final SkillEntry[] EMPTY_ARRAY = new SkillEntry[0];

	private final SkillEntryType _entryType;
	private final Skill _skill;

	private boolean _disabled;

	public SkillEntry(SkillEntryType key, Skill value)
	{
		_entryType = key;
		_skill = value;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}

	public SkillEntryType getEntryType()
	{
		return _entryType;
	}

	public Skill getTemplate()
	{
		return _skill;
	}

	public int getId()
	{
		return _skill.getId();
	}

	public int getDisplayId()
	{
		return _skill.getDisplayId();
	}

	public int getLevel()
	{
		return _skill.getLevel();
	}

	public int getDisplayLevel()
	{
		return _skill.getDisplayLevel();
	}

	public Skill.SkillType getSkillType()
	{
		return _skill.getSkillType();
	}

	public String getName()
	{
		return _skill.getName();
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return _skill.checkCondition(this, activeChar, target, forceUse, dontMove, first);
	}

	public SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		return _skill.checkTarget(activeChar, target, aimingTarget, forceUse, first);
	}

	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster)
	{
		_skill.getEffects(this, effector, effected, calcChance, applyOnCaster);
	}

	public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final long timeConst, final double timeMult, final int timeFix)
	{
		_skill.getEffects(this, effector, effected, calcChance, applyOnCaster, timeConst, timeMult, timeFix);
	}

	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		_skill.useSkill(this, activeChar, targets);
	}

	public SkillEntry copyTo(SkillEntryType entryType)
	{
		return new SkillEntry(entryType, _skill);
	}

	public Func[] getStatFuncs()
	{
		return _skill.getStatFuncs(this);
	}

	public Skill getLockedSkill()
	{
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		return hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode()
	{
		return _skill.hashCode();
	}

	@Override
	public String toString()
	{
		return _skill.toString();
	}
}
