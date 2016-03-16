package org.mmocore.gameserver.templates.client;

public class NpcStringLine
{
	public static final String[] PARAMS = {"$s1", "$s2", "$s3", "$s4", "$s5"};

	private final int _id, _size;
	private final String _value;

	public NpcStringLine(int id, String value)
	{

		_id = id;
		_value = value;

		for (int i = PARAMS.length; --i >= 0;)
			if (value.contains(PARAMS[i]))
			{
				_size = i + 1;
				return;
			}

		_size = 0;
	}

	public final int getId()
	{
		return _id;
	}

	public final int getSize()
	{
		return _size;
	}

	public final String getValue()
	{
		return _value;
	}
}