package org.mmocore.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.commons.data.xml.AbstractParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.TeleportLocation;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.reward.RewardData;
import org.mmocore.gameserver.model.reward.RewardGroup;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.model.reward.RewardType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.npc.AbsorbInfo;
import org.mmocore.gameserver.templates.npc.Faction;
import org.mmocore.gameserver.templates.npc.MinionData;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 16:16/14.12.2010
 */
public class NpcParser extends AbstractDirParser<NpcHolder>
{
	private static final NpcParser _instance = new NpcParser();

	public static NpcParser getInstance()
	{
		return _instance;
	}

	protected NpcParser()
	{
		super(NpcHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/npc/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return f.getPath().contains("custom");
	}

	@Override
	public String getDTDFileName()
	{
		return "npc.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> npcIterator = rootElement.elementIterator(); npcIterator.hasNext(); )
		{
			Element npcElement = npcIterator.next();
			int npcId = Integer.parseInt(npcElement.attributeValue("id"));
			int templateId = npcElement.attributeValue("template_id") == null ? 0 : Integer.parseInt(npcElement.attributeValue("id"));
			String name = npcElement.attributeValue("name");
			String title = npcElement.attributeValue("title");

			StatsSet set = new StatsSet();
			set.set("npcId", npcId);
			set.set("displayId", templateId);
			set.set("name", name);
			set.set("title", title);
			set.set("baseCpReg", 0);
			set.set("baseCpMax", 0);

			for(Iterator<Element> firstIterator = npcElement.elementIterator(); firstIterator.hasNext(); )
			{
				Element firstElement = firstIterator.next();
				if(firstElement.getName().equalsIgnoreCase("set"))
				{
					set.set(firstElement.attributeValue("name"), firstElement.attributeValue("value"));
				}
				else if(firstElement.getName().equalsIgnoreCase("equip"))
				{
					for(Iterator<Element> eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
					{
						Element eElement = eIterator.next();
						set.set(eElement.getName(), eElement.attributeValue("item_id"));
					}
				}
				else if(firstElement.getName().equalsIgnoreCase("ai_params"))
				{
					StatsSet ai = new StatsSet();
					for(Iterator<Element> eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
					{
						Element eElement = eIterator.next();
						ai.set(eElement.attributeValue("name"), eElement.attributeValue("value"));
					}
					set.set("aiParams", ai);
				}
				else if(firstElement.getName().equalsIgnoreCase("attributes"))
				{
					int[] attributeAttack = new int[6];
					int[] attributeDefence = new int[6];
					for(Iterator<Element> eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
					{
						// DS: name collision
						Element eElement = eIterator.next();
						org.mmocore.gameserver.model.base.Element element;
						if(eElement.getName().equalsIgnoreCase("defence"))
						{
							element = org.mmocore.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
							attributeDefence[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
						}
						else if(eElement.getName().equalsIgnoreCase("attack"))
						{
							element = org.mmocore.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
							attributeAttack[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
						}
					}

					set.set("baseAttributeAttack", attributeAttack);
					set.set("baseAttributeDefence", attributeDefence);
				}
			}

			NpcTemplate template = new NpcTemplate(set);

			for(Iterator<Element> secondIterator = npcElement.elementIterator(); secondIterator.hasNext(); )
			{
				Element secondElement = secondIterator.next();
				String nodeName = secondElement.getName();
				if(nodeName.equalsIgnoreCase("faction"))
				{
					String factionId = secondElement.attributeValue("name");
					Faction faction = new Faction(factionId);
					int factionRange = Integer.parseInt(secondElement.attributeValue("range"));
					faction.setRange(factionRange);
					for(Iterator<Element> nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
					{
						final Element nextElement = nextIterator.next();
						int ignoreId = Integer.parseInt(nextElement.attributeValue("npc_id"));
						faction.addIgnoreNpcId(ignoreId);
					}
					template.setFaction(faction);
				}
				else if(nodeName.equalsIgnoreCase("rewardlist"))
					template.addRewardList(parseRewardList(this, secondElement, String.valueOf(npcId)));
				else if(nodeName.equalsIgnoreCase("skills"))
				{
					for(Iterator<Element> nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));

						// Для определения расы используется скилл 4416
						if(id == 4416)
						{
							template.setRace(level);
						}

						SkillEntry skill = SkillTable.getInstance().getSkillEntry(id, level);

						//TODO
						//if(skill == null || skill.getSkillType() == L2Skill.SkillType.NOTDONE)
						//	unimpl.add(Integer.valueOf(skillId));
						if(skill == null)
						{
							continue;
						}

						// DS: временная затычка для статов самонов - не добавляем пассивки
						// TODO: разобраться со статами и выкинуть
						if (template.isInstanceOf(Servitor.class) && id >= 4408 && id <= 4413)
							continue;
							
						template.addSkill(skill);
					}
				}
				else if(nodeName.equalsIgnoreCase("minions"))
				{
					for(Iterator<Element> nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("npc_id"));
						int count = Integer.parseInt(nextElement.attributeValue("count"));

						template.addMinion(new MinionData(id, count));
					}
				}
				else if(nodeName.equalsIgnoreCase("teach_classes"))
				{
					for(Iterator<Element> nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
					{
						Element nextElement = nextIterator.next();

						int id = Integer.parseInt(nextElement.attributeValue("id"));

						template.addTeachInfo(ClassId.VALUES[id]);
					}
				}
				else if(nodeName.equalsIgnoreCase("absorblist"))
				{
					for(Iterator<Element> nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
					{
						Element nextElement = nextIterator.next();

						int chance = Integer.parseInt(nextElement.attributeValue("chance"));
						int cursedChance = nextElement.attributeValue("cursed_chance") == null ? 0 : Integer.parseInt(nextElement.attributeValue("cursed_chance"));
						int minLevel = Integer.parseInt(nextElement.attributeValue("min_level"));
						int maxLevel = Integer.parseInt(nextElement.attributeValue("max_level"));
						boolean skill = nextElement.attributeValue("skill") != null && Boolean.parseBoolean(nextElement.attributeValue("skill"));
						AbsorbInfo.AbsorbType absorbType = AbsorbInfo.AbsorbType.valueOf(nextElement.attributeValue("type"));

						template.addAbsorbInfo(new AbsorbInfo(skill, absorbType, chance, cursedChance, minLevel, maxLevel));
					}
				}
				else if(nodeName.equalsIgnoreCase("teleportlist"))
				{
					for(Iterator<Element> sublistIterator = secondElement.elementIterator(); sublistIterator.hasNext(); )
					{
						Element subListElement = sublistIterator.next();
						int id = Integer.parseInt(subListElement.attributeValue("id"));
						List<TeleportLocation> list = new ArrayList<TeleportLocation>();
						for(Iterator<Element> targetIterator = subListElement.elementIterator(); targetIterator.hasNext(); )
						{
							Element targetElement = targetIterator.next();
							int itemId = Integer.parseInt(targetElement.attributeValue("item_id", "57"));
							long price = Integer.parseInt(targetElement.attributeValue("price"));
							int npcStringId = Integer.parseInt(targetElement.attributeValue("name"));
							int castleId = Integer.parseInt(targetElement.attributeValue("castle_id", "0"));
							TeleportLocation loc = new TeleportLocation(itemId, price, npcStringId, castleId);
							loc.set(Location.parseLoc(targetElement.attributeValue("loc")));
							list.add(loc);
						}
						template.addTeleportList(id, list.toArray(new TeleportLocation[list.size()]));
					}
				}
			}

			addTemplate(template);
		}
	}

	protected void addTemplate(NpcTemplate template)
	{
		getHolder().addTemplate(template);
	}

	public static RewardList parseRewardList(AbstractParser<?> parser, Element element, String debugString)
	{
		RewardType type = RewardType.valueOf(element.attributeValue("type"));
		boolean autoLoot = element.attributeValue("auto_loot") != null && Boolean.parseBoolean(element.attributeValue("auto_loot"));
		RewardList list = new RewardList(type, autoLoot);

		for(Iterator<Element> nextIterator = element.elementIterator(); nextIterator.hasNext(); )
		{
			final Element nextElement = nextIterator.next();
			final String nextName = nextElement.getName();
			boolean notGroupType = type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED || type == RewardType.EVENT;
			if(nextName.equalsIgnoreCase("group"))
			{
				double enterChance = nextElement.attributeValue("chance") == null ? RewardList.MAX_CHANCE : Double.parseDouble(nextElement.attributeValue("chance")) * 10000;

				RewardGroup group = notGroupType ? null : new RewardGroup(enterChance);
				for(Iterator<Element> rewardIterator = nextElement.elementIterator(); rewardIterator.hasNext(); )
				{
					Element rewardElement = rewardIterator.next();
					RewardData data = parseReward(rewardElement);
					if(notGroupType)
						parser.warn("Can't load rewardlist from group: " + debugString + "; type: " + type);
					else
						group.addData(data);
				}

				if(group != null)
					list.add(group);
			}
			else if(nextName.equalsIgnoreCase("reward"))
			{
				if(!notGroupType)
				{
					parser.warn("Reward can't be without group(and not grouped): " + debugString + "; type: " + type);
					continue;
				}

				RewardData data = parseReward(nextElement);
				RewardGroup g = new RewardGroup(RewardList.MAX_CHANCE);
				g.addData(data);
				list.add(g);
			}
		}

		if(type == RewardType.RATED_GROUPED || type == RewardType.NOT_RATED_GROUPED)
			if(!list.validate())
				parser.warn("Problems with rewardlist: " + debugString + "; type: " + type);

		return list;
	}

	private static RewardData parseReward(org.dom4j.Element rewardElement)
	{
		int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
		RewardData data = new RewardData(itemId);

		long min = Long.parseLong(rewardElement.attributeValue("min"));
		long max = Long.parseLong(rewardElement.attributeValue("max"));

		if(data.getItem().isCommonItem())
		{
			min *= Config.RATE_DROP_COMMON_ITEMS;
			max *= Config.RATE_DROP_COMMON_ITEMS;
		}

		// переводим в системный вид
		int chance = (int) (Double.parseDouble(rewardElement.attributeValue("chance")) * 10000);
		data.setChance(chance);

		data.setNotRate((data.getItem().isArrow() // стрелы не рейтуются
				|| (Config.NO_RATE_EQUIPMENT && data.getItem().isEquipment()) // отключаемая рейтовка эквипа
				|| (Config.NO_RATE_KEY_MATERIAL && data.getItem().isKeyMatherial()) // отключаемая рейтовка ключевых материалов
				|| (Config.NO_RATE_RECIPES && data.getItem().isRecipe())), // отключаемая рейтовка рецептов
				ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId)); // индивидуальная отключаемая однопроходная рейтовка для списка предметов

		data.setMinDrop(min);
		data.setMaxDrop(max);

		return data;
	}
}
