package org.mmocore.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractDirParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.data.xml.holder.MultiSellHolder;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.templates.multisell.MultiSellEntry;
import org.mmocore.gameserver.templates.multisell.MultiSellIngredient;
import org.mmocore.gameserver.templates.multisell.MultiSellListContainer;

/**
 * @author VISTALL
 * @date 15:42/01.08.2011
 */
public class MultiSellParser extends AbstractDirParser<MultiSellHolder>
{
	private static final MultiSellParser _instance = new MultiSellParser();

	public static MultiSellParser getInstance()
	{
		return _instance;
	}

	protected MultiSellParser()
	{
		super(MultiSellHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/multisell");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "multisell.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int id = Integer.parseInt(_currentFile.replace(".xml", ""));
		MultiSellListContainer list = new MultiSellListContainer();

		int entryId = 0;
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			if("config".equalsIgnoreCase(element.getName()))
			{
				String line = element.attributeValue("showall");
				if (line != null)
					list.setShowAll(Boolean.parseBoolean(line));
				line = element.attributeValue("notax");
				if (line != null)
					list.setNoTax(Boolean.parseBoolean(line));
				line = element.attributeValue("keepenchanted");
				if (line != null)
					list.setKeepEnchant(Boolean.parseBoolean(line));
				line = element.attributeValue("nokey");
				if (line != null)
					list.setNoKey(Boolean.parseBoolean(line));
				line = element.attributeValue("bbsallowed");
				if (line != null)
					list.setBBSAllowed(Boolean.parseBoolean(line));
				line = element.attributeValue("disabled");
				if (line != null)
					list.setDisabled(Boolean.parseBoolean(line));
			}
			else if("item".equalsIgnoreCase(element.getName()))
			{
				MultiSellEntry e = parseEntry(element, id);
				if(e != null)
				{
					e.setEntryId(entryId++);
					list.addEntry(e);
				}
			}
		}
		addMultiSellListContainer(id, list);
	}

	protected MultiSellEntry parseEntry(Element n, int multiSellId)
	{
		MultiSellEntry entry = new MultiSellEntry();

		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			String line;
			Element d = iterator.next();

			if("ingredient".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
				MultiSellIngredient mi = new MultiSellIngredient(id, count);
				line = d.attributeValue("enchant");
				if(line != null)
					mi.setItemEnchant(Integer.parseInt(line));
				line = d.attributeValue("mantainIngredient");
				if(line != null)
					mi.setMantainIngredient(Boolean.parseBoolean(line));
				//Elements
				line = d.attributeValue("fireAttr");
				if(line != null)
					mi.getItemAttributes().setFire(Integer.parseInt(line));
				line = d.attributeValue("waterAttr");
				if(line != null)
					mi.getItemAttributes().setWater(Integer.parseInt(line));
				line = d.attributeValue("earthAttr");
				if(line != null)
					mi.getItemAttributes().setEarth(Integer.parseInt(line));
				line = d.attributeValue("windAttr");
				if(line != null)
					mi.getItemAttributes().setWind(Integer.parseInt(line));
				line = d.attributeValue("holyAttr");
				if(line != null)
					mi.getItemAttributes().setHoly(Integer.parseInt(line));
				line = d.attributeValue("unholyAttr");
				if(line != null)
					mi.getItemAttributes().setUnholy(Integer.parseInt(line));

				entry.addIngredient(mi);
			}
			else if("production".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
				MultiSellIngredient mi = new MultiSellIngredient(id, count);
				line = d.attributeValue("enchant");
				if(line != null)
					mi.setItemEnchant(Integer.parseInt(line));
				//Elements
				line = d.attributeValue("fireAttr");
				if(line != null)
					mi.getItemAttributes().setFire(Integer.parseInt(line));
				line = d.attributeValue("waterAttr");
				if(line != null)
					mi.getItemAttributes().setWater(Integer.parseInt(line));
				line = d.attributeValue("earthAttr");
				if(line != null)
					mi.getItemAttributes().setEarth(Integer.parseInt(line));
				line = d.attributeValue("windAttr");
				if(line != null)
					mi.getItemAttributes().setWind(Integer.parseInt(line));
				line = d.attributeValue("holyAttr");
				if(line != null)
					mi.getItemAttributes().setHoly(Integer.parseInt(line));
				line = d.attributeValue("unholyAttr");
				if(line != null)
					mi.getItemAttributes().setUnholy(Integer.parseInt(line));

				if(!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0)
				{
					ItemTemplate item = ItemHolder.getInstance().getTemplate(id);
					if(item != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS)
						return null;
				}

				entry.addProduct(mi);
			}
		}

		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			_log.warn("MultiSell [" + multiSellId + "] is empty!");
			return null;
		}

		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				_log.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
				return null;
			}
			if(multiSellId < 70000 || multiSellId > 70010) //FIXME hardcode. Все кроме GM Shop
				if(item.getReferencePrice() > entry.getIngredients().get(0).getItemCount())
					_log.warn("MultiSell [" + multiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + entry.getIngredients().get(0).getItemCount());
		}

		return entry;
	}

	protected void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		getHolder().addMultiSellListContainer(id, list);
	}
}
