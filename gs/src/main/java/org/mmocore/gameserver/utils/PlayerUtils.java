package org.mmocore.gameserver.utils;

import org.mmocore.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 23:43/17.05.2012
 */
public class PlayerUtils
{
	public static void updateAttackableFlags(Player player)
	{
		player.broadcastRelation();
		if(player.getServitor() != null)
			player.getServitor().broadcastCharInfo();
	}
}
