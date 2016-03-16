package org.mmocore.gameserver.data.client.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.templates.client.NpcNameLine;
import org.mmocore.gameserver.utils.Language;
import org.mmocore.gameserver.utils.Util;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class NpcNameLineHolder extends AbstractHolder
{
	private static final NpcNameLineHolder _instance = new NpcNameLineHolder();

	private Map<Language, IntObjectMap<NpcNameLine>> _names = new HashMap<Language, IntObjectMap<NpcNameLine>>();
	private List<String> _blackList = new ArrayList<String>(100);

	public static NpcNameLineHolder getInstance()
	{
		return _instance;
	}

	public NpcNameLine get(Language lang, int npcId)
	{
		IntObjectMap<NpcNameLine> map = _names.get(lang);
		if(map == null)
			return null;
		return map.get(npcId);
	}

	public void put(Language lang, NpcNameLine npcName)
	{
		IntObjectMap<NpcNameLine> map = _names.get(lang);
		if(map == null)
			_names.put(lang, map = new HashIntObjectMap<NpcNameLine>());

		map.put(npcName.getNpcId(), npcName);

		String name = npcName.getName().toLowerCase();
		if (!_blackList.contains(name) && Util.isMatchingRegexp(name, Config.CNAME_TEMPLATE))
			_blackList.add(name);
	}

	public Collection<NpcNameLine> getNpcNames(Language lang)
	{
		IntObjectMap<NpcNameLine> map = _names.get(lang);
		if(map == null)
			return Collections.emptyList();
		return map.values();
	}

	public boolean isBlackListContainsName(String name)
	{
		return _blackList.contains(name.toLowerCase());
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, IntObjectMap<NpcNameLine>> entry : _names.entrySet())
			info("load npcname line(s): " + entry.getValue().size() + " for lang: " + entry.getKey());
		info("load " + _blackList.size() + " blacklist names");
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
