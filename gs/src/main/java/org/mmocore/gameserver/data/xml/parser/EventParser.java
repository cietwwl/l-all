package org.mmocore.gameserver.data.xml.parser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventAction;
import org.mmocore.gameserver.model.entity.events.actions.*;
import org.mmocore.gameserver.model.entity.events.objects.*;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.components.SysString;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.PlaySound;
import org.mmocore.gameserver.scripts.Scripts;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 12:56/10.12.2010
 */
public final class EventParser extends AbstractDirParser<EventHolder>
{
	private static final EventParser _instance = new EventParser();

	public static EventParser getInstance()
	{
		return _instance;
	}

	protected EventParser()
	{
		super(EventHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/events/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "events.dtd";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("event"); iterator.hasNext();)
		{
			Element eventElement = iterator.next();
			int id = Integer.parseInt(eventElement.attributeValue("id"));
			String name = eventElement.attributeValue("name");
			String impl = eventElement.attributeValue("impl");
			Class<Event> eventClass = null;
			try
			{
				eventClass = (Class<Event>) Class.forName("org.mmocore.gameserver.model.entity.events.impl." + impl + "Event");
			}
			catch(ClassNotFoundException e)
			{
				eventClass = (Class<Event>) Scripts.getInstance().getClasses().get("events."+ impl + "Event");
			}

			if(eventClass == null)
			{
				info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
				continue;
			}

			Constructor<Event> constructor = eventClass.getConstructor(MultiValueSet.class);

			MultiValueSet<String> set = new MultiValueSet<String>();
			set.set("id", id);
			set.set("name", name);

			for(Iterator<Element> parameterIterator = eventElement.elementIterator("parameter"); parameterIterator.hasNext();)
			{
				Element parameterElement = parameterIterator.next();
				set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
			}

			Event event = constructor.newInstance(set);

			event.addOnStartActions(parseActions(eventElement.element("on_start"), Integer.MAX_VALUE));
			event.addOnStopActions(parseActions(eventElement.element("on_stop"), Integer.MAX_VALUE));
			event.addOnInitActions(parseActions(eventElement.element("on_init"), Integer.MAX_VALUE));

			Element onTime = eventElement.element("on_time");
			if(onTime != null)
				for(Iterator<Element> onTimeIterator = onTime.elementIterator("on"); onTimeIterator.hasNext();)
				{
					Element on = onTimeIterator.next();
					int time = Integer.parseInt(on.attributeValue("time"));

					List<EventAction> actions = parseActions(on, time);

					event.addOnTimeActions(time, actions);
				}

			for(Iterator<Element> objectIterator = eventElement.elementIterator("objects"); objectIterator.hasNext();)
			{
				Element objectElement = objectIterator.next();
				String objectsName = objectElement.attributeValue("name");
				List<Object> objects = parseObjects(objectElement, id + ":" + name);

				event.addObjects(objectsName, objects);
			}


			getHolder().addEvent(event);
		}
	}

