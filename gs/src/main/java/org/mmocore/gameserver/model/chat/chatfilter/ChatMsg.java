package org.mmocore.gameserver.model.chat.chatfilter;

import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Запись о сообщении в чат, для сохранения в списке последних сообщений игрока, используемом фильтрами чата.
 *
 * @author G1ta0
 */
public class ChatMsg
{
	public final ChatType chatType;
	public final int recipient;
	public final int msgHashcode;
	public final int time;

	public ChatMsg(ChatType chatType, int recipient, int msgHashcode, int time)
	{
		this.chatType = chatType;
		this.recipient = recipient;
		this.msgHashcode = msgHashcode;
		this.time = time;
	}
}
