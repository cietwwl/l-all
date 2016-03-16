package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

public class RequestPetGetItem extends L2GameClientPacket
{
	// format: cd
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		Servitor servitor = activeChar.getServitor();
		if(servitor == null || !servitor.isPet() || servitor.isDead() || servitor.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance item = (ItemInstance) activeChar.getVisibleObject(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!ItemFunctions.checkIfCanPickup(servitor, item))
		{
			SystemMessage sm;
			if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			{
				sm = new SystemMessage(SystemMsg.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
				sm.addNumber(item.getCount());
			}
			else
			{
				sm = new SystemMessage(SystemMsg.YOU_HAVE_FAILED_TO_PICK_UP_S1);
				sm.addItemName(item.getItemId());
			}
			sendPacket(sm);
			activeChar.sendActionFailed();
			return;
		}

		servitor.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
	}
}