package org.mmocore.gameserver.network.l2.c2s;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.cache.ItemInfoCache;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.handler.voicecommands.VoicedCommandHandler;
import org.mmocore.gameserver.instancemanager.PetitionManager;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.chat.ChatFilters;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilter;
import org.mmocore.gameserver.model.chat.chatfilter.ChatMsg;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.olympiad.OlympiadGame;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.matching.MatchingRoom;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.Say2;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.ChatUtils;
import org.mmocore.gameserver.utils.Language;
import org.mmocore.gameserver.utils.Log;
import org.mmocore.gameserver.utils.Strings;
import org.mmocore.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.VarMap;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

	/** RegExp для кэширования ссылок на предметы, пример ссылки: \b\tType=1 \tID=268484598 \tColor=0 \tUnderline=0 \tTitle=\u001BAdena\u001B\b */
	public static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	public static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = org.mmocore.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_type == null || StringUtils.isEmpty(_text))
			return;

		_text = _text.replaceAll("\\\\n", "\n");

		if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = StringUtils.EMPTY;
			for(int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(_text.length() > 0)
					_text += "\n  >";
				_text += lines[i];
			}
		}

		if(_text.isEmpty())
			return;

		if(_text.startsWith("."))
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage(new CustomMessage("common.CommandNotFound"));
			return;
		}
		else if(_text.startsWith("=="))
		{
			String expression = _text.substring(2);
			Expression expr = null;

			if(!expression.isEmpty())
			{
				try
				{
					expr = ExpressionTree.parse(expression);
				}
				catch(ExpressionParseException epe)
				{

				}

				if(expr != null)
				{
					double result;

					try
					{
						VarMap vm = new VarMap();
						vm.setValue("adena", activeChar.getAdena());
						result = expr.eval(vm, null);
						activeChar.sendMessage(expression);
						activeChar.sendMessage("=" + Util.formatDouble(result, "NaN", false));
					}
					catch(Exception e)
					{

					}
				}
			}

			return;
		}

		if(activeChar.getInvisible() == SpecialEffectState.GM)
		{
			activeChar.sendMessage("You cannot chat while in hide.");
			return;
		}

		Player receiver = _target == null ? null : World.getPlayer(_target);

		long currentTimeMillis = System.currentTimeMillis();

		//пропускать фильтры для GM
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			loop: for(ChatFilter f : ChatFilters.getinstance().getFilters())
			{
				if(f.isMatch(activeChar, _type, _text, receiver))
				{
					switch(f.getAction())
					{
						case ChatFilter.ACTION_BAN_CHAT:
							activeChar.updateNoChannel(Integer.parseInt(f.getValue()) * 1000L);
							break loop;
						case ChatFilter.ACTION_WARN_MSG:
							activeChar.sendMessage(new CustomMessage(f.getValue()));
							return;
						case ChatFilter.ACTION_REPLACE_MSG:
							_text = f.getValue();
							break loop;
						case ChatFilter.ACTION_REDIRECT_MSG:
							_type = ChatType.valueOf(f.getValue());
							continue loop;
					}
				}
			}
		}

		//Выдем сообщение, только если бан не перманент
		if(activeChar.getNoChannel() > 0)
		{
			if(ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type))
			{
				if(activeChar.getNoChannelRemained() > 0)
				{
					long timeRemained = activeChar.getNoChannelRemained() / 60000L;
					activeChar.sendMessage(new CustomMessage("common.ChatBanned").addNumber(timeRemained));
					return;
				}
				activeChar.updateNoChannel(0);
			}
		}

		if(_text.isEmpty())
			return;

		// Кэширование линков предметов
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		ItemInstance item;
		int objectId;
		Language lang = null;

		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

			if(item == null)
				return;

			lang = activeChar.getLanguage();
			ItemInfoCache.getInstance().put(item);
		}

		String translit = activeChar.getVar("translit");
		if(translit != null)
		{
			//Исключаем из транслитерации ссылки на предметы
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(_text.substring(end, end = m.end()));
			}

			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}

		Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text, lang);

		int identifierForLog = 0;
		switch(_type)
		{
			case TELL:
				if(receiver == null || receiver.getInvisible() == SpecialEffectState.GM)
				{
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target));
					return;
				}
				if(receiver.isInOfflineMode())
				{
					activeChar.sendMessage(new CustomMessage("common.PlayerInOfflineTrade"));
					return;
				}
				if(receiver.isInBlockList(activeChar) || receiver.isBlockAll())
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT);
					return;
				}
				if(receiver.getMessageRefusal())
				{
					activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					return;
				}

				if(activeChar.canTalkWith(receiver))
					receiver.sendPacket(cs);

				cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text, lang);
				activeChar.sendPacket(cs);
				break;
			case SHOUT:
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(Config.GLOBAL_SHOUT)
					ChatUtils.announce(activeChar, cs);
				else
					ChatUtils.shout(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case TRADE:
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(Config.GLOBAL_TRADE_CHAT)
					ChatUtils.announce(activeChar, cs);
				else
					ChatUtils.shout(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case ALL:
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(activeChar.isCursedWeaponEquipped())
					cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text, lang);

				if(activeChar.isInOlympiadMode())
				{
					OlympiadGame game = activeChar.getOlympiadGame();
					if(game != null)
					{
						ChatUtils.say(activeChar, game.getAllPlayers(), cs);
						break;
					}
				}

				ChatUtils.say(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if(activeChar.getClan() == null)
					return;

				identifierForLog = activeChar.getClanId();
				activeChar.getClan().broadcastToOnlineMembers(cs);
				break;
			case ALLIANCE:
				if(activeChar.getClan() == null || activeChar.getClan().getAlliance() == null)
					return;

				identifierForLog = activeChar.getAllyId();
				activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				break;
			case PARTY:
				if(!activeChar.isInParty())
					return;

				identifierForLog = activeChar.getPlayerGroup().hashCode();
				activeChar.getParty().broadCast(cs);
				break;
			case PARTY_ROOM:
				MatchingRoom room = activeChar.getMatchingRoom();
				if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
					return;

				identifierForLog = room.getId();
				for (Player roomMember : room.getPlayers())
					if (activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				break;
			case COMMANDCHANNEL_ALL:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() != activeChar)
				{
					activeChar.sendPacket(SystemMsg.ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT);
					return;
				}

				identifierForLog = activeChar.getPlayerGroup().hashCode();

				activeChar.getParty().getCommandChannel().broadCast(cs);
				break;
			case COMMANDCHANNEL_COMMANDER:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(!activeChar.getParty().isLeader(activeChar))
				{
					activeChar.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
					return;
				}

				identifierForLog = activeChar.getParty().getCommandChannel().hashCode();

				activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
				break;
			case HERO_VOICE:
				if(!activeChar.isHero() && !activeChar.getPlayerAccess().CanAnnounce)
					return;

				ChatUtils.announce(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT);
					return;
				}

				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			case BATTLEFIELD:
				DominionSiegeEvent siegeEvent = activeChar.getEvent(DominionSiegeEvent.class);
				if(siegeEvent == null)
					return;

				identifierForLog = siegeEvent.getId();

				for(Player player : GameObjectsStorage.getPlayers())
					if(!player.isInBlockList(activeChar) && !player.isBlockAll() && activeChar.canTalkWith(player) && player.getEvent(DominionSiegeEvent.class) == siegeEvent)
						player.sendPacket(cs);
				break;
			case MPCC_ROOM:
				MatchingRoom mpccRoom = activeChar.getMatchingRoom();
				if(mpccRoom == null || mpccRoom.getType() != MatchingRoom.CC_MATCHING)
					return;

				identifierForLog = mpccRoom.getId();

				for (Player roomMember : mpccRoom.getPlayers())
					if (activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				break;
			default:
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
				return;
		}

		Log.LogChat(_type.name(), activeChar.getName(), _target, _text, identifierForLog);

		activeChar.getMessageBucket().addLast(new ChatMsg(_type, receiver == null ? 0 : receiver.getObjectId(), _text.hashCode(), (int) (currentTimeMillis / 1000L)));

		activeChar.getListeners().onSay(_type, _target, _text);
	}
}