package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Логическое отрицание для условия.
 *
 * @author G1ta0
 */
public class MatchLogicalNot implements ChatFilterMatcher
{
	private final ChatFilterMatcher _match;

	public MatchLogicalNot(ChatFilterMatcher match)
	{
		_match = match;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return !_match.isMatch(player, type, msg, recipient);
	}
}
