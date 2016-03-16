package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.model.Player;
import org.napile.pair.primitive.IntObjectPair;

public class ConfirmDlg extends L2GameClientPacket
{
	private int _answer, _requestId;

	@Override
	protected void readImpl()
	{
		readD();
		_answer = readD();
		_requestId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(true);
		if(entry == null || entry.getKey() != _requestId)
			return;

		OnAnswerListener listener = entry.getValue();
		if(_answer == 1)
			listener.sayYes(activeChar);
		else
			listener.sayNo(activeChar);
	}
}