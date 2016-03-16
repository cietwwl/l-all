package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.TradeHelper;

public class Offline implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "offline" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return false;

		if(activeChar.isInObserverMode() || activeChar.getOlympiadGame() != null  || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return false;
		}

		if(!activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new HtmlMessage(0).setFile("command/offline_err01.htm"));
			return false;
		}

		if(activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			activeChar.sendPacket(new HtmlMessage(0).setFile("command/offline_err02.htm").replace("%level%", String.valueOf(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)));
			return false;
		}

		if(Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE && !activeChar.isInZone(Zone.ZoneType.offshore))
		{
			activeChar.sendPacket(new HtmlMessage(0).setFile("command/offline_err03.htm"));
			return false;
		}

		if(Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM != 0)
		{
			long adena = Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM == ItemTemplate.ITEM_ID_ADENA ? Config.SERVICES_OFFLINE_TRADE_PRICE : 0;
			if(!TradeHelper.validateStore(activeChar, adena) || !ItemFunctions.deleteItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE))
			{
				activeChar.sendPacket(new HtmlMessage(0).setFile("command/offline_err04.htm").replace("%item_id%", String.valueOf(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM)).replace("%item_count%", String.valueOf(Config.SERVICES_OFFLINE_TRADE_PRICE)));
				return false;
			}
		}

		activeChar.offline();
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}