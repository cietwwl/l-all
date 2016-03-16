package org.mmocore.gameserver.listener.actor.player.impl;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.gameserver.listener.actor.player.OnPlayerExitListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerSayListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.s2c.Snoop;

/**
 * @author VISTALL
 * @date 22:00/15.09.2011
 */
public class SnoopPlayerSayListener implements OnPlayerSayListener, OnPlayerExitListener
{
	private HardReference<Player> _ownerRef;

	public SnoopPlayerSayListener(Player owner)
	{
		_ownerRef = owner.getRef();
	}

	@Override
	public void onSay(Player activeChar, ChatType type, String target, String text)
	{
		Player owner = _ownerRef.get();
		if(owner == null)
		{
			activeChar.removeListener(this);
			return;
		}

		String speaker = type == ChatType.TELL ? "->" + target : activeChar.getName();

		owner.sendPacket(new Snoop(activeChar.getObjectId(), activeChar.getName(), type, speaker, text));
	}

	@Override
	public void onPlayerExit(Player player)
	{
		Player owner = _ownerRef.get();
		if(owner == null)
			return;

		owner.getVars().remove(Player.SNOOP_TARGET);
	}

	public Player getOwner()
	{
		return _ownerRef.get();
	}
}
