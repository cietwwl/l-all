package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.collections.CollectionUtils;
import org.mmocore.gameserver.dao.CharacterTPBookmarkDAO;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.TpBookMark;
import org.mmocore.gameserver.network.l2.s2c.ExGetBookMarkInfo;

/**
 * dSdS
 */
public class RequestModifyBookMarkSlot extends L2GameClientPacket
{
	private String _name, _acronym;
	private int _icon, _slot;

	@Override
	protected void readImpl()
	{
		_slot = readD();
		_name = readS(32);
		_icon = readD();
		_acronym = readS(4);
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		final TpBookMark mark = CollectionUtils.safeGet(player.getTpBookMarks(), _slot - 1);
		if(mark != null)
		{
			mark.setName(_name);
			mark.setIcon(_icon);
			mark.setAcronym(_acronym);

			CharacterTPBookmarkDAO.getInstance().update(player, mark);
			player.sendPacket(new ExGetBookMarkInfo(player));
		}
	}
}