package org.mmocore.gameserver.templates.moveroute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @date 22:12/24.10.2011
 */
public class MoveRoute
{
	private final List<MoveNode> _nodes = new ArrayList<MoveNode>();
	private final String _name;
	private final MoveRouteType _type;
	private final boolean _isRunning;

	public MoveRoute(String name, MoveRouteType type, boolean isRunning)
	{
		_name = name;
		_type = type;
		_isRunning = isRunning;
	}

	public List<MoveNode> getNodes()
	{
		return _nodes;
	}

	public String getName()
	{
		return _name;
	}

	public MoveRouteType getType()
	{
		return _type;
	}

	public boolean isRunning()
	{
		return _isRunning;
	}
}
