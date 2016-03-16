package org.mmocore.gameserver.network.l2.c2s;

import java.util.List;

import org.mmocore.commons.math.SafeMath;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.TradeItem;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SendTradeDone;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.network.l2.s2c.TradePressOtherOk;
import org.mmocore.gameserver.utils.Log;
import org.mmocore.gameserver.utils.Log.ItemLog;


/**
 * Вызывается при нажатии кнопки OK в окне обмена.
 */
public class TradeDone extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		Player parthner1 = getClient().getActiveChar();
		if(parthner1 == null)
			return;

		Request request = parthner1.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.TRADE))
		{
			parthner1.sendActionFailed();
			return;
		}

		if(!request.isInProgress())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner1.isOutOfControl())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner1.isInStoreMode())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			parthner1.sendActionFailed();
			return;
		}

		Player parthner2 = request.getOtherPlayer(parthner1);
		if(parthner2 == null)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner2.isInStoreMode())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner2.getRequest() != request)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if(_response == 0)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner2.sendPacket(SendTradeDone.FAIL, new SystemMessage(SystemMsg.C1_HAS_CANCELLED_THE_TRADE).addName(parthner1));
			return;
		}

		if(!parthner1.isInRangeZ(parthner2, Creature.INTERACTION_DISTANCE))
		{
			request.cancel();
			SystemMessage msg = new SystemMessage(SystemMsg.C1_HAS_CANCELLED_THE_TRADE).addName(parthner1);
			parthner1.sendPacket(SendTradeDone.FAIL, msg);
			parthner2.sendPacket(SendTradeDone.FAIL, msg);
			parthner1.sendActionFailed();
			parthner2.sendActionFailed();
			return;
		}

		// first party accepted the trade
		// notify clients that "OK" button has been pressed.
		request.confirm(parthner1);
		parthner2.sendPacket(new SystemMessage(SystemMsg.C1_HAS_CONFIRMED_THE_TRADE).addName(parthner1), TradePressOtherOk.STATIC);

		if(!request.isConfirmed(parthner2)) // Check for dual confirmation
		{
			parthner1.sendActionFailed();
			return;
		}

		List<TradeItem> tradeList1 = parthner1.getTradeList();
		List<TradeItem> tradeList2 = parthner2.getTradeList();
		int slots = 0;
		long weight = 0;
		boolean success = false;

		parthner1.getInventory().writeLock();
		parthner2.getInventory().writeLock();
		try
		{
			slots = 0;
			weight = 0;

			for(TradeItem ti : tradeList1)
			{
				ItemInstance item = parthner1.getInventory().getItemByObjectId(ti.getObjectId());
				if(item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner1))
					return;

				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
				if(!ti.getItem().isStackable() || parthner2.getInventory().getItemByItemId(ti.getItemId()) == null)
					slots++;
			}

			if(!parthner2.getInventory().validateWeight(weight))
			{
				parthner2.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if(!parthner2.getInventory().validateCapacity(slots))
			{
				parthner2.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			slots = 0;
			weight = 0;

			for(TradeItem ti : tradeList2)
			{
				ItemInstance item = parthner2.getInventory().getItemByObjectId(ti.getObjectId());
				if(item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner2))
					return;

				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
				if(!ti.getItem().isStackable() || parthner1.getInventory().getItemByItemId(ti.getItemId()) == null)
					slots++;
			}

			if(!parthner1.getInventory().validateWeight(weight))
			{
				parthner1.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if(!parthner1.getInventory().validateCapacity(slots))
			{
				parthner1.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			for(TradeItem ti : tradeList1)
			{
				ItemInstance item = parthner1.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
				Log.LogItem(parthner1, ItemLog.TradeSell, item);
				Log.LogItem(parthner2, ItemLog.TradeBuy, item);
				parthner2.getInventory().addItem(item);
			}

			for(TradeItem ti : tradeList2)
			{
				ItemInstance item = parthner2.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
				Log.LogItem(parthner2, ItemLog.TradeSell, item);
				Log.LogItem(parthner1, ItemLog.TradeBuy, item);
				parthner1.getInventory().addItem(item);
			}

			parthner1.sendPacket(SystemMsg.YOUR_TRADE_WAS_SUCCESSFUL);
			parthner2.sendPacket(SystemMsg.YOUR_TRADE_WAS_SUCCESSFUL);

			success = true;
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			return;
		}
		finally
		{
			parthner2.getInventory().writeUnlock();
			parthner1.getInventory().writeUnlock();

			request.done();

			parthner1.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
			parthner2.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
		}
	}
}