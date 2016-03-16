package org.mmocore.gameserver.data.client.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.client.holder.NpcNameLineHolder;
import org.mmocore.gameserver.templates.client.NpcNameLine;
import org.mmocore.gameserver.utils.Language;

public class NpcNameLineParser extends AbstractDirParser<NpcNameLineHolder>
{
	private static final NpcNameLineParser _instance = new NpcNameLineParser();

	public static NpcNameLineParser getInstance()
	{
		return _instance;
	}

	private NpcNameLineParser()
	{
		super(NpcNameLineHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/client");
	}

	@Override
	public String getDTDFileName()
	{
		return "npcname.dtd";
	}

	@Override
	public boolean isIgnored(File f)
	{
		return !f.getName().startsWith("NpcName");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Language lang = Language.valueOf(rootElement.attributeValue("lang"));
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element dataElement = iterator.next();

			int npcId = Integer.parseInt(dataElement.elementText("npc_id"));
			String name = dataElement.elementText("name");
			String title = dataElement.elementText("description");
			long color = Long.parseLong(dataElement.elementText("color"));

			NpcNameLine line = new NpcNameLine(lang, npcId, name, title, color);
			getHolder().put(lang, line);
		}
	}
}
