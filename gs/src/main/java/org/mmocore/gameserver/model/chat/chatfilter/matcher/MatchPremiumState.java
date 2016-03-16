package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка на статус Премиум Аккаунта у игрока.
 *
 * @author G1ta0
 */
public class MatchPremiumState implements ChatFilterMatcher
{
	private final boolean _excludePremium;

	public MatchPremiumState(boolean premiumState)
	{
		_excludePremium = premiumState;
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return _excludePremium || !player.hasBonus();
	}


}
