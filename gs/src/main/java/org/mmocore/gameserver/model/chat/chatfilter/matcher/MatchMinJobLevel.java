package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка на минимальный уровень профессии игрока.
 *
 * @author G1ta0
 */
public class MatchMinJobLevel implements ChatFilterMatcher
{
	private final int _classLevel;

	public MatchMinJobLevel(int classLevel)
	{
		_classLevel = classLevel;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getClassId().level() < _classLevel;
	}
}
