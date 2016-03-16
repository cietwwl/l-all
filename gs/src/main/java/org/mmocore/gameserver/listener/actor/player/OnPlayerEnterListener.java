package org.mmocore.gameserver.listener.actor.player;

import org.mmocore.gameserver.listener.PlayerListener;
import org.mmocore.gameserver.model.Player;

public interface OnPlayerEnterListener extends PlayerListener
{
	public void onPlayerEnter(Player player);
}
