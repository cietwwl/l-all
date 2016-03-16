package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.network.l2.s2c.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private int _pledgeType;
	private String _target;

	@Override
	protected void readImpl()
	{
		_pledgeType = readD();
		_target = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		SubUnit subUnit = activeChar.getClan() == null ? null : activeChar.getClan().getSubUnit(_pledgeType);
		if(subUnit != null)
		{
			UnitMember cm = subUnit.getUnitMember(_target);
			if(cm != null)
				activeChar.sendPacket(new PledgeReceivePowerInfo(cm));
		}
	}
}