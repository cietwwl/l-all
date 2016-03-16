package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка на минимальное время с момента создания персонажа.
 *
 * @author G1ta0
 */
public class MatchMinLiveTime implements ChatFilterMatcher
{
	private final long _createTime;

	public MatchMinLiveTime(int createTime)
	{
		_createTime = createTime * 1000L;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return System.currentTimeMillis() - player.getCreateTime() < _createTime;
	}

}
