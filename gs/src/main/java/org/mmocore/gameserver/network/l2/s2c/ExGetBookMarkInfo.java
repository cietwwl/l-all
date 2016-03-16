package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.TpBookMark;

/**
 * dd d*[ddddSdS]
 */
public class ExGetBookMarkInfo extends L2GameServerPacket
{
	private final int _maxSize;
	private final TpBookMark[] _bookMarks;

	public ExGetBookMarkInfo(Player player)
	{
		_maxSize = player.getTpBookmarkSize();
		_bookMarks = player.getTpBookMarks().toArray(new TpBookMark[player.getTpBookMarks().size()]);
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x84);
		writeD(0x00);
		writeD(_maxSize);
		writeD(_bookMarks.length);
		for(int i = 0; i < _bookMarks.length; i++)
		{
			TpBookMark bookMark = _bookMarks[i];
			writeD(i + 1);
			writeD(bookMark.x);
			writeD(bookMark.y);
			writeD(bookMark.z);
			writeS(bookMark.getName());
			writeD(bookMark.getIcon());
			writeS(bookMark.getAcronym());
		}
	}
}