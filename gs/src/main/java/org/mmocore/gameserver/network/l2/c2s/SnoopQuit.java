package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.listener.Listener;
import org.mmocore.gameserver.listener.actor.player.impl.SnoopPlayerSayListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;

public class SnoopQuit extends L2GameClientPacket
{
	private int _targetObjectId;

	@Override
	protected void readImpl()
	{
		_targetObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Player target = GameObjectsStorage.getPlayer(_targetObjectId);
		if(target == null)
			return;

		for(Listener<Creature> $listener : target.getListeners().getListeners())
		{
			if($listener instanceof SnoopPlayerSayListener)
			{
				SnoopPlayerSayListener listener = (SnoopPlayerSayListener) $listener;

				if(listener.getOwner() == player)
				{
					target.removeListener($listener);
					player.getVars().remove(Player.SNOOP_TARGET);
					break;
				}
			}
		}
	}
}