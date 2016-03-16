package org.mmocore.gameserver.model.actor.instances.player.tasks;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;

/**
 * @author VISTALL
 * @date 0:40/21.08.2011
 */
public class LectureInitialPeriodEndTask extends RunnableImpl
{
	private HardReference<Player> _playerRef;

	public LectureInitialPeriodEndTask(Player player)
	{
		_playerRef = player.getRef();
	}

	@Override
	public void runImpl() throws Exception
	{
		Player player = _playerRef.get();
		if(player == null)
			return;

		player.sendPacket(SystemMsg.YOU_HAVE_COMPLETED_THE_INITIAL_LEVEL);
		player.setLectureMark(Player.OFF_MARK, true);
		player.stopLectureTask();
	}
}
