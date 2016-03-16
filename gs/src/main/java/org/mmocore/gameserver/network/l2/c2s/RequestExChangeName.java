package org.mmocore.gameserver.network.l2.c2s;

import java.util.List;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.data.client.holder.NpcNameLineHolder;
import org.mmocore.gameserver.model.CharSelectInfo;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.ExNeedToChangeName;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.utils.Util;

/**
 * @author VISTALL
 */
public class RequestExChangeName extends L2GameClientPacket
{
	private int _type;
	private String _name;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_name = readS();
		//readD();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.EX_CHANGE_NAME_DIALOG)
			return;

		GameClient client = getClient();
		boolean canReName = false;
		switch(_type)
		{
			case ExNeedToChangeName.TYPE_PLAYER_NAME:
				if(client.getActiveChar() != null)
					return;

				if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE) || NpcNameLineHolder.getInstance().isBlackListContainsName(_name))
				{
					sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER_NAME, ExNeedToChangeName.REASON_INVALID, _name));
					return;
				}
				else if(CharacterDAO.getInstance().getPlayersCountByName(_name) >= 1)
				{
					sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER_NAME, ExNeedToChangeName.REASON_EXISTS, _name));
					return;
				}

				CharSelectInfo csi = ArrayUtils.valid(client.getCharacters(), client.getSelectedIndex());
				if(csi == null)
					return;

				canReName = !Util.isMatchingRegexp(csi.getName(), Config.CNAME_TEMPLATE) || CharacterDAO.getInstance().getPlayersCountByName(csi.getName()) > 1 || NpcNameLineHolder.getInstance().isBlackListContainsName(csi.getName());
				if(!canReName)
					return;

				csi.setName(_name);

				Player pPlayer = GameObjectsStorage.getPlayer(csi.getObjectId());
				if(pPlayer != null)
					pPlayer.reName(_name, false);

				CharacterDAO.getInstance().updateName(csi.getObjectId(), _name);

				client.playerSelected(client.getSelectedIndex());
				break;
			case ExNeedToChangeName.TYPE_CLAN_NAME:
				Player player = client.getActiveChar();
				if(player == null)
					return;
				Clan clan = player.getClan();
				if(clan == null || clan.getLeaderId(Clan.SUBUNIT_MAIN_CLAN) != player.getObjectId())
					return;

				String name = clan.getUnitName(Clan.SUBUNIT_MAIN_CLAN);
				if(!Util.isMatchingRegexp(_name, Config.CLAN_NAME_TEMPLATE))
				{
					sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_CLAN_NAME, ExNeedToChangeName.REASON_INVALID, _name));
					return;
				}
				else if(ClanTable.getInstance().getClanByName(_name) != null)
				{
					sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_CLAN_NAME, ExNeedToChangeName.REASON_EXISTS, _name));
					return;
				}

				canReName = !Util.isMatchingRegexp(name, Config.CLAN_NAME_TEMPLATE) || ClanTable.getInstance().getClansSizeByName(name) > 1;
				if(!canReName)
					return;

				SubUnit subUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
				subUnit.setName(_name, true);

				List<L2GameServerPacket> packets = clan.listAll();
				for(Player cm : clan.getOnlineMembers(0))
				{
					cm.sendPacket(PledgeShowMemberListDeleteAll.STATIC);
					cm.sendPacket(packets);
				}
				break;
		}
	}
}