package org.mmocore.gameserver.network.l2.c2s;

import java.util.Map;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.actor.instances.player.Friend;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
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

		activeChar.sendPacket(SystemMsg.FRIENDS_LIST);
		Map<Integer, Friend> _list = activeChar.getFriendList().getList();
		for(Map.Entry<Integer, Friend> entry : _list.entrySet())
		{
			Player friend = World.getPlayer(entry.getKey());
			if(friend != null)
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CURRENTLY_ONLINE).addName(friend));
			else
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CURRENTLY_OFFLINE).addString(entry.getValue().getName()));
		}
		activeChar.sendPacket(SystemMsg.ID490);
	}
}