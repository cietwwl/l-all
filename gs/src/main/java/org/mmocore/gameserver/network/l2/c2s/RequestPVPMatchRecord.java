package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.events.impl.UndergroundColiseumBattleEvent;
import org.mmocore.gameserver.network.l2.s2c.ExPVPMatchRecord;

/**
 * @author VISTALL
 */
public class RequestPVPMatchRecord extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		UndergroundColiseumBattleEvent battleEvent = player.getEvent(UndergroundColiseumBattleEvent.class);
		if(battleEvent == null)
			return;

		player.sendPacket(new ExPVPMatchRecord(ExPVPMatchRecord.UPDATE, TeamType.NONE, battleEvent));
	}
}