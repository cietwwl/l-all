package org.mmocore.gameserver.network.l2.c2s;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.handler.items.IItemHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExAutoSoulShot;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * format:		chdd
 * @param decrypt
 */
public class RequestAutoSoulShot extends L2GameClientPacket
{
	private int _itemId;
	private boolean _type; // 1 = on : 0 = off;

	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || activeChar.isDead())
			return;

		ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);

		if(item == null)
			return;

		if(_type)
		{
			if (ArrayUtils.contains(Servitor.BEAST_SHOTS, _itemId) && activeChar.getServitor() == null)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATICUSE_FUNCTION);
				return;
			}
			activeChar.addAutoSoulShot(_itemId);
			activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
			activeChar.sendPacket(new SystemMessage(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addItemName(_itemId));
			IItemHandler handler = item.getTemplate().getHandler();
			handler.useItem(activeChar, item, false);
			return;
		}

		activeChar.removeAutoSoulShot(_itemId);
		activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
		activeChar.sendPacket(new SystemMessage(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addItemName(_itemId));
	}
}