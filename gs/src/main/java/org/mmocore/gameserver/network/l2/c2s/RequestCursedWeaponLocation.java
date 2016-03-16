package org.mmocore.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.model.CursedWeapon;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.network.l2.s2c.ExCursedWeaponLocation;
import org.mmocore.gameserver.network.l2.s2c.ExCursedWeaponLocation.CursedWeaponInfo;
import org.mmocore.gameserver.utils.Location;


public class RequestCursedWeaponLocation extends L2GameClientPacket
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

		List<CursedWeaponInfo> list = new ArrayList<CursedWeaponInfo>();
		for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			Location pos = cw.getWorldPosition();
			if(pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}

		activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
}