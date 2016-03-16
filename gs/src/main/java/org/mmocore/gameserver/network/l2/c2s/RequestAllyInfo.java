package org.mmocore.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.Alliance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.tables.ClanTable;


public class RequestAllyInfo extends L2GameClientPacket
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

		Alliance ally = player.getAlliance();
		if(ally == null)
			return;

		int clancount = 0;
		Clan leaderclan = player.getAlliance().getLeader();
		clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
		int[] online = new int[clancount + 1];
		int[] count = new int[clancount + 1];
		Clan[] clans = player.getAlliance().getMembers();
		for(int i = 0; i < clancount; i++)
		{
			online[i + 1] = clans[i].getOnlineMembers(0).size();
			count[i + 1] = clans[i].getAllSize();
			online[0] += online[i + 1];
			count[0] += count[i + 1];
		}

		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>(7 + 5 * clancount);
		packets.add(new SystemMessage(SystemMsg.ALLIANCE_INFORMATION));
		packets.add(new SystemMessage(SystemMsg.ALLIANCE_NAME_S1).addString(player.getClan().getAlliance().getAllyName()));
		packets.add(new SystemMessage(SystemMsg.CONNECTION_S1__TOTAL_S2).addNumber(online[0]).addNumber(count[0])); //Connection
		packets.add(new SystemMessage(SystemMsg.ALLIANCE_LEADER_S2_OF_S1).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));
		packets.add(new SystemMessage(SystemMsg.AFFILIATED_CLANS_TOTAL_S1_CLANS).addNumber(clancount)); //clan count
		packets.add(new SystemMessage(SystemMsg.CLAN_INFORMATION));
		for(int i = 0; i < clancount; i++)
		{
			packets.add(new SystemMessage(SystemMsg.CLAN_NAME_S1).addString(clans[i].getName()));
			packets.add(new SystemMessage(SystemMsg.CLAN_LEADER__S1).addString(clans[i].getLeaderName()));
			packets.add(new SystemMessage(SystemMsg.CLAN_LEVEL_S1).addNumber(clans[i].getLevel()));
			packets.add(new SystemMessage(SystemMsg.CONNECTION_S1__TOTAL_S2).addNumber(online[i + 1]).addNumber(count[i + 1]));
			packets.add(new SystemMessage(SystemMsg.ID500));
		}
		packets.add(new SystemMessage(SystemMsg.ID490));

		player.sendPacket(packets);
	}
}