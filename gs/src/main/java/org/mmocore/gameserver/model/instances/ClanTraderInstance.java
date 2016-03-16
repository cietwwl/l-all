package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

public final class ClanTraderInstance extends NpcInstance
{
	public ClanTraderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		HtmlMessage html = new HtmlMessage(this);

		if(command.equalsIgnoreCase("crp"))
		{
			if(player.getClan() != null && player.getClan().getLevel() > 4)
				html.setFile("default/" + getNpcId() + "-2.htm");
			else
				html.setFile("default/" + getNpcId() + "-1.htm");

			player.sendPacket(html);
		}
		else if(command.startsWith("exchange"))
		{
			if(!player.isClanLeader())
			{
				html.setFile("default/" + getNpcId() + "-no.htm");
				player.sendPacket(html);
				return;
			}

			int itemId = Integer.parseInt(command.substring(9).trim());

			int reputation = 0;
			long itemCount = 0;

			switch(itemId)
			{
				case 9911: // Blood alliance
					reputation = 500;
					itemCount = 1;
					break;
				case 9910: // 10 Blood oath
					reputation = 200;
					itemCount = 10;
					break;
				case 9912: // 100 Knight's Epaulettes
					reputation = 20;
					itemCount = 100;
					break;
			}

			if(ItemFunctions.deleteItem(player, itemId, itemCount))
			{
				player.getClan().incReputation(reputation, false, "ClanTrader " + itemId + " from " + player.getName());
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				player.sendPacket(new SystemMessage(SystemMsg.YOUR_CLAN_HAS_ADDED_S1_POINTS_TO_ITS_CLAN_REPUTATION_SCORE).addNumber(reputation));

				html.setFile("default/" + getNpcId() + "-ExchangeSuccess.htm");
			}
			else
				html.setFile("default/" + getNpcId() + "-ExchangeFailed.htm");

			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
}