package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.network.l2.s2c.ShowMiniMap;
import org.mmocore.gameserver.utils.ItemFunctions;

public class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Map of Hellbound
		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP) ||
				(activeChar.isInZone("[Hellbound_territory]") && ItemFunctions.getItemCount(activeChar, 9994) == 0))
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_AN_AREA_WHERE_YOU_CANNOT_USE_THE_MINI_MAP_THE_MINI_MAP_CANNOT_BE_OPENED);
			return;
		}

		sendPacket(new ShowMiniMap(activeChar, 0));
	}
}