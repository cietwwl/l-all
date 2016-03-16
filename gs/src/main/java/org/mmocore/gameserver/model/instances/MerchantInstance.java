package org.mmocore.gameserver.model.instances;

import java.util.StringTokenizer;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.data.xml.holder.BuyListHolder;
import org.mmocore.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import org.mmocore.gameserver.data.xml.holder.MultiSellHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.instancemanager.MapRegionManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.network.l2.c2s.L2GameClientPacket;
import org.mmocore.gameserver.network.l2.c2s.RequestBuyItem;
import org.mmocore.gameserver.network.l2.c2s.RequestExRefundItem;
import org.mmocore.gameserver.network.l2.c2s.RequestPreviewItem;
import org.mmocore.gameserver.network.l2.c2s.RequestSellItem;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExBuySellList;
import org.mmocore.gameserver.network.l2.s2c.ExGetPremiumItemList;
import org.mmocore.gameserver.network.l2.s2c.ShopPreviewList;
import org.mmocore.gameserver.templates.mapregion.DomainArea;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerchantInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(MerchantInstance.class);

	public MerchantInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + pom + ".htm";

		String temp = "merchant/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "teleporter/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "petmanager/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		return "default/" + pom + ".htm";
	}

	private void showWearWindow(Player player, int val)
	{
		NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);

		if(list != null)
		{
			ShopPreviewList bl = new ShopPreviewList(list, player);
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}

	protected void showShopWindow(Player player, int listId, boolean tax)
	{
		double taxRate = 0;

		if(tax)
		{
			Castle castle = getCastle(player);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		NpcTradeList list = BuyListHolder.getInstance().getBuyList(listId);
		if(list == null || list.getNpcId() == getNpcId())
			player.sendPacket(new ExBuySellList.BuyList(list, player, taxRate), new ExBuySellList.SellRefundList(player, false));
		else
		{
			_log.warn("[L2MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}

	protected void showShopWindow(Player player)
	{
		showShopWindow(player, 0, false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if(actualCommand.equalsIgnoreCase("Buy") || actualCommand.equalsIgnoreCase("Sell"))
		{
			int val = 0;
			if(st.countTokens() > 0)
				val = Integer.parseInt(st.nextToken());
			showShopWindow(player, val, true);
		}
		else if(actualCommand.equalsIgnoreCase("Wear"))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("Multisell"))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			Castle castle = getCastle(player);
			MultiSellHolder.getInstance().SeparateAndSend(val, player, getObjectId(), castle != null ? castle.getTaxRate() : 0);
		}
		else if(actualCommand.equalsIgnoreCase("ReceivePremium"))
		{
			if(player.getPremiumItemList().isEmpty())
			{
				player.sendPacket(SystemMsg.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
				return;
			}

			player.sendPacket(new ExGetPremiumItemList(player));
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public Castle getCastle(Player player)
	{
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX || (getReflection() == ReflectionManager.PARNASSUS && Config.SERVICES_PARNASSUS_NOTAX))
			return null;
		if(getReflection() == ReflectionManager.GIRAN_HARBOR || getReflection() == ReflectionManager.PARNASSUS)
		{
			String var = player.getVar("backCoords");
			if(var != null && !var.isEmpty())
			{
				Location loc = Location.parseLoc(var);

				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, loc);
				if(domain != null)
					return ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
			}

			return super.getCastle();
		}
		return super.getCastle(player);
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBuyItem.class || packet == RequestSellItem.class || packet == RequestExRefundItem.class || packet == RequestPreviewItem.class || super.canPassPacket(player, packet, arg);
	}
}