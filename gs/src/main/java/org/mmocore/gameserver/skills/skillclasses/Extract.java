package org.mmocore.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mmocore.commons.math.random.RndSelector;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 17:21/31.08.2011
 */
public class Extract extends Skill
{
	public static class ExtractItem
	{
		private final int _itemId;
		private final int _count;

		public ExtractItem(int itemId, int count)
		{
			_itemId = itemId;
			_count = count;
		}
	}

	public static class ExtractGroup extends ArrayList<ExtractItem>
	{
		private static final long serialVersionUID = -2124531921046325587L;

		private final double _chance;

		public ExtractGroup(double chance)
		{
			_chance = chance;
		}
	}

	private final RndSelector<ExtractGroup> _selector;
	private final boolean _isFish;

	@SuppressWarnings("unchecked")
	public Extract(StatsSet set)
	{
		super(set);
		List<ExtractGroup> extractGroupList = (List<ExtractGroup>)set.get("extractlist");
		if(extractGroupList == null)
			extractGroupList = Collections.emptyList();

		_selector = new RndSelector<ExtractGroup>(extractGroupList.size());

		for(ExtractGroup g : extractGroupList)
			_selector.add(g, (int)(g._chance * 10000));

		_isFish = set.getBool("isFish", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			Player targetPlayer = target.getPlayer();
			if(targetPlayer == null)
				return;

			ExtractGroup extractGroup = _selector.chance(RewardList.MAX_CHANCE);
			if(extractGroup != null)
				for(ExtractItem item : extractGroup)
					ItemFunctions.addItem(targetPlayer, item._itemId, _isFish ? (long)(item._count * Config.RATE_FISH_DROP_COUNT) : item._count);
			else
				targetPlayer.sendPacket(SystemMsg.THERE_WAS_NOTHING_FOUND_INSIDE);
		}
	}
}