	private List<Object> parseObjects(Element element, String str)
	{
		if(element == null)
			return Collections.emptyList();

		List<Object> objects = new ArrayList<Object>(2);
		for(Iterator<Element> objectIterator = element.elementIterator(); objectIterator.hasNext();)
		{
			Element objectElement = objectIterator.next();
			final String nodeName = objectElement.getName();
			if(nodeName.equalsIgnoreCase("boat_point"))
				objects.add(BoatPoint.parse(objectElement));
			else if(nodeName.equalsIgnoreCase("point"))
				objects.add(Location.parse(objectElement));
			else if(nodeName.equalsIgnoreCase("spawn_ex"))
				objects.add(new SpawnExObject(objectElement.attributeValue("name")));
			else if(nodeName.equalsIgnoreCase("door"))
				objects.add(new DoorObject(Integer.parseInt(objectElement.attributeValue("id"))));
			else if(nodeName.equalsIgnoreCase("static_object"))
				objects.add(new StaticObjectObject(Integer.parseInt(objectElement.attributeValue("id"))));
			else if(nodeName.equalsIgnoreCase("spawn_npc"))
			{
				int id = Integer.parseInt(objectElement.attributeValue("id"));
				int x = Integer.parseInt(objectElement.attributeValue("x"));
				int y = Integer.parseInt(objectElement.attributeValue("y"));
				int z = Integer.parseInt(objectElement.attributeValue("z"));
				objects.add(new SpawnSimpleObject(id, new Location(x, y, z)));
			}
			else if(nodeName.equalsIgnoreCase("combat_flag"))
			{
				int x = Integer.parseInt(objectElement.attributeValue("x"));
				int y = Integer.parseInt(objectElement.attributeValue("y"));
				int z = Integer.parseInt(objectElement.attributeValue("z"));
				objects.add(new FortressCombatFlagObject(new Location(x, y, z)));
			}
			else if(nodeName.equalsIgnoreCase("territory_ward"))
			{
				int x = Integer.parseInt(objectElement.attributeValue("x"));
				int y = Integer.parseInt(objectElement.attributeValue("y"));
				int z = Integer.parseInt(objectElement.attributeValue("z"));
				int itemId = Integer.parseInt(objectElement.attributeValue("item_id"));
				int npcId = Integer.parseInt(objectElement.attributeValue("npc_id"));
				objects.add(new TerritoryWardObject(itemId, npcId, new Location(x, y, z)));
			}
			else if(nodeName.equalsIgnoreCase("siege_toggle_npc"))
			{
				int id = Integer.parseInt(objectElement.attributeValue("id"));
				int fakeId = Integer.parseInt(objectElement.attributeValue("fake_id"));
				int x = Integer.parseInt(objectElement.attributeValue("x"));
				int y = Integer.parseInt(objectElement.attributeValue("y"));
				int z = Integer.parseInt(objectElement.attributeValue("z"));
				int hp = Integer.parseInt(objectElement.attributeValue("hp"));
				Set<String> set = Collections.emptySet();
				for(Iterator<Element> oIterator = objectElement.elementIterator(); oIterator.hasNext();)
				{
					Element sub = oIterator.next();
					if(set.isEmpty())
						set = new HashSet<String>();
					set.add(sub.attributeValue("name"));
				}
				objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x, y, z), hp, set));
			}
			else if(nodeName.equalsIgnoreCase("castle_zone"))
			{
				long price = Long.parseLong(objectElement.attributeValue("price"));
				objects.add(new CastleDamageZoneObject(objectElement.attributeValue("name"), price));
			}
			else if(nodeName.equalsIgnoreCase("zone"))
			{
				objects.add(new ZoneObject(objectElement.attributeValue("name")));
			}
			else if(nodeName.equalsIgnoreCase("ctb_team"))
			{
				int mobId = Integer.parseInt(objectElement.attributeValue("mob_id"));
				int flagId = Integer.parseInt(objectElement.attributeValue("id"));
				Location loc = Location.parse(objectElement);

				objects.add(new CTBTeamObject(mobId, flagId, loc));
			}
			else if(nodeName.equalsIgnoreCase("rewardlist"))
				objects.add(NpcParser.parseRewardList(this, objectElement, str));
		}

		return objects;
	}

	private List<EventAction> parseActions(Element element, int time)
	{
		if(element == null)
			return Collections.emptyList();

		IfElseAction lastIf = null;
		List<EventAction> actions = new ArrayList<EventAction>(0);
		for(Iterator<Element> iterator = element.elementIterator(); iterator.hasNext();)
		{
			Element actionElement = iterator.next();
			if(actionElement.getName().equalsIgnoreCase("start"))
			{
				String name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, true);
				actions.add(startStopAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("stop"))
			{
				String name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, false);
				actions.add(startStopAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("spawn"))
			{
				String name = actionElement.attributeValue("name");
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, true);
				actions.add(spawnDespawnAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("despawn"))
			{
				String name = actionElement.attributeValue("name");
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, false);
				actions.add(spawnDespawnAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("respawn"))
			{
				String name = actionElement.attributeValue("name");
				RespawnAction respawnAction = new RespawnAction(name);
				actions.add(respawnAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("open"))
			{
				String name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(true, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("close"))
			{
				String name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(false, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("active"))
			{
				String name = actionElement.attributeValue("name");
				ActiveDeactiveAction a = new ActiveDeactiveAction(true, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("deactive"))
			{
				String name = actionElement.attributeValue("name");
				ActiveDeactiveAction a = new ActiveDeactiveAction(false, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("refresh"))
			{
				String name = actionElement.attributeValue("name");
				RefreshAction a = new RefreshAction(name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("init"))
			{
				String name = actionElement.attributeValue("name");
				InitAction a = new InitAction(name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("global_add_reward"))
			{
				String name = actionElement.attributeValue("name");
				actions.add(new GlobalRewardListAction(true, name));
			}
			else if(actionElement.getName().equalsIgnoreCase("global_remove_reward"))
			{
				String name = actionElement.attributeValue("name");
				actions.add(new GlobalRewardListAction(false, name));
			}
			else if(actionElement.getName().equalsIgnoreCase("npc_say"))
			{
				int npc = Integer.parseInt(actionElement.attributeValue("npc"));
				ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				NpcString string = NpcString.valueOf(actionElement.attributeValue("text"));
				NpcSayAction action = new NpcSayAction(npc, range, chat, string);
				actions.add(action);
			}
			else if(actionElement.getName().equalsIgnoreCase("play_sound"))
			{
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				String sound = actionElement.attributeValue("sound");
				PlaySound.Type type = PlaySound.Type.valueOf(actionElement.attributeValue("type"));

				PlaySoundAction action = new PlaySoundAction(range, sound, type);
				actions.add(action);
			}
			else if(actionElement.getName().equalsIgnoreCase("give_item"))
			{
				int itemId = Integer.parseInt(actionElement.attributeValue("id"));
				long count = Integer.parseInt(actionElement.attributeValue("count"));

				GiveItemAction action = new GiveItemAction(itemId, count);
				actions.add(action);
			}
			else if(actionElement.getName().equalsIgnoreCase("announce"))
			{
				String val = actionElement.attributeValue("val");
				if(val == null && time == Integer.MAX_VALUE)
				{
					info("Can't get announce time." + getCurrentFileName());
					continue;
				}

				int val2 = val == null ? time : Integer.parseInt(val);
				EventAction action = new AnnounceAction(val2);
				actions.add(action);
			}
			else if(actionElement.getName().equalsIgnoreCase("if"))
			{
				String name = actionElement.attributeValue("name");
				IfElseAction action = new IfElseAction(name, false);

				action.setIfList(parseActions(actionElement, time));
				actions.add(action);

				lastIf = action;
			}
			else if(actionElement.getName().equalsIgnoreCase("ifnot"))
			{
				String name = actionElement.attributeValue("name");
				IfElseAction action = new IfElseAction(name, true);

				action.setIfList(parseActions(actionElement, time));
				actions.add(action);

				lastIf = action;
			}
			else if(actionElement.getName().equalsIgnoreCase("else"))
			{
				if(lastIf == null)
					info("Not find <if> for <else> tag");
				else
					lastIf.setElseList(parseActions(actionElement, time));
			}
			else if(actionElement.getName().equalsIgnoreCase("say"))
			{
				ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range = Integer.parseInt(actionElement.attributeValue("range"));

				String how = actionElement.attributeValue("how");
				String text = actionElement.attributeValue("text");

				SysString sysString = SysString.valueOf2(how);

				SayAction sayAction = null;
				if(sysString != null)
					sayAction = new SayAction(range, chat, sysString, SystemMsg.valueOf(text));
				else
					sayAction = new SayAction(range, chat, how, NpcString.valueOf(text));

				actions.add(sayAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("teleport_players"))
			{
				String name = actionElement.attributeValue("id");
				TeleportPlayersAction a = new TeleportPlayersAction(name);
				actions.add(a);
			}
		}

		return actions.isEmpty() ? Collections.<EventAction> emptyList() : actions;
	}
}
