package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.network.l2.components.ChatType;

public class Snoop extends L2GameServerPacket
{
	private int _objectId, _type;
	private String _name, _speaker, _text;

	public Snoop(int id, String name, ChatType type, String speaker, String txt)
	{
		_objectId = id;
		_name = name;
		_type = type.ordinal();
		_speaker = speaker;
		_text = txt;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xdb);
		writeD(_objectId);
		writeS(_name);
		writeD(_objectId);
		writeD(_type);
		writeS(_speaker);
		writeS(_text);
	}
}