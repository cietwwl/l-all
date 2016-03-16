package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Обязательное условие для филтьтра чата. Проверка совпадения по списку каналов, по которым идет фильтрация.
 *
 * @author G1ta0
 */
public class MatchChatChannels implements ChatFilterMatcher
{
	private final ChatType[] _channels;

	public MatchChatChannels(ChatType[] channels)
	{
		_channels = channels;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		for(ChatType ct : _channels)
			if(ct == type)
				return true;
		return false;
	}
}
