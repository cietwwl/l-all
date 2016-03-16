package org.mmocore.gameserver.model.instances;

import java.util.StringTokenizer;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.SevenSigns;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

public final class WyvernManagerInstance extends NpcInstance
{
	public WyvernManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();

		if(actualCommand.equalsIgnoreCase("RideHelp"))
		{
			HtmlMessage html = new HtmlMessage(this);
			html.setFile("wyvern/help_ride.htm");
			player.sendPacket(html);
			player.sendActionFailed();
		}
		else if(actualCommand.equalsIgnoreCase("RideWyvern"))
		{
			if(!validateCondition(player))
				return;
			if(!player.isRiding() || !PetDataTable.isStrider(player.getMountNpcId()))
			{
				HtmlMessage html = new HtmlMessage(this);
				html.setFile("wyvern/not_ready.htm");
				player.sendPacket(html);
			}
			else if(ItemFunctions.getItemCount(player, 1460) < 25)
			{
				HtmlMessage html = new HtmlMessage(this);
				html.setFile("wyvern/havenot_cry.htm");
				player.sendPacket(html);
			}
			else if(SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3)
			{
				HtmlMessage html = new HtmlMessage(this);
				html.setFile("wyvern/no_ride_dusk.htm");
				player.sendPacket(html);
			}
			else if(ItemFunctions.deleteItem(player, 1460, 25L))
			{
				player.setMount(PetDataTable.WYVERN_ID, player.getMountObjId(), player.getMountLevel(), player.getMountCurrentFed());
				HtmlMessage html = new HtmlMessage(this);
				html.setFile("wyvern/after_ride.htm");
				player.sendPacket(html);
			}
		}

		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(!validateCondition(player))
		{
			HtmlMessage html = new HtmlMessage(this);
			html.setFile("wyvern/lord_only.htm");
			player.sendPacket(html);
			player.sendActionFailed();
			return;
		}
		HtmlMessage html = new HtmlMessage(this);
		html.setFile("wyvern/lord_here.htm");
		html.replace("%Char_name%", String.valueOf(player.getName()));
		player.sendPacket(html);
		player.sendActionFailed();
	}

	private boolean validateCondition(Player player)
	{
		Residence residence = getCastle();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		residence = getFortress();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		residence = getClanHall();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		return false;
	}
}