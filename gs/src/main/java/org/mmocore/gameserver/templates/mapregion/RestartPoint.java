package org.mmocore.gameserver.templates.mapregion;

import java.util.List;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.utils.Location;

public class RestartPoint
{
	private final String _name;
	private final int _bbs;
	private final SystemMsg _message;
	private final List<Location> _restartPoints;
	private final List<Location> _PKrestartPoints;

	public RestartPoint(String name, int bbs, int msgId, List<Location> restartPoints, List<Location> PKrestartPoints)
	{
		_name = name;
		_bbs = bbs;
		_message = SystemMsg.valueOf(msgId);
		_restartPoints = restartPoints;
		_PKrestartPoints = PKrestartPoints;
	}

	public String getName()
	{
		return _name;
	}

	public int getBbs()
	{
		return _bbs;
	}

	public SystemMsg getMessage()
	{
		return _message;
	}

	public List<Location> getRestartPoints()
	{
		return _restartPoints;
	}

	public List<Location> getPKrestartPoints()
	{
		return _PKrestartPoints;
	}
}
