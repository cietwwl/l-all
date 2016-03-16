package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.instancemanager.itemauction.ItemAuction;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.s2c.ExItemAuctionInfo;

/**
 * @author n0nam3
 */
public final class RequestInfoItemAuction extends L2GameClientPacket
{
	private int _instanceId;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
	}

	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.getAndSetLastItemAuctionRequest();

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if(instance == null)
			return;

		final ItemAuction auction = instance.getCurrentAuction();
		NpcInstance broker = activeChar.getLastNpc();
		if(auction == null || broker == null || broker.getNpcId() != _instanceId || !activeChar.isInRangeZ(broker, Creature.INTERACTION_DISTANCE))
			return;

		activeChar.sendPacket(new ExItemAuctionInfo(true, auction, instance.getNextAuction()));
	}
}