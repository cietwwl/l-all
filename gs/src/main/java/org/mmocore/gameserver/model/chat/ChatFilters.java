package org.mmocore.gameserver.model.chat;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilter;

/**
 * Менеджер фильтров чата.
 *
 * @author G1ta0
 */
public class ChatFilters extends AbstractHolder
{
	private final static ChatFilters _instance = new ChatFilters();

	public final static ChatFilters getinstance()
	{
		return _instance;
	}

	private ChatFilter[] filters = new ChatFilter[0];

	private ChatFilters() {}

	public ChatFilter[] getFilters()
	{
		return filters;
	}

	public void add(ChatFilter f)
	{
		filters = ArrayUtils.add(filters, f);
	}

	@Override
	public void log()
	{
		info(String.format("loaded %d filter(s).", size()));
	}

	@Override
	public int size()
	{
		return filters.length;
	}

	@Override
	public void clear()
	{
		filters = new ChatFilter[0];
	}

}
