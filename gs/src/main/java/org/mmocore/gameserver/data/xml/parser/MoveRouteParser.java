package org.mmocore.gameserver.data.xml.parser;

import java.io.File;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.MoveRouteHolder;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.templates.moveroute.MoveNode;
import org.mmocore.gameserver.templates.moveroute.MoveRoute;
import org.mmocore.gameserver.templates.moveroute.MoveRouteType;

/**
 * @author VISTALL
 * @date 10:28/25.10.2011
 */
public class MoveRouteParser extends AbstractDirParser<MoveRouteHolder>
{
	private static MoveRouteParser _instance = new MoveRouteParser();

	public static MoveRouteParser getInstance()
	{
		return _instance;
	}

	private MoveRouteParser()
	{
		super(MoveRouteHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "./data/moveroute");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "moveroute.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Element e : rootElement.elements())
		{
			String name = e.attributeValue("name");
			MoveRouteType type = MoveRouteType.valueOf(e.attributeValue("type"));
			String running = e.attributeValue("is_running");

			MoveRoute moveRoute = new MoveRoute(name, type, running != null ? Boolean.parseBoolean(running) : false);
			getHolder().addRoute(moveRoute);

			for(Element nodeElement : e.elements())
			{
				int x = Integer.parseInt(nodeElement.attributeValue("x"));
				int y = Integer.parseInt(nodeElement.attributeValue("y"));
				int z = Integer.parseInt(nodeElement.attributeValue("z"));
				int socialId = Integer.parseInt(nodeElement.attributeValue("social", "0"));
				long delay = Long.parseLong(nodeElement.attributeValue("delay", "0"));
				NpcString npcString = null;
				ChatType chatType = null;
				String npcString0 = nodeElement.attributeValue("npc_string");
				if(npcString0 != null)
				{
					npcString = NpcString.valueOf(npcString0);
					chatType = ChatType.valueOf(nodeElement.attributeValue("chat_type"));
				}

				MoveNode node = new MoveNode(x, y, z, npcString, socialId, delay, chatType);
				moveRoute.getNodes().add(node);
			}
		}
	}
}