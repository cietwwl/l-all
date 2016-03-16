package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.collections.CollectionUtils;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.TpBookMark;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;

public class RequestTeleportBookMark extends L2GameClientPacket
{
	private static final SkillEntry SKILL = SkillTable.getInstance().getSkillEntry(2588, 1);
	private int _slot;

	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled() || activeChar.isTeleportBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		TpBookMark bookMark = CollectionUtils.safeGet(activeChar.getTpBookMarks(), _slot - 1);
		if(bookMark == null)
			return;

		activeChar.getVars().set(Player.TELEPORT_BOOKMARK, bookMark);
		activeChar.getAI().Cast(SKILL, activeChar);
	}
}