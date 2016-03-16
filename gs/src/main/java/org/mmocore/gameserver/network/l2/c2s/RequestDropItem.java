package org.mmocore.gameserver.network.l2.c2s;


import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.Log;
import org.mmocore.gameserver.utils.Log.ItemLog;

public class RequestDropItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;
	private Location _loc;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_loc = new Location(readD(), readD(), readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_count < 1 || _loc.isNull())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_DISCARDITEM)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestDropItem.Disallowed"));
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isSitting() || activeChar.isDropDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
			return;
		}

		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_DROP_ITEM))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE);
			return;
		}

		if(!activeChar.isInRangeSq(_loc, 22500) || Math.abs(_loc.z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DISCARD_SOMETHING_THAT_FAR_AWAY_FROM_YOU);
			return;
		}

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		synchronized (item)
		{
			if(!item.canBeDropped(activeChar, false))
			{
				activeChar.sendPacket(SystemMsg.THAT_ITEM_CANNOT_BE_DISCARDED);
				return;
			}

			if(!item.getTemplate().getHandler().dropItem(activeChar, item, _count, _loc))
				return;

			if(item.isEquipped())
			{
				activeChar.getInventory().unEquipItem(item);
				activeChar.sendUserInfo(true);
			}

			item = activeChar.getInventory().removeItemByObjectId(item.getObjectId(), _count);
			if(item == null)
			{
				//TODO audit
				activeChar.sendActionFailed();
				return;
			}

			Log.LogItem(activeChar, ItemLog.Drop, item);

			item.dropToTheGround(activeChar, _loc);
		}

		activeChar.disableDrop(1000);

		activeChar.sendChanges();
	}
}