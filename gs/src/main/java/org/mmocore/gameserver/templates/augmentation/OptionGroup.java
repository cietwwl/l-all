package org.mmocore.gameserver.templates.augmentation;

import org.mmocore.commons.math.random.RndSelector;
import org.mmocore.gameserver.model.reward.RewardList;

/**
 * @author VISTALL
 * @date 15:31/14.03.2012
 */
public class OptionGroup
{
	private RndSelector<Integer> _options = new RndSelector<Integer>();

	public OptionGroup()
	{
		//
	}

	public void addOptionWithChance(int option, int chance)
	{
		_options.add(option, chance);
	}

	public Integer random()
	{
		return _options.chance(RewardList.MAX_CHANCE);
	}
}
