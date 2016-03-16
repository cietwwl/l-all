package org.mmocore.gameserver.model.instances;


import org.mmocore.gameserver.instancemanager.RaidBossSpawnManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Spawner;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.s2c.ExShowQuestInfo;
import org.mmocore.gameserver.network.l2.s2c.RadarControl;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(AdventurerInstance.class);

	public AdventurerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("npcfind_byid"))
			try
		{
				int bossId = Integer.parseInt(command.substring(12).trim());
				switch(RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId))
				{
					case ALIVE:
					case DEAD:
						Spawner spawn = RaidBossSpawnManager.getInstance().getSpawnTable().get(bossId);

						Location loc = spawn.getCurrentSpawnRange().getRandomLoc(spawn.getReflection().getGeoIndex());

						// Убираем и ставим флажок на карте и стрелку на компасе
						player.sendPacket(new RadarControl(2, 2, loc), new RadarControl(0, 1, loc));
						break;
					case UNDEFINED:
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2AdventurerInstance.BossNotInGame").addNumber(bossId));
						break;
				}
		}
		catch(NumberFormatException e)
		{
			_log.warn("AdventurerInstance: Invalid Bypass to Server command parameter.");
		}
		else if(command.startsWith("raidInfo"))
		{
			int bossLevel = Integer.parseInt(command.substring(9).trim());

			String filename = "adventurer_guildsman/raid_info/info.htm";
			if(bossLevel != 0)
				filename = "adventurer_guildsman/raid_info/level" + bossLevel + ".htm";

			showChatWindow(player, filename);
		}
		else if(command.equalsIgnoreCase("questlist"))
			player.sendPacket(ExShowQuestInfo.STATIC);
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "adventurer_guildsman/" + pom + ".htm";
	}
}