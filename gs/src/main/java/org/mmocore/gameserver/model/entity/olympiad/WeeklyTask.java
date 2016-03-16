package org.mmocore.gameserver.model.entity.olympiad;

import java.util.Calendar;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(WeeklyTask.class);

	@Override
	public void runImpl() throws Exception
	{
		Olympiad.doWeekTasks();
		_log.info("Olympiad System: Added weekly points to nobles");

		Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
	}
}