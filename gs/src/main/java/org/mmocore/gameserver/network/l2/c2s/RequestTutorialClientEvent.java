package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	// format: cd
	private int _event;

	/**
	 * Пакет от клиента, если вы в туториале подергали мышкой как надо - клиент пришлет его со значением 1 ну или нужным ивентом
	 */
	@Override
	protected void readImpl()
	{
		_event = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.processQuestEvent(255, "CE" + _event, null);
	}
}