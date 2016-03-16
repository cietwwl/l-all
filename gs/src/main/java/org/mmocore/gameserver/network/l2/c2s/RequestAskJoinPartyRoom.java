package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.matching.MatchingRoom;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExAskJoinPartyRoom;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * format: (ch)S
 */
public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private String _name; // not tested, just guessed

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		Player targetPlayer = World.getPlayer(_name);

		if(targetPlayer == null || targetPlayer == player)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isProcessingRequest())
		{
			player.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(targetPlayer.isProcessingRequest())
		{
			player.sendPacket(new SystemMessage(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(targetPlayer));
			return;
		}

		if(targetPlayer.getMatchingRoom() != null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
			return;
/*
		if(room.getLeader() != player)
		{
			player.sendPacket(SystemMsg.ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM);
			return;
		}
*/
		if(room.getPlayers().size() >= room.getMaxMembersSize())
		{
			player.sendPacket(SystemMsg.THE_PARTY_ROOM_IS_FULL);
			return;
		}

		new Request(L2RequestType.PARTY_ROOM, player, targetPlayer).setTimeout(10000L);

		targetPlayer.sendPacket(new ExAskJoinPartyRoom(player.getName(), room.getTopic()));

		player.sendPacket(new SystemMessage(SystemMsg.S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2).addName(player).addString(room.getTopic()));
		targetPlayer.sendPacket(new SystemMessage(SystemMsg.S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2).addName(player).addString(room.getTopic()));
	}
}