package org.mmocore.gameserver.model.entity.events.actions;

import org.mmocore.gameserver.model.entity.events.EventAction;
import org.mmocore.gameserver.model.entity.events.Event;

/**
 * @author VISTALL
 * @date 11:12/11.03.2011
 */
public class AnnounceAction implements EventAction
{
	private int _id;

	public AnnounceAction(int id)
	{
		_id = id;
	}

	@Override
	public void call(Event event)
	{
		event.announce(_id);
	}
}
