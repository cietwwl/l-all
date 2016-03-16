package org.mmocore.gameserver.model.actor.instances.player.tasks;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;

/**
 * @author VISTALL
 * @date 17:29/03.10.2011
 */
public class EnableUserRelationTask extends RunnableImpl
{
	private HardReference<Player> _playerRef;
	private SiegeEvent<?, ?> _siegeEvent;

	public EnableUserRelationTask(Player player, SiegeEvent<?, ?> siegeEvent)
	{
		_siegeEvent = siegeEvent;
		_playerRef = player.getRef();
	}

	@Override
	public void runImpl() throws Exception
	{
		Player player = _playerRef.get();
		if(player == null)
			return;

		_siegeEvent.removeBlockFame(player);

		player.stopEnableUserRelationTask();
		player.broadcastUserInfo(true);
	}
}
