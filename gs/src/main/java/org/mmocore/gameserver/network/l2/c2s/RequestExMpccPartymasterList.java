package org.mmocore.gameserver.network.l2.c2s;

import java.util.HashSet;
import java.util.Set;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.matching.MatchingRoom;
import org.mmocore.gameserver.network.l2.s2c.ExMpccPartymasterList;

/**
 * @author VISTALL
 */
public class RequestExMpccPartymasterList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.CC_MATCHING)
			return;

		Set<String> set = new HashSet<String>();
		for(Player $member : room.getPlayers())
			if($member.getParty() != null)
				set.add($member.getParty().getPartyLeader().getName());

		player.sendPacket(new ExMpccPartymasterList(set));
	}
}