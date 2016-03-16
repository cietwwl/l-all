package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.model.ObservePoint;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.utils.Location;

public class CannotMoveAnymore extends L2GameClientPacket
{
	private Location _loc = new Location();

	/**
	 * packet type id 0x47
	 *
	 * sample
	 *
	 * 36
	 * a8 4f 02 00 // x
	 * 17 85 01 00 // y
	 * a7 00 00 00 // z
	 * 98 90 00 00 // heading?
	 *
	 * format:		cdddd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (activeChar.isInObserverMode())
		{
			ObservePoint observer = activeChar.getObservePoint();
			if (observer != null)
				observer.stopMove();
			return;
		}

		if (!activeChar.isOutOfControl())
			activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
	}
}