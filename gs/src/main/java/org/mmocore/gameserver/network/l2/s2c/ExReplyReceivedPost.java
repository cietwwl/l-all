package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.mail.Mail;
import org.mmocore.gameserver.network.l2.c2s.RequestExReceivePost;
import org.mmocore.gameserver.network.l2.c2s.RequestExRejectPost;
import org.mmocore.gameserver.network.l2.c2s.RequestExRequestReceivedPost;

/**
 * Просмотр полученного письма. Шлется в ответ на {@link RequestExRequestReceivedPost}.
 * При попытке забрать приложенные вещи клиент шлет {@link RequestExReceivePost}.
 * При возврате письма клиент шлет {@link RequestExRejectPost}.
 * @see ExReplySentPost
 */
public class ExReplyReceivedPost extends L2GameServerPacket
{
	private final Mail mail;

	public ExReplyReceivedPost(Mail mail)
	{
		this.mail = mail;
	}

	// dddSSS dx[hddQdddhhhhhhhhhh] Qdd
	@Override
	protected void writeImpl()
	{
		writeEx(0xAB);

		writeD(mail.getMessageId()); // id письма
		writeD(mail.isPayOnDelivery() ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо
		writeD(mail.getType() == Mail.SenderType.NORMAL ? 0 : 1); // returnable

		writeS(mail.getSenderName()); // от кого
		writeS(mail.getTopic()); // топик
		writeS(mail.getBody()); // тело

		writeD(mail.getAttachments().size()); // количество приложенных вещей
		for(ItemInstance item : mail.getAttachments())
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}

		writeQ(mail.getPrice()); // для писем с оплатой - цена
		writeD(mail.getAttachments().size() > 0 ? 1 : 0); // 1 - письмо можно вернуть
		writeD(mail.getType().ordinal()); // 1 - на письмо нельзя отвечать, его нельзя вернуть, в отправителе значится news informer (или "****" если установлен флаг в начале пакета)
	}
}