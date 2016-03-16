package org.mmocore.gameserver.network.l2.c2s;

import java.util.List;

import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.residence.Fortress;
import org.mmocore.gameserver.network.l2.s2c.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends L2GameClientPacket
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
		List<Fortress> fortressList = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
		for(Fortress fort : fortressList)
			if(fort != null && fort.getSiegeEvent().isInProgress())
				activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
	}
}