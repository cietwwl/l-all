package org.mmocore.gameserver.data.client.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.client.holder.ItemNameLineHolder;
import org.mmocore.gameserver.templates.client.ItemNameLine;
import org.mmocore.gameserver.utils.Language;

/**
 * @author VISTALL
 * @date 20:42/02.09.2011
 */
public class ItemNameLineParser extends AbstractDirParser<ItemNameLineHolder>
{
	private static final ItemNameLineParser _instance = new ItemNameLineParser();

	public static ItemNameLineParser getInstance()
	{
		return _instance;
	}

	private ItemNameLineParser()
	{
		super(ItemNameLineHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/client");
	}

	@Override
	public String getDTDFileName()
	{
		return "itemname.dtd";
	}

	@Override
	public boolean isIgnored(File f)
	{
		return !f.getName().startsWith("ItemName");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Language lang = Language.valueOf(rootElement.attributeValue("lang"));
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element dataElement = iterator.next();

			int itemId = Integer.parseInt(dataElement.elementText("item_id"));
			String name = dataElement.elementText("name");
			int color = Integer.parseInt(dataElement.elementText("color"));

			ItemNameLine line = new ItemNameLine(lang, itemId, name, color);
			getHolder().put(lang, line);
		}
	}
}
