package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.dao.CharacterTPBookmarkDAO;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.TpBookMark;
import org.mmocore.gameserver.network.l2.s2c.ExGetBookMarkInfo;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
	private int _slot;

	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		TpBookMark bookMark = player.getTpBookMarks().remove(_slot - 1);
		if(bookMark != null)
		{
			CharacterTPBookmarkDAO.getInstance().delete(player, bookMark);
			player.sendPacket(new ExGetBookMarkInfo(player));
		}
	}
}