package org.mmocore.gameserver.model.reward;

import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.templates.item.ItemTemplate;

public class RewardData implements Cloneable
{
	private int _itemId;
	private boolean _notRate;
	private boolean _onePassOnly;
	
	private long _mindrop;
	private long _maxdrop;
	private double _chance;
	private double _chanceInGroup;
	
	public RewardData(int itemId)
	{
		_itemId = itemId;
	}

	public RewardData(int itemId, long min, long max, double chance)
	{
		this(itemId);
		_mindrop = min;
		_maxdrop = max;
		_chance = chance;
	}

	/**
	 * Рейты к вещи не применяются, но делается несколько проходов по группе. Шанс выпадения предмета может достигнуть шанса группы.
	 */
	public boolean notRate()
	{
		return _notRate;
	}

	/**
	 * Рейты к вещи не применяются, плюс делается только один проход по группе. Шанс выпадения предмета соответствует рейтам 1х.
	 */
	public boolean onePassOnly()
	{
		return _onePassOnly;
	}

	public void setNotRate(boolean notRate, boolean onePassOnly)
	{
		_notRate = notRate || onePassOnly;
		_onePassOnly = onePassOnly;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public ItemTemplate getItem()
	{
		return ItemHolder.getInstance().getTemplate(_itemId);
	}

	public long getMinDrop()
	{
		return _mindrop;
	}

	public long getMaxDrop()
	{
		return _maxdrop;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setMinDrop(long mindrop)
	{
		_mindrop = mindrop;
	}

	public void setMaxDrop(long maxdrop)
	{
		_maxdrop = maxdrop;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}

	public void setChanceInGroup(double chance)
	{
		_chanceInGroup = chance;
	}

	public double getChanceInGroup()
	{
		return _chanceInGroup;
	}

	@Override
	public String toString()
	{
		return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
	}

	@Override
	public RewardData clone()
	{
		return new RewardData(getItemId(), getMinDrop(), getMaxDrop(), getChance());
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof RewardData)
		{
			RewardData drop = (RewardData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}
}