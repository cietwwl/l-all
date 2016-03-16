package org.mmocore.gameserver.data.xml.parser;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.stats.StatTemplate;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.stats.conditions.*;
import org.mmocore.gameserver.stats.funcs.FuncTemplate;
import org.mmocore.gameserver.stats.triggers.TriggerInfo;
import org.mmocore.gameserver.stats.triggers.TriggerType;
import org.mmocore.gameserver.templates.item.ArmorTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;

/**
 * @author VISTALL
 * @date 13:48/10.01.2011
 */
public abstract class StatParser<H extends AbstractHolder> extends AbstractDirParser<H>
{
	protected StatParser(H holder)
	{
		super(holder);
	}

	protected Condition parseFirstCond(Element sub)
	{
		List<Element> e = sub.elements();
		if(e.isEmpty())
			return null;
		Element element = e.get(0);

		return parseCond(element);
	}

	protected Condition parseCond(Element element)
	{
		String name = element.getName();
		if(name.equalsIgnoreCase("and"))
			return parseLogicAnd(element);
		else if(name.equalsIgnoreCase("or"))
			return parseLogicOr(element);
		else if(name.equalsIgnoreCase("not"))
			return parseLogicNot(element);
		else if(name.equalsIgnoreCase("target"))
			return parseTargetCondition(element);
		else if(name.equalsIgnoreCase("player"))
			return parsePlayerCondition(element);
		else if(name.equalsIgnoreCase("using"))
			return parseUsingCondition(element);
		else if(name.equalsIgnoreCase("zone"))
			return parseZoneCondition(element);
		else if(name.equalsIgnoreCase("config"))
			return parseConfigCondition(element);

		return null;
	}

