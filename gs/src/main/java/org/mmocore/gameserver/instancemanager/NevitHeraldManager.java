package org.mmocore.gameserver.instancemanager;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.s2c.ExShowScreenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pchayka
 */
public class NevitHeraldManager
{
	private static final String spawngroup = "nevitt_herald_group";

	private static final Logger _log = LoggerFactory.getLogger(NevitHeraldManager.class);
	private static NevitHeraldManager _instance = new NevitHeraldManager();
	private static boolean _spawned = false;

	public static NevitHeraldManager getInstance()
	{
		return _instance;
	}

	public void doSpawn(int bossId)
	{
		NpcString ns = null;
		if(bossId == 29068)
			ns = NpcString.ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED;
		else if(bossId == 29028)
			ns = NpcString.VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED;

		if(ns != null)
			for(Player player : GameObjectsStorage.getPlayers())
				player.sendPacket(new ExShowScreenMessage(ns, 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
		if(_spawned)
			return;
		_spawned = true;
		SpawnManager.getInstance().spawn(spawngroup);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				SpawnManager.getInstance().despawn(spawngroup);
				_spawned = false;
			}
		}, 3 * 3600 * 1000L); // 3 hours
	}

}
