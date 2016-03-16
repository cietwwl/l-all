package org.mmocore.gameserver.listener.actor.player;

import org.mmocore.gameserver.listener.PlayerListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * @author VISTALL
 * @date 20:45/15.09.2011
 */
public interface OnPlayerSayListener extends PlayerListener
{
	void onSay(Player activeChar, ChatType type, String target, String text);
}
