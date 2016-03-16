package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 7:55/03.10.2011
 */
public class ConditionPlayerDominionWar extends Condition
{
	private int _id;

	public ConditionPlayerDominionWar(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		DominionSiegeEvent dominionSiegeEvent = env.character.getEvent(DominionSiegeEvent.class);

		return dominionSiegeEvent != null && dominionSiegeEvent.getId() == _id;
	}
}
