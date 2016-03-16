package org.mmocore.gameserver.model.chat.chatfilter;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Класс фильтрации сообщений в чат, в зависимости от выбранных условий.
 *
 * @author G1ta0
 */
public class ChatFilter
{
	public final static int ACTION_NONE = 0;
	/** Заблокировать игроку чат на каналах Config.BAN_CHANNEL_LIST */
	public final static int ACTION_BAN_CHAT = 1;
	/** Предупредить сообщением */
	public final static int ACTION_WARN_MSG = 2;
	/** Заменить сообщение */
	public final static int ACTION_REPLACE_MSG = 3;
	/** Направить сообщение в другой канал */
	public final static int ACTION_REDIRECT_MSG = 4;

	private final ChatFilterMatcher _matcher;

	private final int _action;
	private final String _value;

	public ChatFilter(ChatFilterMatcher matcher, int action, String value)
	{
		_matcher = matcher;
		_action = action;
		_value = value;
	}

	public int getAction()
	{
		return _action;
	}

	public String getValue()
	{
		return _value;
	}

	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return _matcher.isMatch(player, type, msg, recipient);
	}
}
