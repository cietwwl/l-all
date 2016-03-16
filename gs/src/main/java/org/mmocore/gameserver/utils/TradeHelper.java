package org.mmocore.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.commons.math.SafeMath;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.items.TradeItem;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.item.ItemTemplate;

public final class TradeHelper
{
	private TradeHelper()
	{}

	public static boolean checksIfCanOpenStore(Player player, int storeType)
	{
		if(player.getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
		{
			player.sendMessage(new CustomMessage("trade.NotHavePermission").addNumber(Config.SERVICES_TRADE_MIN_LEVEL));
			return false;
		}

		String tradeBan = player.getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			player.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP);
			return false;
		}

		String BLOCK_ZONE = storeType == Player.STORE_PRIVATE_MANUFACTURE ? Zone.BLOCKED_ACTION_PRIVATE_WORKSHOP : Zone.BLOCKED_ACTION_PRIVATE_STORE;
		if(player.isActionBlocked(BLOCK_ZONE))
			if(!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && player.isInOfflineMode())
			{
				player.sendPacket(storeType == Player.STORE_PRIVATE_MANUFACTURE ? SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE : SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
				return false;
			}

		if(player.isCastingNow())
		{
			player.sendPacket(SystemMsg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			return false;
		}

		if(player.isInCombat())
		{
			player.sendPacket(SystemMsg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(player.isActionsDisabled() || player.isMounted() || player.isInOlympiadMode() || player.isInDuel() || player.isProcessingRequest())
			return false;

		if(Config.SERVICES_TRADE_ONLY_FAR)
		{
			boolean tradenear = false;
			for(Player p : World.getAroundPlayers(player, Config.SERVICES_TRADE_RADIUS, 200))
				if(p.isInStoreMode())
				{
					tradenear = true;
					break;
				}

			if(World.getAroundNpc(player, Config.SERVICES_TRADE_RADIUS + 100, 200).size() > 0)
				tradenear = true;

			if(tradenear)
			{
				player.sendMessage(new CustomMessage("trade.OtherTradersNear"));
				return false;
			}
		}

		return true;
	}

	/**
	 * Проверка торговца на валидность магазина
	 *
	 * @param player торговец
	 * @return true, если магазин валидный
	 */
	public static boolean validateStore(Player player)
	{
		return validateStore(player, 0);
	}

	/**
	 * Проверка торговца на валидность магазина
	 *
	 * @param player торговец
	 * @param adena зарезервированное количество адены, например цена за оффлайн торговлю
	 * @return true, если магазин валидный
	 */
	public static boolean validateStore(Player player, long adena)
	{
		if(player.isDead())
			return false;

		if(player.getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
			return false;

		String tradeBan = player.getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			return false;

		String BLOCK_ZONE = player.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE ? Zone.BLOCKED_ACTION_PRIVATE_WORKSHOP : Zone.BLOCKED_ACTION_PRIVATE_STORE;
		if(player.isActionBlocked(BLOCK_ZONE))
			if(!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && player.isInOfflineMode())
				return false;

		switch (player.getPrivateStoreType())
		{
			case Player.STORE_PRIVATE_BUY:
				return validateBuyStore(player, adena);
			case Player.STORE_PRIVATE_SELL:
			case Player.STORE_PRIVATE_SELL_PACKAGE:
				return true; // TODO
			case Player.STORE_PRIVATE_MANUFACTURE:
				return true; // TODO
		}

		if(Config.SERVICES_TRADE_ONLY_FAR)
		{
			for(Creature c : World.getAroundCharacters(player, Config.SERVICES_TRADE_RADIUS, 200))
			{
				if(c.isNpc())
					return false;
				if(!c.isPlayer())
					continue;
				Player p = c.getPlayer();
				if(p.isInStoreMode())
					return false;
			}
		}

		return false;
	}

	public static boolean validateBuyStore(Player player, long adena)
	{
		List<TradeItem> buyList = player.getBuyList();
		if(buyList.isEmpty())
			return false;

		if(buyList.size() > player.getTradeLimit())
			return false;

		long totalCost = adena;
		int slots = 0;
		long weight = 0;

		try
		{
			ItemTemplate template;
			for (TradeItem item : buyList)
			{
				template = item.getItem();
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(item.getCount(), item.getOwnersPrice()));
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), template.getWeight()));
				if(!template.isStackable() || player.getInventory().getItemByItemId(item.getItemId()) == null)
					slots++;
			}
		}
		catch (ArithmeticException ae)
		{
			return false;
		}

		if(totalCost > player.getAdena())
			return false;

		if(!player.getInventory().validateWeight(weight))
			return false;

		if(!player.getInventory().validateCapacity(slots))
			return false;

		return true;
	}

