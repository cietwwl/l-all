package org.mmocore.gameserver.network.l2.components;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author VISTALL
 * @date  13:28/01.12.2010
 */
public interface IBroadcastPacket
{
	L2GameServerPacket packet(Player player);
}
