package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;

/**
 * @author VISTALL
 */
public class RequestExBR_LectureMark extends L2GameClientPacket
{
	private int _mark;

	@Override
	protected void readImpl() throws Exception
	{
		_mark = readC();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_LECTURE_MARK)
			return;

		player.setLectureMark(_mark, true);
	}
}