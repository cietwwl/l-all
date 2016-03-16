package org.mmocore.gameserver.data.client.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.client.holder.NpcStringLineHolder;
import org.mmocore.gameserver.utils.Language;

public class NpcStringLineParser extends AbstractDirParser<NpcStringLineHolder>
{
	private static final NpcStringLineParser _instance = new NpcStringLineParser();

	public static NpcStringLineParser getInstance()
	{
		return _instance;
	}

	private NpcStringLineParser()
	{
		super(NpcStringLineHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/client");
	}

	@Override
	public String getDTDFileName()
	{
		return "npcstring.dtd";
	}

	@Override
	public boolean isIgnored(File f)
	{
		return !f.getName().startsWith("NpcString");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Language lang = Language.valueOf(rootElement.attributeValue("lang"));
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element dataElement = iterator.next();

			int id = Integer.parseInt(dataElement.elementText("id"));
			String value = dataElement.elementText("text");

			getHolder().put(lang, id, value);
		}
	}
}
