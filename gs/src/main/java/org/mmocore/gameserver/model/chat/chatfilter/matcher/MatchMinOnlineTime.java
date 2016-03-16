package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка на минимальное общее время онлайн игрока.
 *
 * @author G1ta0
 */
public class MatchMinOnlineTime implements ChatFilterMatcher
{
	private final long _onlineTime;

	public MatchMinOnlineTime(int onlineTime)
	{
		_onlineTime = onlineTime * 1000L;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getOnlineTime() < _onlineTime;
	}

}
