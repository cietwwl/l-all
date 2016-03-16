package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка на минимальный уровень игрока.
 *
 * @author G1ta0
 */
public class MatchMinLevel implements ChatFilterMatcher
{
	private final int _level;

	public MatchMinLevel(int level)
	{
		_level = level;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getLevel() < _level;
	}

}
