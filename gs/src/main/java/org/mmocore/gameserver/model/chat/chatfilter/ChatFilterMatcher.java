package org.mmocore.gameserver.model.chat.chatfilter;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Интерфейс класса проверки условий фильтрации.
 *
 * @author G1ta0
 */
public interface ChatFilterMatcher
{
	/**
	 * Проверка условия для фильтрации.
	 *
	 * @return true, если условие совпадает
	 */
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient);
}