	public final static void purchaseItem(Player buyer, Player seller, TradeItem item)
	{
		long price = item.getCount() * item.getOwnersPrice();
		if(!item.getItem().isStackable())
		{
			if(item.getEnchantLevel() > 0)
			{
				seller.sendPacket(new SystemMessage(SystemMsg.S2S3_HAS_BEEN_SOLD_TO_C1_AT_THE_PRICE_OF_S4_ADENA).addName(buyer).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
				buyer.sendPacket(new SystemMessage(SystemMsg.S2S3_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S4_ADENA).addName(seller).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
			}
			else
			{
				seller.sendPacket(new SystemMessage(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA).addName(buyer).addItemName(item.getItemId()).addNumber(price));
				buyer.sendPacket(new SystemMessage(SystemMsg.S2_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S3_ADENA).addName(seller).addItemName(item.getItemId()).addNumber(price));
			}
		}
		else
		{
			seller.sendPacket(new SystemMessage(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA).addName(buyer).addNumber(item.getCount()).addItemName(item.getItemId()).addNumber(price));
			buyer.sendPacket(new SystemMessage(SystemMsg.S3_S2_HAS_BEEN_PURCHASED_FROM_C1_FOR_S4_ADENA).addName(seller).addItemName(item.getItemId()).addNumber(item.getCount()).addNumber(price));
		}
	}

	public final static long getTax(Player seller, long price)
	{
		long tax = (long) (price * Config.SERVICES_TRADE_TAX / 100);
		if(seller.isInZone(Zone.ZoneType.offshore))
			tax = (long) (price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100);
		if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !seller.isInOfflineMode())
			tax = 0;
		if(Config.SERVICES_PARNASSUS_NOTAX && seller.getReflection() == ReflectionManager.PARNASSUS)
			tax = 0;

		return tax;
	}

	/**
	 * Отключение режима торговли у персонажа, оф. трейдеров кикает.
	 */
	public static void cancelStore(Player activeChar)
	{
		activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		if(activeChar.isInOfflineMode())
		{
			activeChar.setOfflineMode(false);
			activeChar.kick();
		}
		else
			activeChar.broadcastCharInfo();
	}

	public static int restoreOfflineTraders() throws Exception
	{
		int count = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			//Убираем просроченных
			if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			{
				int expireTimeSecs = (int) (System.currentTimeMillis() / 1000L - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);

				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
				statement.setInt(1, expireTimeSecs);
				statement.executeUpdate();

				DbUtils.close(statement);
			}

			//Убираем забаненных
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();

			DbUtils.close(statement);

			statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
			rset = statement.executeQuery();

			int objectId;
			int expireTimeSecs;
			Player p;

			while(rset.next())
			{
				objectId = rset.getInt("obj_id");
				expireTimeSecs = rset.getInt("value");

				p = Player.restore(objectId);
				if(p == null)
					continue;

				//радиус не проверяется, т.к. торговец еще не заспавнен в мир
				if(!validateStore(p))
				{
					p.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					p.setOfflineMode(false);
					p.kick();
					continue;
				}

				p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
				p.setOfflineMode(true);
				p.setOnlineStatus(true);
				p.entering = false;

				p.spawnMe();

				if(p.getClan() != null && p.getClan().getAnyMember(p.getObjectId()) != null)
					p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);

				if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
					p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + expireTimeSecs  - System.currentTimeMillis() / 1000L) * 1000L);

				count++;
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return count;
	}
}