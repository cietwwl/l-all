package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.RecipeHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Recipe;
import org.mmocore.gameserver.model.RecipeComponent;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.ManufactureItem;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.RecipeShopItemInfo;
import org.mmocore.gameserver.network.l2.s2c.StatusUpdate;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.TradeHelper;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;

	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
		_price = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isFishing())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInRangeZ(buyer, Creature.INTERACTION_DISTANCE))
		{
			buyer.sendActionFailed();
			return;
		}

		Recipe recipeList = null;
		for(ManufactureItem mi : manufacturer.getCreateList())
			if(mi.getRecipeId() == _recipeId)
				if(_price == mi.getCost())
				{
					recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
					break;
				}

		if(recipeList == null)
		{
			buyer.sendActionFailed();
			return;
		}

		int success = 0;

		if(!manufacturer.findRecipe(_recipeId))
		{
			buyer.sendActionFailed();
			return;
		}

		if(manufacturer.getCurrentMp() < recipeList.getMpCost())
		{
			manufacturer.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			buyer.sendPacket(SystemMsg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
			return;
		}

		buyer.getInventory().writeLock();
		try
		{
			if(buyer.getAdena() < _price)
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			RecipeComponent[] recipes = recipeList.getRecipes();

			for(RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				ItemInstance item = buyer.getInventory().getItemByItemId(recipe.getItemId());

				if(item == null || recipe.getQuantity() > item.getCount())
				{
					buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
					return;
				}
			}

			if(!buyer.reduceAdena(_price, false))
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			for(RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					buyer.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity());
					//TODO audit
					buyer.sendPacket(SystemMessage.removeItems(recipe.getItemId(), recipe.getQuantity()));
				}

			long tax = TradeHelper.getTax(manufacturer, _price);
			long price = _price;
			if(tax > 0)
			{
				price -= tax;
				manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax").addNumber(tax));
			}

			manufacturer.addAdena(price);
		}
		finally
		{
			buyer.getInventory().writeUnlock();
		}

		manufacturer.reduceCurrentMp(recipeList.getMpCost(), null);
		manufacturer.sendStatusUpdate(false, false, StatusUpdate.CUR_MP);

		int tryCount = 1, successCount = 0;
		if(Rnd.chance(Config.CRAFT_DOUBLECRAFT_CHANCE))
			tryCount++;

		for(int i = 0; i < tryCount; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				int itemId = recipeList.getFoundation() != 0 ? Rnd.chance(Config.CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId() : recipeList.getItemId();
				long count = recipeList.getCount();
				ItemFunctions.addItem(buyer, itemId, count);
				success = 1;
				successCount++;
			}

		SystemMessage sm;
		if(successCount == 0)
		{
			sm = new SystemMessage(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			sm.addName(manufacturer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			sm.addName(buyer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);

		}
		else if(recipeList.getCount() > 1 || successCount > 1)
		{
			sm = new SystemMessage(SystemMsg.C1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
			sm.addName(manufacturer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(recipeList.getCount() * successCount);
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA);
			sm.addName(buyer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(recipeList.getCount() * successCount);
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);

		}
		else
		{
			sm = new SystemMessage(SystemMsg.C1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
			sm.addName(manufacturer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA);
			sm.addName(buyer);
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);
		}

		buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
		buyer.sendChanges();
	}
}