package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Логическое "ИЛИ" для нескольких условий.
 *
 * @author G1ta0
 */
public class MatchLogicalOr implements ChatFilterMatcher
{
	private final ChatFilterMatcher[] _matches;

	public MatchLogicalOr(ChatFilterMatcher[] matches)
	{
		_matches = matches;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		for(ChatFilterMatcher m : _matches)
			if(m.isMatch(player, type, msg, recipient))
				return true;
		return false;
	}
}
