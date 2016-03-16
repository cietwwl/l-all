package org.mmocore.gameserver.model.entity.events.actions;

import java.util.List;

import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventAction;
import org.mmocore.gameserver.model.reward.RewardGroup;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 18:12/29.04.2012
 */
public class GlobalRewardListAction implements EventAction
{
	private boolean _add;
	private String _name;

	public GlobalRewardListAction(boolean add, String name)
	{
		_add = add;
		_name = name;
	}

	@Override
	public void call(Event event)
	{
		List<Object> list = event.getObjects(_name);
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
		if (npc != null && !npc.getRewards().isEmpty())
			loop: for (RewardList rl : npc.getRewards())
				for (RewardGroup rg : rl)
					if (!rg.isAdena())
					{
						for(Object o : list)
							if(o instanceof RewardList)
							{
								if(_add)
									npc.addRewardList((RewardList)o);
								else
									npc.removeRewardList((RewardList)o);
							}
						break loop;
					}
	}
}
