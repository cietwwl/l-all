package org.mmocore.gameserver.listener.actor.player;

import org.mmocore.gameserver.listener.PlayerListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.Reflection;

public interface OnTeleportListener extends PlayerListener
{
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection);
}
