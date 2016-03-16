package org.mmocore.gameserver.network.l2.c2s;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import org.apache.commons.lang3.tuple.Pair;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.MultiSellHolder;
import org.mmocore.gameserver.handler.admincommands.AdminCommandHandler;
import org.mmocore.gameserver.handler.bbs.BbsHandlerHolder;
import org.mmocore.gameserver.handler.bbs.IBbsHandler;
import org.mmocore.gameserver.handler.bypass.BypassHolder;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.handler.voicecommands.VoicedCommandHandler;
import org.mmocore.gameserver.instancemanager.OlympiadHistoryManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.Hero;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.utils.BypassStorage.ValidBypass;
import org.mmocore.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);

	private String _bypass = null;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _bypass.isEmpty())
			return;

		ValidBypass bp = activeChar.getBypassStorage().validate(_bypass);
		if(bp == null)
		{
			_log.debug("RequestBypassToServer: Unexpected bypass : " + _bypass + " client : " + getClient() + "!");
			return;
		}

		NpcInstance npc = activeChar.getLastNpc();
		GameObject target = activeChar.getTarget();
		if(npc == null && target != null && target.isNpc())
			npc = (NpcInstance) target;

		try
		{
			if(_bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, _bypass);
			else if(_bypass.startsWith("player_help "))
				playerHelp(activeChar, _bypass.substring(12));
			else if(_bypass.startsWith("scripts_"))
			{
				_log.error("Trying to call script bypass: " + _bypass + " " + activeChar);
			}
			else if(_bypass.startsWith("htmbypass_"))
			{
				String command = _bypass.substring(10).trim();
				String word = command.split("\\s+")[0];

				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
					b.getValue().invoke(b.getKey(), activeChar, npc, command.substring(word.length()).trim().split("\\s+"));
			}
			else if(_bypass.startsWith("user_"))
			{
				String command = _bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
					vch.useVoicedCommand(word, activeChar, args);
				else
					_log.warn("Unknow voiced command '" + word + "'");
			}
			else if(_bypass.startsWith("npc_"))
			{
				int endOfId = _bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
					id = _bypass.substring(4, endOfId);
				else
					id = _bypass.substring(4);
				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.isInRangeZ(object.getLoc(), Creature.INTERACTION_DISTANCE))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, _bypass.substring(endOfId + 1));
				}
			}
			else if(_bypass.startsWith("_olympiad?")) // _olympiad?command=move_op_field&field=1
			{
				// Переход в просмотр олимпа разрешен только от менеджера или с арены.
				final NpcInstance manager = NpcUtils.canPassPacket(activeChar, this, _bypass.split("&")[0]);
				if (manager != null)
					manager.onBypassFeedback(activeChar, _bypass);
			}
			else if(_bypass.startsWith("_diary"))
			{
				String params = _bypass.substring(_bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
			}
			else if(_bypass.startsWith("_match"))
			{
				String params = _bypass.substring(_bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);

				OlympiadHistoryManager.getInstance().showHistory(activeChar,  heroclass, heropage);
			}
			else if(_bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				GameObject object = activeChar.getTarget();
				if(object != null && object.isNpc())
					((NpcInstance) object).onBypassFeedback(activeChar, _bypass);
			}
			else if(_bypass.startsWith("multisell "))
			{
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(_bypass.substring(10)), activeChar, npc != null ? npc.getObjectId() : -1, 0);
			}
			else if(_bypass.startsWith("Quest "))
			{
				/*String p = _bypass.substring(6).trim();
				int idx = p.indexOf(' ');
				if(idx < 0)
					activeChar.processQuestEvent(Integer.parseInt(p.split("_")[1]), StringUtils.EMPTY, npc);
				else
					activeChar.processQuestEvent(Integer.parseInt(p.substring(0, idx).split("_")[1]), p.substring(idx).trim(), npc); */
				_log.warn("Trying to call Quest bypass: " + _bypass + ", player: " + activeChar);
			}
			else if(bp.bbs)
			{
				if(!Config.COMMUNITYBOARD_ENABLED)
					activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
				else
				{
					IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(_bypass);
					if(handler != null)
						handler.onBypassCommand(activeChar, _bypass);
				}
			}
		}
		catch(Exception e)
		{
			String st = "Error while handling bypass: " + _bypass;
			if(npc != null)
				st = st + " via NPC " + npc;

			_log.error(st, e);
		}
	}

	private static void playerHelp(Player activeChar, String path)
	{
		HtmlMessage html = new HtmlMessage(5);
		html.setFile(path);
		activeChar.sendPacket(html);
	}
}