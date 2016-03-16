package org.mmocore.gameserver.data.xml.parser;

import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractFileParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.CubicHolder;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.CubicTemplate;

/**
 * @author VISTALL
 * @date  15:24/22.12.2010
 */
public final class CubicParser extends AbstractFileParser<CubicHolder>
{
	private static CubicParser _instance = new CubicParser();

	public static CubicParser getInstance()
	{
		return _instance;
	}

	protected CubicParser()
	{
		super(CubicHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/cubics.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "cubics.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		String val;
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element cubicElement = (Element) iterator.next();
			int id = Integer.parseInt(cubicElement.attributeValue("id"));
			int level = Integer.parseInt(cubicElement.attributeValue("level"));
			int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
			int maxCount = Integer.parseInt(cubicElement.attributeValue("max_count"));
			CubicTemplate template = new CubicTemplate(id, level, delay);
			getHolder().addCubicTemplate(template);

			// skills
			for(Iterator<Element> skillsIterator = cubicElement.elementIterator(); skillsIterator.hasNext();)
			{
				Element skillsElement = (Element)skillsIterator.next();
				int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
				List<CubicTemplate.SkillInfo> skills = new ArrayList<CubicTemplate.SkillInfo>(1);
				// skill
				for(Iterator<Element> skillIterator = skillsElement.elementIterator(); skillIterator.hasNext();)
				{
					Element skillElement = (Element)skillIterator.next();
					int id2 = Integer.parseInt(skillElement.attributeValue("id"));
					int level2 = Integer.parseInt(skillElement.attributeValue("level"));
					val = skillElement.attributeValue("chance");
					int chance2 = val == null ? 0 : Integer.parseInt(val);
					boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
					val = skillElement.attributeValue("min_hp");
					int minHp = val == null ? 0 : Integer.parseInt(val);
					val = skillElement.attributeValue("min_hp_per");
					int minHpPer = val == null ? 0 : Integer.parseInt(val);
					CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));

					TIntIntHashMap set = new TIntIntHashMap();
					for(Iterator<Element> chanceIterator = skillElement.elementIterator(); chanceIterator.hasNext();)
					{
						Element chanceElement = (Element)chanceIterator.next();
						int min = Integer.parseInt(chanceElement.attributeValue("min"));
						int max = Integer.parseInt(chanceElement.attributeValue("max"));
						int value = Integer.parseInt(chanceElement.attributeValue("value"));
						for(int i = min; i <= max; i++)
							set.put(i,value);
					}

					if(chance2 == 0 && set.isEmpty())
					{
						warn("Wrong skill chance. Cubic: " + id + "/" + level);
					}
					SkillEntry skill = SkillTable.getInstance().getSkillEntry(id2, level2);
					if(skill != null)
					{
						skill.getTemplate().setCubicSkill(true);
						skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, minHp, minHpPer, maxCount, set));
					}
				}

				template.putSkills(chance, skills);
			}
		}
	}
}
