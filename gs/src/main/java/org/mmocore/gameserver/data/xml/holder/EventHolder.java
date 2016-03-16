package org.mmocore.gameserver.data.xml.holder;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

/**
 * @author VISTALL
 * @date  12:55/10.12.2010
 */
public final class EventHolder extends AbstractHolder
{
	private static final EventHolder _instance = new EventHolder();
	private final IntObjectMap<Event> _events = new TreeIntObjectMap<Event>();

	public static EventHolder getInstance()
	{
		return _instance;
	}

	public void addEvent(Event event)
	{
		_events.put(event.getType().step() + event.getId(), event);
	}

	@SuppressWarnings("unchecked")
	public <E extends Event> E getEvent(EventType type, int id)
	{
		return (E) _events.get(type.step() + id);
	}

	public void findEvent(Player player)
	{
		for(Event event : _events.values())
			event.findEvent(player);
	}

	public void callInit()
	{
		for(Event event : _events.values())
			event.initEvent();
	}

	@Override
	public int size()
	{
		return _events.size();
	}

	@Override
	public void clear()
	{
		_events.clear();
	}
}
