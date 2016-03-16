package org.mmocore.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mmocore.commons.math.SafeMath;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;

public class RewardGroup implements Cloneable
{
	private double _chance;
	private boolean _isAdena = false; // Шанс фиксирован, растет только количество
	private boolean _notRate = false; // Рейты вообще не применяются
	private List<RewardData> _items = new ArrayList<RewardData>();
	private double _chanceSum;

	public RewardGroup(double chance)
	{
		setChance(chance);
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}

	public boolean isAdena()
	{
		return _isAdena;
	}

	public void setIsAdena(boolean isAdena)
	{
		_isAdena = isAdena;
	}

	public void addData(RewardData item)
	{
		if(item.getItem().isAdena())
			_isAdena = true;
		_chanceSum += item.getChance();
		item.setChanceInGroup(_chanceSum);
		_items.add(item);
	}

	/**
	 * Возвращает список вещей
	 */
	public List<RewardData> getItems()
	{
		return _items;
	}

	/**
	 * Возвращает полностью независимую копию группы
	 */
	@Override
	public RewardGroup clone()
	{
		RewardGroup ret = new RewardGroup(_chance);
		for(RewardData i : _items)
			ret.addData(i.clone());
		return ret;
	}

	/**
	 * Функция используется в основном механизме расчета дропа, выбирает одну/несколько вещей из группы, в зависимости от рейтов
	 *
	 */
	public List<RewardItem> roll(RewardType type, Player player, double baseRewardRate, double mod)
	{
		switch(type)
		{
			case NOT_RATED_GROUPED:
			case NOT_RATED_NOT_GROUPED:
				return rollItems(1.0, mod);
			case EVENT:
				if (player.getReflection().isDefault())
					return rollItems(player.getRateItems(), mod);
				return Collections.emptyList();
			case SWEEP:
				return rollSpoil(Config.RATE_DROP_SPOIL, player.getRateSpoil(), mod);
			case RATED_GROUPED:
				if(_isAdena)
					return rollAdena(Config.RATE_DROP_ADENA, player.getRateAdena(), mod);

				return rollItems(baseRewardRate, mod);
			default:
				return Collections.emptyList();
		}
	}

	private List<RewardItem> rollItems(double baseRewardRate, double mod)
	{
		if(mod <= 0)
			return Collections.emptyList();

		double rate;
		if(_notRate)
			rate = Math.min(mod, 1.0);
		else
			rate = baseRewardRate * mod;

		double mult = Math.ceil(rate);

		boolean firstPass = true;
		List<RewardItem> ret = new ArrayList<RewardItem>(_items.size() * 3 / 2);
		for(long n = 0; n < mult; n++)
		{
			//double gmult = rate - n;
                        double gmult = rate;
			if(Rnd.get(1, RewardList.MAX_CHANCE) <= _chance * Math.min(gmult, 1.0))
				if(Config.ALT_MULTI_DROP)
				{
					rollFinal(_items, ret, 1.0, firstPass);
				}
				else
				{
					rollFinal(_items, ret, Math.max(gmult, 1.0), firstPass);
					break;
				}

			firstPass = false;
		}
		return ret;
	}

	private List<RewardItem> rollSpoil(double baseRate, double playerRate, double mod)
	{
		if(mod <= 0)
			return Collections.emptyList();

		double rate;
		if(_notRate)
			rate = Math.min(mod, 1.0);
		else
			rate = baseRate * playerRate * mod;

		double mult = Math.ceil(rate);

		boolean firstPass = true;
		List<RewardItem> ret = new ArrayList<RewardItem>(_items.size() * 3 / 2);
		for(long n = 0; n < mult; n++)
		{
			if(Rnd.get(1, RewardList.MAX_CHANCE) <= _chance * Math.min(rate - n, 1.0))
				rollFinal(_items, ret, 1.0, firstPass);

			firstPass = false;
		}

		return ret;
	}

	private List<RewardItem> rollAdena(double baseRate, double playerRate, double mod)
	{
		double chance = _chance;
		if(mod > 10)
		{
			mod *= _chance / RewardList.MAX_CHANCE;
			chance = RewardList.MAX_CHANCE;
		}

		if(mod <= 0)
			return Collections.emptyList();

		if(Rnd.get(0, RewardList.MAX_CHANCE) > chance)
			return Collections.emptyList();

		double rate = baseRate * playerRate * mod;

		List<RewardItem> ret = new ArrayList<RewardItem>(_items.size());
		rollFinal(_items, ret, rate, true);
		for(RewardItem i : ret)
			i.isAdena = true;

		return ret;
	}

	private void rollFinal(List<RewardData> items, List<RewardItem> ret, double mult, boolean firstPass)
	{
		// перебираем все вещи в группе и проверяем шанс
		int chance = Rnd.get(0, RewardList.MAX_CHANCE);
		long count, max;

		for(RewardData i : items)
		{
			if (!firstPass && i.onePassOnly()) // для нерейтующихся итемов делается только первый проход 
				continue;

			if(chance < i.getChanceInGroup() && chance > i.getChanceInGroup() - i.getChance())
			{
				double imult = i.notRate() ? 1.0 : mult;
                                
				count = (long)Math.floor(i.getMinDrop() * imult);
				max = (long)Math.ceil(i.getMaxDrop() * imult);                               
				if (count != max)
					count = Rnd.get(count, max);
                                
				RewardItem t = null;

				for(RewardItem r : ret)
					if(i.getItemId() == r.itemId)
					{
						t = r;
						break;
					}

				if(t == null)
				{
					ret.add(t = new RewardItem(i.getItemId()));
					t.count = count;
				}
				else if(!i.notRate())
				{
					t.count = SafeMath.addAndLimit(t.count, count);
				}

				break;
			}
		}
	}
}