package org.mmocore.gameserver.handler.admincommands.impl;

import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.HardSpawner;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.spawn.PeriodOfDay;
import org.mmocore.gameserver.templates.spawn.SpawnNpcInfo;
import org.mmocore.gameserver.templates.spawn.SpawnTemplate;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.NpcUtils;


public class AdminSpawn implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_spawn
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;

		switch(command)
		{
			case admin_spawn:
				try
				{
					if(wordList.length == 1)
						throw new IllegalArgumentException();

					int npcId = Integer.parseInt(wordList[1]);
					int count = wordList.length >= 3 ? Integer.parseInt(wordList[2]) : 1;
					int respawn = wordList.length >= 4 ? Integer.parseInt(wordList[2]) : 0;

					if(respawn == 0)
					{
						for(int i = 0; i < count; i++)
							NpcUtils.spawnSingle(npcId, activeChar.getLoc(), activeChar.getReflection());
					}
					else
					{
						SpawnTemplate template = new SpawnTemplate(PeriodOfDay.NONE, count, respawn, 0);
						template.addNpc(new SpawnNpcInfo(npcId, count, StatsSet.EMPTY));
						template.addSpawnRange(activeChar.getLoc());
						HardSpawner spawner = new HardSpawner(template);
						spawner.setAmount(count);
						spawner.setReflection(activeChar.getReflection());
						spawner.setRespawnDelay(respawn, 0);
						spawner.startRespawn();
						spawner.init();
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					activeChar.sendMessage("USAGE: //spawn npcId [count] [respawn]");
				}
				return true;
		}

		return false;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}