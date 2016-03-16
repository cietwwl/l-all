package org.mmocore.gameserver.model.entity.events;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author VISTALL
 * @date 10:27/24.02.2011
 */
public abstract class EventOwner
{
	private Set<Event> _events = new ConcurrentSkipListSet<Event>(EventComparator.getInstance());

	@SuppressWarnings("unchecked")
	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		for(Event e : _events)
		{
			if(e.getClass() == eventClass)    // fast hack
				return (E)e;
			if(eventClass.isAssignableFrom(e.getClass()))    //FIXME [VISTALL]    какойто другой способ определить
				return (E)e;
		}

		return null;
	}

	public void addEvent(Event event)
	{
		_events.add(event);
	}

	public void removeEvent(Event event)
	{
		_events.remove(event);
	}

	public void removeEvents(Class<? extends Event> eventClass)
	{
		for(Event e : _events)
		{
			if(e.getClass() == eventClass)    // fast hack
				_events.remove(e);
			else if(eventClass.isAssignableFrom(e.getClass()))    //FIXME [VISTALL]    какойто другой способ определить
				_events.remove(e);
		}

	}

	public Set<Event> getEvents()
	{
		return _events;
	}
}
