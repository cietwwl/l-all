package org.mmocore.gameserver.instancemanager;

import java.util.Collection;

import org.mmocore.gameserver.model.quest.Quest;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class QuestManager
{
	private static IntObjectMap<Quest> _questsById = new HashIntObjectMap<Quest> ();

	public static Quest getQuest(int questId)
	{
		return _questsById.get(questId);
	}

	public static void addQuest(Quest newQuest)
	{
		_questsById.put(newQuest.getId(), newQuest);
	}

	public static Collection<Quest> getQuests()
	{
		return _questsById.values();
	}
}