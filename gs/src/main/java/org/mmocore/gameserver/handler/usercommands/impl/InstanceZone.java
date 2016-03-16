package org.mmocore.gameserver.handler.usercommands.impl;

import org.mmocore.gameserver.data.xml.holder.InstantZoneHolder;
import org.mmocore.gameserver.handler.usercommands.IUserCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * Support for command: /instancezone
 */
public class InstanceZone implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 114 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		Reflection actionRef = activeChar.getActiveReflection();
		if(actionRef != null)
			activeChar.sendPacket(new SystemMessage(SystemMsg.INSTANT_ZONE_CURRENTLY_IN_USE_S1).addInstanceName(actionRef.getInstancedZoneDisplayId()));

		int limit;
		boolean noLimit = true;
		boolean showMsg = false;
		for(int i : activeChar.getInstanceReuses().keySet())
		{
			limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, activeChar);
			if(limit > 0)
			{
				noLimit = false;
				if(!showMsg)
				{
					activeChar.sendPacket(SystemMsg.INSTANCE_ZONE_TIME_LIMIT);
					showMsg = true;
				}
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_WILL_BE_AVAILABLE_FOR_REUSE_AFTER_S2_HOURS_S3_MINUTES).addInstanceName(i).addNumber(limit / 60).addNumber(limit % 60));
			}
		}
		if(noLimit)
			activeChar.sendPacket(SystemMsg.THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT);

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}