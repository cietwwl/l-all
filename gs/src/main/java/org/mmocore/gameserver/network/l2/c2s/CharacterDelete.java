package org.mmocore.gameserver.network.l2.c2s;


import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.model.CharSelectInfo;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.CharacterDeleteFail;
import org.mmocore.gameserver.network.l2.s2c.CharacterDeleteSuccess;
import org.mmocore.gameserver.network.l2.s2c.CharacterSelectionInfo;
import org.mmocore.gameserver.tables.ClanTable;

public class CharacterDelete extends L2GameClientPacket
{
	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();
		if(activeChar != null)
			return;

		CharSelectInfo[] cs = client.getCharacters();
		if (_charSlot < 0 || _charSlot >= cs.length)
			return;

		CharSelectInfo csi = cs[_charSlot];
		if(csi == null)
			return;

		int charId = csi.getObjectId();
		if(charId <= 0)
			return;

		Player player = GameObjectsStorage.getPlayer(charId);
		if(player != null)
		{
			sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
			return;
		}

		if(csi.getClanId() > 0)
		{
			Clan clan = ClanTable.getInstance().getClan(csi.getClanId());

			if(clan.getLeaderId() == csi.getObjectId())
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			else
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));

			return;
		}

		if(Config.DELETE_DAYS == 0)
			CharacterDAO.getInstance().deleteCharByObjId(charId);
		else
			CharacterDAO.getInstance().markDeleteCharByObjId(charId, true);

		sendPacket(new CharacterDeleteSuccess());

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}