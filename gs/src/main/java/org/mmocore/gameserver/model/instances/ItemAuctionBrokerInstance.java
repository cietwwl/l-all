package org.mmocore.gameserver.model.instances;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuction;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.ExItemAuctionInfo;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.templates.npc.NpcTemplate;


/**
 * @author n0nam3
 */
public final class ItemAuctionBrokerInstance extends NpcInstance
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	private ItemAuctionInstance _instance;

	public ItemAuctionBrokerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, final int val, Object... arg)
	{
		String filename = val == 0 ? "itemauction/itembroker.htm" : "itemauction/itembroker-" + val + ".htm";
		player.sendPacket(new HtmlMessage(this, filename));
	}

	@Override
	public final void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		final String[] params = command.split(" ");
		if(params.length == 1)
			return;

		if(params[0].equals("auction"))
		{
			if(_instance == null)
			{
				_instance = ItemAuctionManager.getInstance().getManagerInstance(getTemplate().npcId);
				if(_instance == null)
					//_log.error("L2ItemAuctionBrokerInstance: Missing instance for: " + getTemplate().npcId);
					return;
			}

			if(params[1].equals("cancel"))
			{
				if(params.length == 3)
				{
					int auctionId = 0;

					try
					{
						auctionId = Integer.parseInt(params[2]);
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
						return;
					}

					final ItemAuction auction = _instance.getAuction(auctionId);
					if(auction != null)
						auction.cancelBid(player);
					else
						player.sendPacket(SystemMsg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU);
				}
				else
				{
					final ItemAuction[] auctions = _instance.getAuctionsByBidder(player.getObjectId());
					for(final ItemAuction auction : auctions)
						auction.cancelBid(player);
				}
			}
			else if(params[1].equals("show"))
			{
				final ItemAuction currentAuction = _instance.getCurrentAuction();
				final ItemAuction nextAuction = _instance.getNextAuction();

				if(currentAuction == null)
				{
					player.sendPacket(SystemMsg.IT_IS_NOT_AN_AUCTION_PERIOD);

					if(nextAuction != null)
						player.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + ".");
					return;
				}

				if(!player.getAndSetLastItemAuctionRequest())
				{
					player.sendPacket(SystemMsg.THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR);
					return;
				}

				player.sendPacket(new ExItemAuctionInfo(false, currentAuction, nextAuction));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}