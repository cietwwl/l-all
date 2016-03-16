package org.mmocore.gameserver.network.l2.c2s;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.Util;

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int[] ITEM_IDS = new int[] { 13021, 13307 };

	private static final int COLORS[] =
	{
		0x9393FF,	// Pink
		0x7C49FC,	// Rose Pink
		0x97F8FC,	// Lemon Yellow
		0xFA9AEE,	// Lilac
		0xFF5D93,	// Cobalt Violet
		0x00FCA0,	// Mint Green
		0xA0A601,	// Peacock Green
		0x7898AF,	// Yellow Ochre
		0x486295,	// Chocolate
		0x999999	// Silver
	};

	private int _colorNum, _itemObjectId;
	private String _title;

	@Override
	protected void readImpl()
	{
		_colorNum = readD();
		_title = readS();
		_itemObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_colorNum < 0 || _colorNum >= COLORS.length)
			return;

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(item == null || !ArrayUtils.contains(ITEM_IDS, item.getItemId()))
			return;

		if(!_title.isEmpty() && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			activeChar.sendMessage(new CustomMessage("INCORRECT_TITLE"));
			return;
		}

		if(activeChar.getInventory().destroyItemByObjectId(_itemObjectId, 1))
		{
			activeChar.sendPacket(SystemMessage.removeItems(item.getItemId(), 1));
			activeChar.setTitleColor(COLORS[_colorNum]);
			activeChar.setTitle(_title);
			activeChar.broadcastUserInfo(true);
		}
	}
}