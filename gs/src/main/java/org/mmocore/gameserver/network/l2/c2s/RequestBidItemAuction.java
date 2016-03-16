package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.instancemanager.itemauction.ItemAuction;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;

/**
 * @author n0nam3
 */
public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_bid < 0 || _bid > activeChar.getAdena())
			return;

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		NpcInstance broker = activeChar.getLastNpc();
		if(broker == null || broker.getNpcId() != _instanceId || !activeChar.isInRangeZ(broker, Creature.INTERACTION_DISTANCE))
			return;
		if(instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if(auction != null)
				auction.registerBid(activeChar, _bid);
		}
	}
}