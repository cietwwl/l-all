package org.mmocore.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.templates.moveroute.MoveRoute;

/**
 * @author VISTALL
 * @date 10:16/25.10.2011
 */
public class MoveRouteHolder extends AbstractHolder
{
	private static MoveRouteHolder _instance = new MoveRouteHolder();
	private Map<String, MoveRoute> _routes = new HashMap<String, MoveRoute>();

	public static MoveRouteHolder getInstance()
	{
		return _instance;
	}

	private MoveRouteHolder()
	{
	}

	public void addRoute(MoveRoute route)
	{
		_routes.put(route.getName(), route);
	}

	public MoveRoute getRoute(String name)
	{
		return _routes.get(name);
	}

	@Override
	public int size()
	{
		return _routes.size();
	}

	@Override
	public void clear()
	{
		_routes.clear();
	}
}