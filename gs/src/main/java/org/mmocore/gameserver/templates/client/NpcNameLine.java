package org.mmocore.gameserver.templates.client;

import org.mmocore.gameserver.utils.Language;

public class NpcNameLine
{
	private final int _npcId;
	private final long _color;

	private final String _name;
	private final String _title;

	public NpcNameLine(Language lang, int npcId, String name, String title, long color)
	{
		_npcId = npcId;
		_color = color;

		_name = name;
		_title = title;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public long getColor()
	{
		return _color;
	}

	public String getName()
	{
		return _name;
	}

	public String getTitle()
	{
		return _title;
	}
}
