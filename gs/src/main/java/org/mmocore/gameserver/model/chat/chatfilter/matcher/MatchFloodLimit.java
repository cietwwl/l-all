package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import java.util.Deque;
import java.util.Iterator;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.model.chat.chatfilter.ChatMsg;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Условие ограничения на частоту отправки одинаковых одинаковых сообщений в канал.
 *
 * @author G1ta0
 */
public class MatchFloodLimit implements ChatFilterMatcher
{
	private final int _limitCount;
	private final int _limitTime;
	private final int _limitBurst;

	public MatchFloodLimit(int limitCount, int limitTime, int limitBurst)
	{
		_limitCount = limitCount;
		_limitTime = limitTime;
		_limitBurst = limitBurst;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		int currentTime = (int) (System.currentTimeMillis() / 1000L);
		int firstMsgTime = currentTime;
		int count = 0;
		int msgHashcode = msg.hashCode();

		Deque<ChatMsg> msgBucket = player.getMessageBucket();

		Iterator<ChatMsg> itr = msgBucket.descendingIterator();
		while(itr.hasNext())
		{
			ChatMsg cm = itr.next();
			if(cm.chatType == type && cm.msgHashcode == msgHashcode)
			{
				firstMsgTime = cm.time;
				count++;
				if(_limitBurst == count)
					break;
			}
		}

		count -= ((currentTime - firstMsgTime) / _limitTime) * _limitCount;

		return _limitBurst <= count;
	}

}
