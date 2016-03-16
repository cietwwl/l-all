package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.instancemanager.QuestManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.network.l2.s2c.QuestList;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questID;

	@Override
	protected void readImpl()
	{
		_questID = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		Quest quest = QuestManager.getQuest(_questID);
		if(activeChar == null || quest == null)
			return;

		if(!quest.canAbortByPacket())
		{
			// обновляем клиент, ибо он сам удаляет
			activeChar.sendPacket(new QuestList(activeChar));
			return;
		}

		QuestState qs = activeChar.getQuestState(_questID);
		if(qs != null && !qs.isCompleted())
			qs.abortQuest();
	}
}