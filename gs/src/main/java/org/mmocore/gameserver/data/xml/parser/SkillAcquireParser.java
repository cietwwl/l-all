package org.mmocore.gameserver.data.xml.parser;

import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.model.SkillLearn;

/**
 * @author VISTALL
 * @date  20:55/30.11.2010
 */
public final class SkillAcquireParser extends AbstractDirParser<SkillAcquireHolder>
{
	private static final SkillAcquireParser _instance = new SkillAcquireParser();

	public static SkillAcquireParser getInstance()
	{
		return _instance;
	}

	protected SkillAcquireParser()
	{
		super(SkillAcquireHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
	}

	@Override
	public boolean isIgnored(File b)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "tree.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("certification_skill_tree"); iterator.hasNext();)
			getHolder().addAllCertificationLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("sub_unit_skill_tree"); iterator.hasNext();)
			getHolder().addAllSubUnitLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("pledge_skill_tree"); iterator.hasNext();)
			getHolder().addAllPledgeLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("collection_skill_tree"); iterator.hasNext();)
			getHolder().addAllCollectionLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("fishing_skill_tree"); iterator.hasNext();)
			getHolder().addAllFishingLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("fishing_non_dwarf_skill_tree"); iterator.hasNext();)
			getHolder().addAllFishingNonDwarfLearns(parseSkillLearn((Element) iterator.next()));


		for(Iterator<Element> iterator = rootElement.elementIterator("transfer_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllTransferLearns(classId, learns);
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("normal_skill_tree"); iterator.hasNext();)
		{
			TIntObjectHashMap<List<SkillLearn>> map = new TIntObjectHashMap<List<SkillLearn>>();
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);

				map.put(classId, learns);
			}

			getHolder().addAllNormalSkillLearns(map);
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("transformation_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("race"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllTransformationLearns(race, learns);
			}
		}
	}

	private List<SkillLearn> parseSkillLearn(Element tree)
	{
		List<SkillLearn> skillLearns = new ArrayList<SkillLearn>();
		for(Iterator<Element> iterator = tree.elementIterator("skill"); iterator.hasNext();)
		{
			Element element = iterator.next();
			String value;

			int id = Integer.parseInt(element.attributeValue("id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			value = element.attributeValue("cost");
			int cost = value == null ? 0 : Integer.parseInt(value);
			int min_level = Integer.parseInt(element.attributeValue("min_level"));
			value = element.attributeValue("item_id");
			int item_id = value == null ? 0 : Integer.parseInt(value);
			value = element.attributeValue("item_count");
			long item_count = value == null ? 1 : Long.parseLong(value);
			value = element.attributeValue("clicked");
			boolean clicked = value != null && Boolean.parseBoolean(value);
			value = element.attributeValue("auto_learn");
			Boolean autoLearn = value == null ? null : Boolean.parseBoolean(value);

			skillLearns.add(new SkillLearn(id, level, min_level, cost, item_id, item_count, clicked, autoLearn));
		}

		return skillLearns;
	}
}
