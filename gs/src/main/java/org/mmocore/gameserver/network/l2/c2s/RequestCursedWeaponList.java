package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.network.l2.s2c.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Creature activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExCursedWeaponList());
	}
}