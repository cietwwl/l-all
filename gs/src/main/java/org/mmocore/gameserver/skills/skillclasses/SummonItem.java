package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.ItemFunctions;


public class SummonItem extends Skill
{
	private final int _itemId;
	private final int _minId;
	private final int _maxId;
	private final long _minCount;
	private final long _maxCount;

	public SummonItem(final StatsSet set)
	{
		super(set);

		_itemId = set.getInteger("SummonItemId", 0);
		_minId = set.getInteger("SummonMinId", 0);
		_maxId = set.getInteger("SummonMaxId", _minId);
		_minCount = set.getLong("SummonMinCount");
		_maxCount = set.getLong("SummonMaxCount", _minCount);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, final Creature activeChar, final List<Creature> targets)
	{
		if(!activeChar.isPlayable())
			return;
		for(Creature target : targets)
			if(target != null)
			{
				int itemId = _minId > 0 ? Rnd.get(_minId, _maxId) : _itemId;
				long count = _minCount == _maxCount ? _minCount : Rnd.get(_minCount, _maxCount);

				ItemFunctions.addItem((Playable)activeChar, itemId, count);
				getEffects(skillEntry, activeChar, target, true, false);
			}
	}
}