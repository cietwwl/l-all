package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExReceiveOlympiad;
import org.mmocore.gameserver.utils.NpcUtils;

/**
 * @author VISTALL
 * @date 0:20/09.04.2011
 */
public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
		// trigger
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if (NpcUtils.canPassPacket(player, this) != null)
			return;

		// fallback
		if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
		{
			player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}

		player.sendPacket(new ExReceiveOlympiad.MatchList());
	}
}
