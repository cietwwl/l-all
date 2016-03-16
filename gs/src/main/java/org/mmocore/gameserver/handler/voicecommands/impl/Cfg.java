package org.mmocore.gameserver.handler.voicecommands.impl;


import org.apache.commons.lang3.math.NumberUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.s2c.ClientSetTime;
import org.mmocore.gameserver.utils.Util;

public class Cfg implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "cfg" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(command.equals("cfg"))
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("dli"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("DroplistIcons", "1", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("DroplistIcons");

					if(param[0].equalsIgnoreCase("noe"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("NoExp", "1", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("NoExp");

					if(param[0].equalsIgnoreCase(Player.NO_TRADERS_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowTraders(true);
							activeChar.setVar(Player.NO_TRADERS_VAR, "true", -1);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setNotShowTraders(false);
							activeChar.unsetVar(Player.NO_TRADERS_VAR);
						}

					if(param[0].equalsIgnoreCase("pegs"))
						if(param[1].equalsIgnoreCase("one"))
						{
							activeChar.unsetVar(Player.STOREMODE_VAR);
							activeChar.sendMessage(Util.getCfgDirect());
						}
						else if(param[1].equalsIgnoreCase("zero"))
						{
							activeChar.setDisableFogAndRain(true);
							activeChar.sendMessage(Util.packChar(activeChar, param));
						}

					if(param[0].equalsIgnoreCase(Player.ANIMATION_OF_CAST_RANGE_VAR))
					{
						int range = 15 * NumberUtils.toInt(param[1], 0);

						if (range < 0)
							range = -1;
						else if (range > 1500)
							range = 1500;

						activeChar.setBuffAnimRange(range);
						activeChar.setVar(Player.ANIMATION_OF_CAST_RANGE_VAR, String.valueOf(range), -1);
					}

					if(param[0].equalsIgnoreCase(Player.DISABLE_FOG_AND_RAIN))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setDisableFogAndRain(true);
							activeChar.sendPacket(new ClientSetTime(activeChar));
							activeChar.setVar(Player.DISABLE_FOG_AND_RAIN, "true", -1);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setDisableFogAndRain(false);
							activeChar.sendPacket(new ClientSetTime(activeChar));
							activeChar.unsetVar(Player.DISABLE_FOG_AND_RAIN);
						}
					}

					if(param[0].equalsIgnoreCase("noShift"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noShift", "1", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("noShift");

					if(Config.SERVICES_ENABLE_NO_CARRIER && param[0].equalsIgnoreCase("noCarrier"))
					{
						int time = NumberUtils.toInt(param[1], 0);

						if(time <= 0)
							time = 0;
						else if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
							time = Config.SERVICES_NO_CARRIER_MAX_TIME;
						else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
							time = Config.SERVICES_NO_CARRIER_MIN_TIME;

						activeChar.setVar("noCarrier", String.valueOf(time), -1);
					}

					if(param[0].equalsIgnoreCase("translit"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("translit", "tl", -1);
						else if(param[1].equalsIgnoreCase("la"))
							activeChar.setVar("translit", "tc", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("translit");

					if (Config.AUTO_LOOT_INDIVIDUAL)
					{
						if(param[0].equalsIgnoreCase("autoloot"))
							activeChar.setAutoLoot(NumberUtils.toInt(param[1], Player.AUTO_LOOT_NONE));

						if(param[0].equalsIgnoreCase("autolooth"))
							activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
					}
				}
			}

		HtmlMessage dialog = new HtmlMessage(5);
		dialog.setFile("command/cfg.htm");

		dialog.replace("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
		dialog.replace("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
		dialog.replace("%notraders%", activeChar.getVarB(Player.NO_TRADERS_VAR) ? "On" : "Off");
		dialog.replace("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");
		dialog.replace("%noCarrier%", Config.SERVICES_ENABLE_NO_CARRIER ? (activeChar.getVarB("noCarrier") ? activeChar.getVar("noCarrier") : "0") : "N/A");
		dialog.replace("%disableFogAndRain%", activeChar.getVarB(Player.DISABLE_FOG_AND_RAIN) ? "On" : "Off");

		if (activeChar.buffAnimRange() < 0)
			dialog.replace("%buffAnimRange%", "Off");
		else if (activeChar.buffAnimRange() == 0)
		{
			if (activeChar.isLangRus())
				dialog.replace("%buffAnimRange%", "Свои");
			else
				dialog.replace("%buffAnimRange%", "Self");
		}
		else
			dialog.replace("%buffAnimRange%", String.valueOf(activeChar.buffAnimRange() / 15));

		String tl = activeChar.getVar("translit");
		if(tl == null)
			dialog.replace("%translit%", "Off");
		else if(tl.equals("tl"))
			dialog.replace("%translit%", "On");
		else
			dialog.replace("%translit%", "Lt");

		if (Config.AUTO_LOOT_INDIVIDUAL)
		{
			switch (activeChar.isAutoLootEnabled())
			{
				case Player.AUTO_LOOT_ALL:
					dialog.replace("%autoLoot%", "On");
					break;
				case Player.AUTO_LOOT_ALL_EXCEPT_ARROWS:
					if (activeChar.isLangRus())
						dialog.replace("%autoLoot%", "Кроме стрел");
					else
						dialog.replace("%autoLoot%", "Except Arrows");
					break;
				default:
					dialog.replace("%autoLoot%", "Off");
					break;
			}
			dialog.replace("%autoLootH%", activeChar.isAutoLootHerbsEnabled() ? "On" : "Off");
		}
		else
		{
			dialog.replace("%autoLoot%", "N/A");
			dialog.replace("%autoLootH%", "N/A");
		}

		StringBuilder events = new StringBuilder();
		for(Event e : activeChar.getEvents())
			events.append(e.toString()).append("<br>");
		
		dialog.replace("%events%", events.toString());

		activeChar.sendPacket(dialog);

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}