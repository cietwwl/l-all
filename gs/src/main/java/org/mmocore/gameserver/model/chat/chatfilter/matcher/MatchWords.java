package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import java.util.regex.Pattern;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Проверка по ключевым словам в тексте сообщения.
 *
 * @author G1ta0
 */
public class MatchWords implements ChatFilterMatcher
{
	public final Pattern[] _patterns;

	public MatchWords(String[] words)
	{
		_patterns = new Pattern[words.length];
		for(int i = 0; i < words.length; i++)
			_patterns[i] = Pattern.compile(words[i], Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		for(Pattern p : _patterns)
			if(p.matcher(msg).find())
				return true;
		return false;
	}

}
