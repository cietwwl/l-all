package org.mmocore.gameserver.model.instances;

import java.util.List;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.MultiSellHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.olympiad.CompType;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.model.entity.olympiad.OlympiadDatabase;
import org.mmocore.gameserver.network.l2.c2s.L2GameClientPacket;
import org.mmocore.gameserver.network.l2.c2s.RequestBypassToServer;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExReceiveOlympiad;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadManagerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManagerInstance.class);

	public OlympiadManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		if(Config.ENABLE_OLYMPIAD)
			Olympiad.addOlympiadNpc(this);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!Config.ENABLE_OLYMPIAD)
			return;

		if(checkForDominionWard(player))
			return;

		// до всех проверок
		if (command.startsWith("_olympiad?")) // _olympiad?command=move_op_field&field=1
		{
			String[] ar = command.split("&");
			if (ar.length < 2)
				return;

			if (ar[0].equalsIgnoreCase("_olympiad?command=move_op_field"))
			{
				if (!Config.ENABLE_OLYMPIAD_SPECTATING)
					return;

				String[] command2 = ar[1].split("=");
				if (command2.length < 2)
					return;

				Olympiad.addSpectator(Integer.parseInt(command2[1]) - 1, player);
			}
			return;
		}

		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("OlympiadNoble"))
		{
			int val = Integer.parseInt(command.substring(14));
			HtmlMessage html = new HtmlMessage(this);

			switch(val)
			{
				case 1:
					Olympiad.unRegisterNoble(player);
					showChatWindow(player, 0);
					break;
				case 2:
					if(Olympiad.isRegistered(player))
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_noregister.htm"));
					else
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_register.htm"));
					break;
				case 4:
					Olympiad.registerNoble(player, CompType.NON_CLASSED);
					break;
				case 5:
					Olympiad.registerNoble(player, CompType.CLASSED);
					break;
				case 6:
					int passes = Olympiad.getNoblessePasses(player);
					if(passes > 0)
						ItemFunctions.addItem(player, Config.ALT_OLY_COMP_RITEM, passes);
					else
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_nopoints.htm"));
					break;
				case 7:
					MultiSellHolder.getInstance().SeparateAndSend(102, player, getObjectId(), 0);
					break;
				case 9:
					MultiSellHolder.getInstance().SeparateAndSend(103, player, getObjectId(), 0);
					break;
				case 10:
					Olympiad.registerNoble(player, CompType.TEAM);
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else if(command.startsWith("Olympiad"))
		{
			int val = Integer.parseInt(command.substring(9, 10));

			HtmlMessage reply = new HtmlMessage(this);

			switch(val)
			{
				case 1:
					if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
					{
						player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
						return;
					}
					player.sendPacket(new ExReceiveOlympiad.MatchList());		
					break;
				case 2:
					// for example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if(classId >= 88)
					{
						reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_ranking.htm");

						List<String> names = OlympiadDatabase.getClassLeaderBoard(classId);

						int index = 1;
						for(String name : names)
						{
							reply.replace("%place" + index + "%", String.valueOf(index));
							reply.replace("%rank" + index + "%", name);
							index++;
							if(index > 10)
								break;
						}
						for(; index <= 10; index++)
						{
							reply.replace("%place" + index + "%", "");
							reply.replace("%rank" + index + "%", "");
						}

						player.sendPacket(reply);
					}
					// TODO Send player each class rank
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(checkForDominionWard(player))
			return;

		String fileName = Olympiad.OLYMPIAD_HTML_PATH + "manager";
		if(player.isNoble())
			fileName += "_n";
		if(val > 0)
			fileName += "-" + val;
		fileName += ".htm";
		player.sendPacket(new HtmlMessage(this, fileName));
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBypassToServer.class && arg.length == 1 && arg[0].equals("_olympiad?command=move_op_field");
	}
}