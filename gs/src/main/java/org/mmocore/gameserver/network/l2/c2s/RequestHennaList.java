package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.HennaEquipList;

public class RequestHennaList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//readD(); - unknown
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new HennaEquipList(player));
	}
}