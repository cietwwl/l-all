package org.mmocore.gameserver.model;

import org.mmocore.gameserver.Config;

/**
 * @author VISTALL
 */
public final class SkillLearn implements Comparable<SkillLearn>
{
	private final int _id;
	private final int _level;
	private final int _minLevel;
	private final int _cost;
	private final int _itemId;
	private final long _itemCount;
	private final boolean _clicked;
	private final Boolean _autoLearn;

	public SkillLearn(int id, int lvl, int minLvl, int cost, int itemId, long itemCount, boolean clicked, Boolean autoLearn)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_cost = cost;

		_itemId = itemId;
		_itemCount = itemCount;
		_clicked = clicked;
		_autoLearn = autoLearn;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getCost()
	{
		return _cost;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public boolean isClicked()
	{
		return _clicked;
	}

	public boolean canAutoLearn()
	{
		if (_autoLearn != null)
			return _autoLearn.booleanValue();

		if (_id == 1405)
			return Config.AUTO_LEARN_DIVINE_INSPIRATION;

		return !_clicked || Config.AUTO_LEARN_FORGOTTEN_SKILLS;
	}

	@Override
	public int compareTo(SkillLearn o)
	{
		if(getId() == o.getId())
			return getLevel() - o.getLevel();
		else
			return getId() - o.getId();
	}
}