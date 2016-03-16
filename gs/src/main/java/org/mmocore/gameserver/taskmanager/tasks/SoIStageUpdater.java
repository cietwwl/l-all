package org.mmocore.gameserver.taskmanager.tasks;

import org.mmocore.commons.time.cron.SchedulingPattern;
import org.mmocore.gameserver.instancemanager.SoIManager;

/**
 * @author VISTALL
 * @date 5:53/12.08.2011
 */
public class SoIStageUpdater extends AutomaticTask
{
	private static final SchedulingPattern PATTERN = new SchedulingPattern("0 12 * * mon");

	@Override
	public void doTask() throws Exception
	{
		SoIManager.setCurrentStage(1);

		_log.info("Seed of Infinity update Task: Seed updated successfuly.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}
