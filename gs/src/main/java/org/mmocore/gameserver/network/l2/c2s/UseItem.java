package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExUseSharedGroupItem;
import org.mmocore.gameserver.skills.TimeStamp;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int itemId = item.getItemId();

		if(activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(activeChar.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
		{
			activeChar.sendReuseMessage(item);
			return;
		}

		if(!item.getTemplate().testCondition(activeChar, item, true))
			return;

		if(activeChar.getInventory().isLockedItem(item))
			return;

		IBroadcastPacket result = activeChar.canUseItem(item, _ctrlPressed);
		if (result != null)
		{
			activeChar.sendPacket(result);
			return;
		}

		if(activeChar.isOutOfControl() || activeChar.isDead() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed())
		{
			activeChar.sendActionFailed();
			return;
		}

		boolean success = item.getTemplate().getHandler().useItem(activeChar, item, _ctrlPressed);
		if(success)
		{
			long nextTimeUse = item.getTemplate().getReuseType().next(item);
			if(nextTimeUse > System.currentTimeMillis())
			{
				TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
				activeChar.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);

				if(item.getTemplate().getReuseDelay() > 0)
					activeChar.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
			}
		}
	}
}