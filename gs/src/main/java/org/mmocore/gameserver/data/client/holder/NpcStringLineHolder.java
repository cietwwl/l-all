package org.mmocore.gameserver.data.client.holder;

import java.util.HashMap;
import java.util.Map;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.templates.client.NpcStringLine;
import org.mmocore.gameserver.utils.Language;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class NpcStringLineHolder extends AbstractHolder
{
	private static final NpcStringLineHolder _instance = new NpcStringLineHolder();

	private Map<Language, IntObjectMap<NpcStringLine>> _strings = new HashMap<Language, IntObjectMap<NpcStringLine>>();

	public static NpcStringLineHolder getInstance()
	{
		return _instance;
	}

	public NpcStringLine get(Language lang, int id)
	{
		IntObjectMap<NpcStringLine> map = _strings.get(lang);
		if(map == null)
			return null;

		return map.get(id);
	}

	public void put(Language lang, int id, String value)
	{
		IntObjectMap<NpcStringLine> map = _strings.get(lang);
		if(map == null)
			_strings.put(lang, map = new HashIntObjectMap<NpcStringLine>());

		map.put(id, new NpcStringLine(id, value));
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, IntObjectMap<NpcStringLine>> entry : _strings.entrySet())
			info("load npcstring line(s): " + entry.getValue().size() + " for lang: " + entry.getKey());
	}

	@Override
	public int size()
	{
		return _strings.size();
	}

	@Override
	public void clear()
	{
		_strings.clear();
	}
}
