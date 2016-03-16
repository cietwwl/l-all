package org.mmocore.gameserver.listener.actor.player;

import org.mmocore.gameserver.listener.PlayerListener;
import org.mmocore.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener
{
	public void onPartyInvite(Player player);
}
