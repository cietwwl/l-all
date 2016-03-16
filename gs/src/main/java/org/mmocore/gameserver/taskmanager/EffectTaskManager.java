package org.mmocore.gameserver.taskmanager;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.threading.SteppingRunnableQueueManager;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;

/**
 * Менеджер задач для работы с эффектами, шаг выполенния задач 250 мс.
 * 
 * @author G1ta0
 */
public class EffectTaskManager extends SteppingRunnableQueueManager
{
	private final static long TICK = 250L;

	private static int _randomizer;

	private final static EffectTaskManager[] _instances = new EffectTaskManager[Config.EFFECT_TASK_MANAGER_COUNT];
	static
	{
		for(int i = 0; i < _instances.length; i++)
			_instances[i] = new EffectTaskManager();
	}

	public final static EffectTaskManager getInstance()
	{
		return _instances[_randomizer++ & (_instances.length - 1)];
	}

	private EffectTaskManager()
	{
		super(TICK);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(TICK), TICK);
		//Очистка каждые 30 секунд со сдвигом
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				EffectTaskManager.this.purge();
			}

		}, 30000L + 1000L * _randomizer++, 30000L);
	}

	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}
}