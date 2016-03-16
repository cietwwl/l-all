package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteObject extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(DeleteObject.class);

	private int _objectId;

	public DeleteObject(GameObject obj)
	{
		this(obj.getObjectId());
	}

	public DeleteObject(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null && activeChar.getObjectId() == _objectId)
		{
			_log.error("You cant send DeleteObject about his character to active user!");
			return;
		}

		writeC(0x08);
		writeD(_objectId);
		writeD(0x01); // Что-то странное. Если объект сидит верхом то при 0 он сперва будет ссажен, при 1 просто пропадет.
	}
}