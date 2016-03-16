package org.mmocore.gameserver.data.xml;

import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.data.client.parser.ItemNameLineParser;
import org.mmocore.gameserver.data.client.parser.NpcNameLineParser;
import org.mmocore.gameserver.data.client.parser.NpcStringLineParser;
import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.data.xml.holder.BuyListHolder;
import org.mmocore.gameserver.data.xml.holder.ProductHolder;
import org.mmocore.gameserver.data.xml.holder.RecipeHolder;
import org.mmocore.gameserver.data.xml.parser.*;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.tables.SkillTable;

/**
 * @author VISTALL
 * @date  20:55/30.11.2010
 */
public abstract class Parsers
{
	public static void parseAll()
	{
		// cache
		HtmCache.getInstance().reload();
		StringHolder.getInstance().load();
		// client
		ItemNameLineParser.getInstance().load();
		NpcNameLineParser.getInstance().load();
		NpcStringLineParser.getInstance().load();
		//
		SkillTable.getInstance().load(); // - SkillParser.getInstance();
		OptionDataParser.getInstance().load();
		ItemParser.getInstance().load();
		//
		NpcParser.getInstance().load();
		MoveRouteParser.getInstance().load();

		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();

		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();

		ReflectionManager.getInstance();
		//
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		//
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		// support(cubic & agathion)
		CubicParser.getInstance().load();
		//
		BuyListHolder.getInstance();
		RecipeHolder.getInstance();
		MultiSellParser.getInstance().load();
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		// item support
		AugmentationDataParser.getInstance().load();
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		FishDataParser.getInstance().load();

		// etc
		PetitionGroupParser.getInstance().load();
	}
}
