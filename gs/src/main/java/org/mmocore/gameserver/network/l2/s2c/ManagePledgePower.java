package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.RankPrivs;

public class ManagePledgePower extends L2GameServerPacket
{
	private int _action, _clanId, privs;

	public ManagePledgePower(Player player, int action, int rank)
	{
		_clanId = player.getClanId();
		_action = action;
		RankPrivs temp = player.getClan().getRankPrivs(rank);
		privs = temp == null ? 0 : temp.getPrivs();
		player.sendPacket(new PledgeReceiveUpdatePower(privs));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2a);
		writeD(_clanId);
		writeD(_action);
		writeD(privs);
	}
}