package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
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

		int charId = client.getObjectIdByIndex(_charSlot);
		if(charId < 0)
			return;

		CharacterDAO.getInstance().markDeleteCharByObjId(charId, false);

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}