package org.mmocore.gameserver.taskmanager.tasks;

import org.mmocore.commons.time.cron.SchedulingPattern;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 5:56/12.08.2011
 */
public class RecNevitResetTask extends AutomaticTask
{
	public static final SchedulingPattern PATTERN = new SchedulingPattern("30 6 * * *");

	@Override
	public void doTask() throws Exception
	{
		long t = System.currentTimeMillis();
		_log.info("RecNevitResetTask: start.");
		for(Player player : GameObjectsStorage.getPlayers())
		{
			player.restartRecom();
			player.getNevitSystem().restartSystem();
		}
		_log.info("RecNevitResetTask: done in " + (System.currentTimeMillis() - t) + " ms.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}
