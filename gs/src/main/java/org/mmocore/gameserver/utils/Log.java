package org.mmocore.gameserver.utils;


import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
	private static final Logger _log = LoggerFactory.getLogger(Log.class);

	private static final Logger _logChat = LoggerFactory.getLogger("chat");
	private static final Logger _logGm = LoggerFactory.getLogger("gmactions");
	private static final Logger _logItems = LoggerFactory.getLogger("item");
	private static final Logger _logGame = LoggerFactory.getLogger("game");
	private static final Logger _logDebug = LoggerFactory.getLogger("debug");

	public static enum ItemLog
	{
		Create,
		Delete,
		Drop,
		PvPDrop,
		Crystalize,
		EnchantFail,
		Pickup,
		PetPickup,
		PartyPickup,
		PrivateStoreBuy,
		PrivateStoreSell,
		RecipeShopBuy, //TODO
		RecipeShopSell, //TODO
		CraftCreate, //TODO
		CraftDelete, //TODO
		TradeBuy,
		TradeSell,
		FromPet,
		ToPet,
		PostRecieve,
		PostSend,
		PostCancel,
		PostExpire,
		PostPrice,
		RefundSell, //TODO
		RefundReturn, //TODO
		WarehouseDeposit,
		WarehouseWithdraw,
		FreightWithdraw,
		FreightDeposit,
		ClanWarehouseDeposit,
		ClanWarehouseWithdraw,
		ExtractCreate, //TODO
		ExtractDelete, //TODO
		NpcBuy, //TODO
		NpcCreate, //TODO
		NpcDelete, //TODO
		MultiSellIngredient,
		MultiSellProduct,
		QuestCreate, //TODO
		QuestDelete, //TODO
		EventCreate, //TODO
		EventDelete //TODO
	}

	public static final String Create = "Create";
	public static final String Delete = "Delete";
	public static final String Drop = "Drop";
	public static final String PvPDrop = "PvPDrop";
	public static final String Crystalize = "Crystalize";
	public static final String EnchantFail = "EnchantFail";
	public static final String Pickup = "Pickup";
	public static final String PetPickup = "PetPickup";
	public static final String PartyPickup = "PartyPickup";
	public static final String PrivateStoreBuy = "PrivateStoreBuy";
	public static final String PrivateStoreSell = "PrivateStoreSell";
	public static final String TradeBuy = "TradeBuy";
	public static final String TradeSell = "TradeSell";
	public static final String FromPet = "FromPet";
	public static final String ToPet = "ToPet";
	public static final String PostRecieve = "PostRecieve";
	public static final String PostSend = "PostSend";
	public static final String PostCancel = "PostCancel";
	public static final String PostExpire = "PostExpire";
	public static final String PostPrice = "PostPrice";
	public static final String RefundSell = "RefundSell";
	public static final String RefundReturn = "RefundReturn";
	public static final String WarehouseDeposit = "WarehouseDeposit";
	public static final String WarehouseWithdraw = "WarehouseWithdraw";
	public static final String FreightWithdraw = "FreightWithdraw";
	public static final String FreightDeposit = "FreightDeposit";
	public static final String ClanWarehouseDeposit = "ClanWarehouseDeposit";
	public static final String ClanWarehouseWithdraw = "ClanWarehouseWithdraw";
	public static final String MultiSellIngredient = "MultiSellIngredient";
	public static final String MultiSellProduct = "MultiSellProduct";

	public static final String ClanChangeLeaderRequestAdd = "ClanChangeLeaderRequestAdd";
	public static final String ClanChangeLeaderRequestDone = "ClanChangeLeaderRequestDone";
	public static final String ClanChangeLeaderRequestCancel = "ClanChangeLeaderRequestCancel";

	public static void add(String text, String cat, Player player)
	{
		StringBuilder output = new StringBuilder();

		output.append(cat);
		if(player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(' ');
		output.append(text);

		_logGame.info(output.toString());
	}

	public static void add(String text, String cat)
	{
		add(text, cat, null);
	}

	public static void debug(String text)
	{
		_logDebug.debug(text);
	}

	public static void debug(String text, Throwable t)
	{
		_logDebug.debug(text, t);
	}

	public static void LogChat(String type, String player, String target, String text, int identifier)
	{
		if(!Config.LOG_CHAT)
			return;

		StringBuilder output = new StringBuilder();
		output.append(type);
		if(identifier > 0)
		{
			output.append(' ');
			output.append(identifier);
		}
		output.append(' ');
		output.append('[');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);

		_logChat.info(output.toString());
	}

	public static void LogCommand(Player player, GameObject target, String command, boolean success)
	{
		//if(!Config.LOG_GM)
		//	return;

		StringBuilder output = new StringBuilder();

		if(success)
			output.append("SUCCESS");
		else
			output.append("FAIL   ");

		output.append(' ');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(' ');
		output.append(command);

		_logGm.info(output.toString());
	}

	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item)
	{
		LogItem(activeChar, logType, item, item.getItemId(), item.getCount(), 0L, 0);
	}

	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, 0L, 0);
	}

	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count, long price)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, price, 0);
	}

	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count, long price, int paramId)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, price, paramId);
	}

	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count)
	{
		LogItem(activeChar, logType, null, itemId, count, 0L, 0);
	}

	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count, long price)
	{
		LogItem(activeChar, logType, null, itemId, count, price, 0);
	}

	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count, long price, int paramId)
	{
		LogItem(activeChar, logType, null, itemId, count, price, paramId);
	}

	private static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, int itemId, long count, long price, int paramId)
	{
		//if(!Config.LOG_ITEM)
		//	return;

		StringBuilder sb = new StringBuilder();
		// Process
		sb.append(logType);
		sb.append(' ');
		// Player
		sb.append(activeChar.getName());
		sb.append('[').append(activeChar.getObjectId()).append(']').append(' ');
		sb.append('(').append("IP: ").append(activeChar.getIP()).append(' ').append("Account: ").append(activeChar.getAccountName()).append(')').append(' ');
		sb.append('(').append("X: ").append(activeChar.getX()).append(' ').append("Y: ").append(activeChar.getY()).append(' ').append("Z: ").append(activeChar.getZ()).append(')');
		sb.append(' ');
		// Item
		sb.append(itemId);
		sb.append(' ');
		if(item != null)
		{
			if(item.getEnchantLevel() > 0)
			{
				sb.append('+');
				sb.append(item.getEnchantLevel());
				sb.append(' ');
			}
			sb.append(item.getTemplate().getName());
			if(!item.getTemplate().getAdditionalName().isEmpty())
			{
				sb.append(' ');
				sb.append('<').append(item.getTemplate().getAdditionalName()).append('>');
			}
			sb.append(' ');
			if(item.getAttributes().getValue() > 0)
			{
				sb.append('(');
				sb.append("Fire: ");
				sb.append(item.getAttributes().getFire());
				sb.append(' ');
				sb.append("Water: ");
				sb.append(item.getAttributes().getWater());
				sb.append(' ');
				sb.append("Wind: ");
				sb.append(item.getAttributes().getWind());
				sb.append(' ');
				sb.append("Earth: ");
				sb.append(item.getAttributes().getEarth());
				sb.append(' ');
				sb.append("Holy: ");
				sb.append(item.getAttributes().getHoly());
				sb.append(' ');
				sb.append("Unholy: ");
				sb.append(item.getAttributes().getUnholy());
				sb.append(')');
				sb.append(' ');
			}
			sb.append('(');
			sb.append(item.getCount());
			sb.append(')');
			sb.append('[');
			sb.append(item.getObjectId());
			sb.append(']');
		}
		else
		{
			ItemTemplate it = ItemHolder.getInstance().getTemplate(itemId);
			sb.append(it.getName());
			if(!it.getAdditionalName().isEmpty())
			{
				sb.append(' ');
				sb.append('<').append(it.getAdditionalName()).append('>');
			}
		}
		sb.append(' ');
		sb.append("Count: ").append(count);
		// Parameter
		switch(logType)
		{
			case CraftCreate:
			case CraftDelete:
				sb.append(' ');
				sb.append("Recipe: ").append(paramId);
				break;
			case PrivateStoreBuy:
			case PrivateStoreSell:
			case RecipeShopBuy:
			case RecipeShopSell:
				sb.append(' ');
				sb.append("Price: ").append(price);
				break;
			case MultiSellIngredient:
			case MultiSellProduct:
				sb.append(' ');
				sb.append("MultiSell: ").append(paramId);
				break;
			case NpcBuy:
				sb.append(' ');
				sb.append("BuyList: ").append(paramId);
				sb.append(' ');
				sb.append("Price: ").append(price);
				break;
			case NpcCreate:
			case NpcDelete:
				sb.append(' ');
				sb.append("NPC: ").append(paramId);
				break;
			case QuestCreate:
			case QuestDelete:
				sb.append(' ');
				sb.append("Quest: ").append(paramId);
				break;
			case EventCreate:
			case EventDelete:
				sb.append(' ');
				sb.append("Event: ").append(paramId);
				break;
		}

		_logItems.info(sb.toString());
	}

	public static void LogPetition(Player fromChar, Integer Petition_type, String Petition_text)
	{
		//TODO: implement
	}

	public static void LogAudit(Player player, String type, String msg)
	{
		//TODO: implement
	}
}