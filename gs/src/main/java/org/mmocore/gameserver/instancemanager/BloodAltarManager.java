package org.mmocore.gameserver.instancemanager;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pchayka
 */
public class BloodAltarManager
{
	private static final String[] GROUPS =
	{
			"bloodaltar_boss_aden",
			"bloodaltar_boss_darkelf",
			"bloodaltar_boss_dion",
			"bloodaltar_boss_dwarw",
			"bloodaltar_boss_giran",
			"bloodaltar_boss_gludin",
			"bloodaltar_boss_gludio",
			"bloodaltar_boss_goddart",
			"bloodaltar_boss_heine",
			"bloodaltar_boss_orc",
			"bloodaltar_boss_oren",
			"bloodaltar_boss_schutgart"
	};

	private static final Logger _log = LoggerFactory.getLogger(BloodAltarManager.class);
	private static BloodAltarManager _instance = new BloodAltarManager();
	private static final long DELAY = 30 * 60 * 1000L;

	private long _bossRespawnTimer = 0;
	private boolean _bossesSpawned;

	public static BloodAltarManager getInstance()
	{
		return _instance;
	}

	public BloodAltarManager()
	{
		_log.info("Blood Altar Manager: Initializing...");
		manageNpcs(true);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if(Rnd.chance(30) && _bossRespawnTimer < System.currentTimeMillis())
					if(!_bossesSpawned)
					{
						manageNpcs(false);
						manageBosses(true);
						_bossesSpawned = true;
					}
					else
					{
						manageBosses(false);
						manageNpcs(true);
						_bossesSpawned = false;
					}
			}
		}, DELAY, DELAY);
	}

	private void manageNpcs(boolean spawnAlive)
	{
		if(spawnAlive)
		{
			SpawnManager.getInstance().despawn("bloodaltar_dead_npc");
			SpawnManager.getInstance().spawn("bloodaltar_alive_npc");
		}
		else
		{
			SpawnManager.getInstance().despawn("bloodaltar_alive_npc");
			SpawnManager.getInstance().spawn("bloodaltar_dead_npc");
		}
	}

	private void manageBosses(boolean spawn)
	{
		if(spawn)
			for(String s : GROUPS)
				SpawnManager.getInstance().spawn(s);
		else
		{
			_bossRespawnTimer = System.currentTimeMillis() + 4 * 3600 * 1000L;
			for(String s : GROUPS)
				SpawnManager.getInstance().despawn(s);
		}
	}
}