	protected Condition parseLogicAnd(Element n)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement));
		}

		if(cond._conditions == null || cond._conditions.length == 0)
			error("Empty <and> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicOr(Element n)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement));
		}

		if(cond._conditions == null || cond._conditions.length == 0)
			error("Empty <or> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicNot(Element n)
	{
		for(Element element : n.elements())
			return new ConditionLogicNot(parseCond(element));
		error("Empty <not> condition in " + getCurrentFileName());
		return null;
	}

	protected Condition parseTargetCondition(Element element)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("pvp"))
				cond = joinAnd(cond, new ConditionTargetPlayable(Boolean.valueOf(value)));
		}

		return cond;
	}

	protected Condition parseZoneCondition(Element element)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("type"))
				cond = joinAnd(cond, new ConditionZoneType(value));
		}

		return cond;
	}

	protected Condition parsePlayerCondition(Element element)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("residence"))
			{
				String[] st = value.split(";");
				cond = joinAnd(cond, new ConditionPlayerResidence(Integer.parseInt(st[1]), st[0]));
			}
			else if(name.equalsIgnoreCase("classId"))
				cond = joinAnd(cond, new ConditionPlayerClassId(value.split(",")));
			else if(name.equalsIgnoreCase("olympiad"))
				cond = joinAnd(cond, new ConditionPlayerOlympiad(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("instance_zone"))
				cond = joinAnd(cond, new ConditionPlayerInstanceZone(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("race"))
				cond = joinAnd(cond, new ConditionPlayerRace(value));
			else if(name.equalsIgnoreCase("minLevel"))
				cond = joinAnd(cond, new ConditionPlayerMinLevel(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("damage"))
			{
				String[] st = value.split(";");
				cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(Double.parseDouble(st[0]), Double.parseDouble(st[1])));
			}
			else if(name.equalsIgnoreCase("isMageClass"))
				cond = joinAnd(cond, new ConditionPlayerIsMageClass(Boolean.parseBoolean(value)));
			else if(name.equalsIgnoreCase("has_pet"))
				cond = joinAnd(cond, new ConditionPlayerHasPet(value.split(",")));
			else if(name.equalsIgnoreCase("mounted"))
				cond = joinAnd(cond, new ConditionPlayerMounted(value.split(",")));
			else if(name.equalsIgnoreCase("is_hero"))
				cond = joinAnd(cond, new ConditionPlayerIsHero(Boolean.parseBoolean(value)));
		}

		return cond;
	}

	protected Condition parseUsingCondition(Element element)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("slotitem"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if(st.hasMoreTokens())
					enchant = Integer.parseInt(st.nextToken().trim());
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if(name.equalsIgnoreCase("kind") || name.equalsIgnoreCase("weapon"))
			{
				long mask = 0;
				StringTokenizer st = new StringTokenizer(value, ",");
				tokens: while(st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for(WeaponTemplate.WeaponType wt : WeaponTemplate.WeaponType.VALUES)
						if(wt.toString().equalsIgnoreCase(item))
						{
							mask |= wt.mask();
							continue tokens;
						}
					for(ArmorTemplate.ArmorType at : ArmorTemplate.ArmorType.VALUES)
						if(at.toString().equalsIgnoreCase(item))
						{
							mask |= at.mask();
							continue tokens;
						}

					error("Invalid item kind: \"" + item + "\" in " + getCurrentFileName());
				}
				if(mask != 0)
					cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if(name.equalsIgnoreCase("skill"))
				cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(value)));
		}
		return cond;
	}

	protected Condition parseConfigCondition(Element element)
	{
		Condition cond = null;
		final Attribute name = element.attribute("name");
		final Attribute val = element.attribute("val");
		if (name != null && val != null)
		{
			Field field = FieldUtils.getField(Config.class, name.getValue());
			if (field != null)
			{
				if (field.getType() == boolean.class)
					cond = joinAnd(cond, new ConditionConfigBoolean(field, Boolean.valueOf(val.getValue())));				
			}
		}
		if(cond == null)
			_log.error("Unrecognized <config> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition joinAnd(Condition cond, Condition c)
	{
		if(cond == null)
			return c;
		if(cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}

	protected void parseFor(Element forElement, StatTemplate template)
	{
		for(Iterator<Element> iterator = forElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			final String elementName = element.getName();
			if(elementName.equalsIgnoreCase("add"))
				attachFunc(element, template, "Add");
			else if(elementName.equalsIgnoreCase("set"))
				attachFunc(element, template, "Set");
			else if(elementName.equalsIgnoreCase("sub"))
				attachFunc(element, template, "Sub");
			else if(elementName.equalsIgnoreCase("mul"))
				attachFunc(element, template, "Mul");
			else if(elementName.equalsIgnoreCase("div"))
				attachFunc(element, template, "Div");
			else if(elementName.equalsIgnoreCase("enchant"))
				attachFunc(element, template, "Enchant");
		}
	}

	protected void parseTriggers(Element f, StatTemplate triggerable)
	{
		for(Iterator<Element> iterator = f.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			int id = parseNumber(element.attributeValue("id")).intValue();
			int level = parseNumber(element.attributeValue("level")).intValue();
			TriggerType t = TriggerType.valueOf(element.attributeValue("type"));
			double chance = parseNumber(element.attributeValue("chance")).doubleValue();

			TriggerInfo trigger = new TriggerInfo(id, level, t, chance);

			triggerable.addTrigger(trigger);
			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();

				Condition condition = parseFirstCond(subElement);
				if(condition != null)
					trigger.addCondition(condition);
			}
		}
	}

	protected void attachFunc(Element n, StatTemplate template, String name)
	{
		Stats stat = Stats.valueOfXml(n.attributeValue("stat"));
		String order = n.attributeValue("order");
		int ord = parseNumber(order).intValue();
		Condition applyCond = parseFirstCond(n);
		double val = 0;
		if(n.attributeValue("value") != null)
			val = parseNumber(n.attributeValue("value")).doubleValue();

		template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
	}

	protected Number parseNumber(String value)
	{
		if(value.charAt(0) == '#')
			value = getTableValue(value).toString();
		try
		{
			if(value.indexOf('.') == -1)
			{
				int radix = 10;
				if(value.length() > 2 && value.substring(0, 2).equalsIgnoreCase("0x"))
				{
					value = value.substring(2);
					radix = 16;
				}
				return Integer.valueOf(value, radix);
			}
			return Double.valueOf(value);
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}

	protected abstract Object getTableValue(String name);
}
