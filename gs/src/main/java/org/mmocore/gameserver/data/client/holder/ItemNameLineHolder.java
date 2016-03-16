package org.mmocore.gameserver.data.client.holder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.templates.client.ItemNameLine;
import org.mmocore.gameserver.utils.Language;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author VISTALL
 * @date 20:44/02.09.2011
 */
public class ItemNameLineHolder extends AbstractHolder
{
	private static final ItemNameLineHolder _instance = new ItemNameLineHolder();

	private Map<Language, IntObjectMap<ItemNameLine>> _names = new HashMap<Language, IntObjectMap<ItemNameLine>>();

	public static ItemNameLineHolder getInstance()
	{
		return _instance;
	}

	public ItemNameLine get(Language lang, int itemId)
	{
		IntObjectMap<ItemNameLine> map = _names.get(lang);
		if(map == null)
			return null;
		return map.get(itemId);
	}

	public void put(Language lang, ItemNameLine itemName)
	{
		IntObjectMap<ItemNameLine> map = _names.get(lang);
		if(map == null)
			_names.put(lang, map = new HashIntObjectMap<ItemNameLine>());

		map.put(itemName.getItemId(), itemName);
	}

	public Collection<ItemNameLine> getItems(Language lang)
	{
		IntObjectMap<ItemNameLine> map = _names.get(lang);
		if(map == null)
			return Collections.emptyList();
		return map.values();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, IntObjectMap<ItemNameLine>> entry : _names.entrySet())
			info("load itemname line(s): " + entry.getValue().size() + " for lang: " + entry.getKey());
	}

	@Override
	public int size()
	{
		return _names.size();
	}

	@Override
	public void clear()
	{
		_names.clear();
	}
}
