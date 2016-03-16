package org.mmocore.gameserver.model.chat.chatfilter.matcher;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.utils.MapUtils;

/**
 * Условие проверки нахождения игрока в регионах.
 *
 * @author G1ta0
 */
public class MatchMaps implements ChatFilterMatcher
{
	private final int[] _maps;

	public MatchMaps(int[] maps)
	{
		_maps = maps;
	}

	/**
	 * Если фильтр работает только в определенных регионах, проверяем, находится ли в одном из них игрок.
	 *
	 * @return true, если условие выполенено, false, если игрок за пределами регионов фильтра.
	 */
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		int rx = MapUtils.regionX(player);
		int ry = MapUtils.regionY(player);

		for(int i =0; i < _maps.length; i+=2)
		{
			int mx = _maps[i];
			int my = _maps[i+1];

			if(mx == rx && my == ry)
				return true;
		}

		return false;
	}

}
