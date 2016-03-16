package org.mmocore.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.data.xml.holder.OptionDataHolder;
import org.mmocore.gameserver.model.Skill.SkillType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.conditions.Condition;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.OptionDataTemplate;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.item.ArmorTemplate;
import org.mmocore.gameserver.templates.item.Bodypart;
import org.mmocore.gameserver.templates.item.EtcItemTemplate;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;

/**
 * @author VISTALL
 * @date 11:26/15.01.2011
 */
public final class ItemParser extends StatParser<ItemHolder>
{
	private static final ItemParser _instance = new ItemParser();

	public static ItemParser getInstance()
	{
		return _instance;
	}

	protected ItemParser()
	{
		super(ItemHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/items/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "item.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> itemIterator = rootElement.elementIterator(); itemIterator.hasNext();)
		{
			Element itemElement = itemIterator.next();
			StatsSet set = new StatsSet();
			set.set("item_id", itemElement.attributeValue("id"));
			set.set("name", itemElement.attributeValue("name"));
			set.set("add_name", itemElement.attributeValue("add_name", StringUtils.EMPTY));

			int slot = 0;
			for(Iterator<Element> subIterator = itemElement.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("set"))
				{
					set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
				}
				else if(subName.equalsIgnoreCase("equip"))
				{
					for(Iterator<Element> slotIterator = subElement.elementIterator(); slotIterator.hasNext();)
					{
						Element slotElement = slotIterator.next();
						Bodypart bodypart = Bodypart.valueOf(slotElement.attributeValue("id"));
						if(bodypart.getReal() != null)
							slot = bodypart.mask();
						else
							slot |= bodypart.mask();
					}
				}
			}

			set.set("bodypart", slot);

			ItemTemplate template = null;
			try
			{
				if(itemElement.getName().equalsIgnoreCase("weapon"))
				{
					if (!set.containsKey("class"))
					{
						if ((slot & ItemTemplate.SLOT_L_HAND) > 0) // щиты
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						else
							set.set("class", ItemTemplate.ItemClass.WEAPON);
					}
					template = new WeaponTemplate(set);
				}
				else if(itemElement.getName().equalsIgnoreCase("armor"))
				{
					if (!set.containsKey("class"))
					{
						if ((slot & ItemTemplate.SLOTS_ARMOR) > 0)
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						else if ((slot & ItemTemplate.SLOTS_JEWELRY) > 0)
							set.set("class", ItemTemplate.ItemClass.JEWELRY);
						else
							set.set("class", ItemTemplate.ItemClass.ACCESSORY);
					}
					template = new ArmorTemplate(set);
				}
				else //if(itemElement.getName().equalsIgnoreCase("etcitem"))
					template = new EtcItemTemplate(set);
			}
			catch(Exception e)
			{
				//for(Map.Entry<String, Object> entry : set.entrySet())
				//{
				//	info("set " + entry.getKey() + ":" + entry.getValue());
				//}
				warn("Fail create item: " + set.get("item_id"), e);
				continue;
			}

			for(Iterator<Element> subIterator = itemElement.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("for"))
				{
					parseFor(subElement, template);
				}
				else if(subName.equalsIgnoreCase("triggers"))
				{
					parseTriggers(subElement, template);
				}
				else if(subName.equalsIgnoreCase("skills"))
				{
					for(Iterator<Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						Element nextElement =  nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));

						SkillEntry skill = SkillTable.getInstance().getSkillEntry(id, level);

						if(skill != null)
						{
							template.attachSkill(skill);
							// Проверка на эксплойты с бесконечной распаковкой
							if (skill.getTemplate().getSkillType() == SkillType.EXTRACT && !ArrayUtils.contains(skill.getTemplate().getItemConsumeId(), template.getItemId()))
								info("Skill (" + id + "," + level + ") consume item(s), but attached item " + set.getObject("item_id") + "; file:" + getCurrentFileName() + " not found, possible infinite extraction !");
						}
						else
							info("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
					}
				}
				else if(subName.equalsIgnoreCase("enchant4_skill"))
				{
					int id = Integer.parseInt(subElement.attributeValue("id"));
					int level = Integer.parseInt(subElement.attributeValue("level"));

					SkillEntry skill = SkillTable.getInstance().getSkillEntry(id, level);
					if(skill != null)
						template.setEnchant4Skill(skill);
				}
				else if(subName.equalsIgnoreCase("cond"))
				{
					Condition condition = parseFirstCond(subElement);
					if(condition != null)
					{
						int msgId = parseNumber(subElement.attributeValue("msgId", "0")).intValue();
						condition.setSystemMsg(msgId);

						template.addCondition(condition);
					}
				}
				else if(subName.equalsIgnoreCase("attributes"))
				{
					int[] attributes = new int[6];
					for(Iterator<Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						// DS: name collision
						Element nextElement = nextIterator.next();
						org.mmocore.gameserver.model.base.Element element;
						if(nextElement.getName().equalsIgnoreCase("attribute"))
						{
							element = org.mmocore.gameserver.model.base.Element.getElementByName(nextElement.attributeValue("element"));
							attributes[element.getId()] = Integer.parseInt(nextElement.attributeValue("value"));
						}
					}
					template.setBaseAtributeElements(attributes);
				}
				else if(subName.equalsIgnoreCase("enchant_options"))
				{
					for(Iterator<Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						Element nextElement = nextIterator.next();

						if(nextElement.getName().equalsIgnoreCase("level"))
						{
							int val = Integer.parseInt(nextElement.attributeValue("val"));

							int i = 0;
							int[] options = new int[3];
							for(Element optionElement : nextElement.elements())
							{
								OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.attributeValue("id")));
								if(optionData == null)
								{
									error("Not found option_data for id: " + optionElement.attributeValue("id") + "; item_id: " + set.get("item_id"));
									continue;
								}
								options[i++] = optionData.getId();
							}
							template.addEnchantOptions(val, options);
						}
					}
				}
			}
			getHolder().addItem(template);
		}
	}

	@Override
	protected Object getTableValue(String name)
	{
		return null;
	}
}
