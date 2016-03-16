package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import gnu.trove.TIntHashSet;

import java.util.Deque;
import java.util.Iterator;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.model.chat.chatfilter.ChatMsg;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Условие ограничения на количество получателей, за определенное время.
 *
 * @author G1ta0
 */
public class MatchRecipientLimit implements ChatFilterMatcher
{
	private final int _limitCount;
	private final int _limitTime;
	private final int _limitBurst;

	public MatchRecipientLimit(int limitCount, int limitTime, int limitBurst)
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

		TIntHashSet recipients = new TIntHashSet();

		Deque<ChatMsg> msgBucket = player.getMessageBucket();

		Iterator<ChatMsg> itr = msgBucket.descendingIterator();
		while(itr.hasNext())
		{
			ChatMsg cm = itr.next();
			if(cm.chatType == type && cm.recipient != 0)
			{
				firstMsgTime = cm.time;
				recipients.add(cm.recipient);
				count = recipients.size();
				if(_limitBurst == count)
					break;
			}
		}

		count -= ((currentTime - firstMsgTime) / _limitTime) * _limitCount;

		return _limitBurst <= count;
	}

}
