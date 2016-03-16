package org.mmocore.gameserver.model.entity.events.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.objects.SiegeClanObject;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.entity.residence.Dominion;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExDominionChannelSet;
import org.mmocore.gameserver.network.l2.s2c.ExPartyMemberRenamed;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.PartySmallWindowUpdate;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;

/**
 * @author VISTALL
 * @date 15:24/14.02.2011
 */
public class DominionSiegeRunnerEvent extends Event
{
	public static final String REGISTRATION = "registration";
	public static final String BATTLEFIELD = "battlefield";

	private Calendar _startTime = Calendar.getInstance();

	private int _state;

	// quests
	private Map<ClassId, Quest> _classQuests = new HashMap<ClassId, Quest>();
	private List<Quest> _breakQuests = new ArrayList<Quest>();
	// dominions
	private List<Dominion> _registeredDominions = new ArrayList<Dominion>(9);

	public DominionSiegeRunnerEvent(MultiValueSet<String> set)
	{
		super(set);

		_startTime.setTimeInMillis(0);
	}

	@Override
	public void initEvent()
	{
		Calendar cal = generateNextDate(Calendar.SATURDAY);
		if(_registeredDominions.size() >= 2 && _startTime.getTimeInMillis() == cal.getTimeInMillis())
			reCalcNextTime(false);

		addState(SiegeEvent.REGISTRATION_STATE);
	}

	@Override
	public void startEvent()
	{
		addState(SiegeEvent.PROGRESS_STATE);

		super.startEvent();

		for(Dominion d : _registeredDominions)
		{
			d.getSiegeEvent().clearActions();
			d.getSiegeEvent().registerActions();

			d.getSiegeEvent().startEvent();
		}

		broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_BEGUN);
	}

	@Override
	public void stopEvent()
	{
		addState(SiegeEvent.REGISTRATION_STATE);
		removeState(SiegeEvent.PROGRESS_STATE);

		super.stopEvent();

		for(Dominion d : _registeredDominions)
		{
			d.getSiegeEvent().clearActions();

			d.getSiegeEvent().stopEvent();
		}

		broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_ENDED);
	}

	@Override
	public void announce(int val)
	{
		switch(val)
		{
			case -20:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_WILL_BEGIN_IN_20_MINUTES);
				break;
			case -10:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_10_MINUTES);
				break;
			case -5:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_5_MINUTES);
				break;
			case -1:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_1_MINUTE);
				break;
			case 3600:
				broadcastToWorld(new SystemMessage(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1HOURS).addNumber(val / 3600));
				break;
			case 600:
			case 300:
			case 60:
				broadcastToWorld(new SystemMessage(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1MINUTES).addNumber(val / 60));
				break;
			case 10:
			case 5:
			case 4:
			case 3:
			case 2:
			case 1:
				broadcastToWorld(new SystemMessage(SystemMsg.S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR).addNumber(val));
				break;
		}
	}

	public Calendar getSiegeDate()
	{
		return _startTime;
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		registerActions();
	}

	@Override
	protected long startTimeMillis()
	{
		return _startTime.getTimeInMillis();
	}

	@Override
	public EventType getType()
	{
		return EventType.MAIN_EVENT;
	}
	//========================================================================================================================================================================
	//                                                         Broadcast
	//========================================================================================================================================================================

	public void broadcastTo(IBroadcastPacket packet)
	{
		for(Dominion dominion : _registeredDominions)
			dominion.getSiegeEvent().broadcastTo(packet);
	}

	public void broadcastTo(L2GameServerPacket packet)
	{
		for(Dominion dominion : _registeredDominions)
			dominion.getSiegeEvent().broadcastTo(packet);
	}

	//========================================================================================================================================================================
	//                                                         Getters/Setters
	//========================================================================================================================================================================

	@Override
	public boolean isInProgress()
	{
		return hasState(SiegeEvent.PROGRESS_STATE);
	}

	public void addState(int b)
	{
		_state |= b;

		for(Dominion d : _registeredDominions)
			d.getSiegeEvent().addState(b);
	}

	public void removeState(int b)
	{
		_state &= ~b;

		for(Dominion d : _registeredDominions)
			d.getSiegeEvent().removeState(b);

		switch(b)
		{
			case SiegeEvent.REGISTRATION_STATE:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_REQUEST_PERIOD_HAS_ENDED);
				break;
		}
	}

	public boolean hasState(int val)
	{
		return (_state & val) == val;
	}

	public boolean isRegistrationOver()
	{
		return !hasState(SiegeEvent.REGISTRATION_STATE);
	}

	public void addClassQuest(ClassId c, Quest quest)
	{
		_classQuests.put(c, quest);
	}

	public Quest getClassQuest(ClassId c)
	{
		return _classQuests.get(c);
	}

	public void addBreakQuest(Quest q)
	{
		_breakQuests.add(q);
	}

	public List<Quest> getBreakQuests()
	{
		return _breakQuests;
	}

	//========================================================================================================================================================================
	//                                                         Overrides Event
	//========================================================================================================================================================================
	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(REGISTRATION))
		{
			if(start)
				addState(SiegeEvent.REGISTRATION_STATE);
			else
				removeState(SiegeEvent.REGISTRATION_STATE);
		}
		else if(name.equalsIgnoreCase(BATTLEFIELD))
		{
			if(start)
			{
				addState(DominionSiegeEvent.BATTLEFIELD_CHAT_STATE);

				initDominions();
			}
			else
			{
				removeState(DominionSiegeEvent.BATTLEFIELD_CHAT_STATE);

				clearDominions();
			}
		}
		else
			super.action(name, start);
	}

	public synchronized void registerDominion(Dominion d, boolean onStart)
	{
		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);

		Calendar dateIfNotSet = generateNextDate(Calendar.SUNDAY);

		boolean dominionAction;
		if(onStart)
		{
			dominionAction = true;
			_registeredDominions.add(d);

			if(_startTime.getTimeInMillis() == 0)
			{
				Calendar current = Calendar.getInstance();
				Calendar nextDate = generateNextDate(Calendar.SATURDAY);
				nextDate.add(Calendar.WEEK_OF_YEAR, -2);

				int minDayOfYear = Integer.MAX_VALUE, maxDayOfYear = Integer.MIN_VALUE;
				for(Castle castle : castles)
				{
					Calendar siegeDate = castle.getSiegeDate();

					int val = (siegeDate.getTimeInMillis() == 0 ? dateIfNotSet : siegeDate).get(Calendar.DAY_OF_YEAR);
					if(val < minDayOfYear)
						minDayOfYear = val;

					if(val > maxDayOfYear)
						maxDayOfYear = val;
				}

				_startTime.setTimeInMillis(nextDate.getTimeInMillis());

				loop:
				{
					// если не все осады закончили
					if(minDayOfYear != maxDayOfYear)
						break loop;

					// если сервак запустился в воскр. когда будут осады, но их ищо нету
					if(current.get(Calendar.DAY_OF_YEAR) == minDayOfYear)
						break loop;

					// если сервак запустился после ТВ
					if(nextDate.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR))
						break loop;

					_startTime.add(Calendar.WEEK_OF_YEAR, 2);
				}
			}

			d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
		}
		else
		{
			dominionAction = !_registeredDominions.contains(d);
			if(dominionAction)
				_registeredDominions.add(d);

			int minDayOfYear = Integer.MAX_VALUE, maxDayOfYear = Integer.MIN_VALUE;
			for(Castle castle : castles)
			{
				Calendar siegeDate = castle.getSiegeDate();

				int val = (siegeDate.getTimeInMillis() == 0 ? dateIfNotSet : siegeDate).get(Calendar.DAY_OF_YEAR);
				if(val < minDayOfYear)
					minDayOfYear = val;

				if(val > maxDayOfYear)
					maxDayOfYear = val;
			}

			if(minDayOfYear == maxDayOfYear && _registeredDominions.size() > 1)
			{
				_startTime.setTimeInMillis(generateNextDate(Calendar.SATURDAY).getTimeInMillis());

				reCalcNextTime(false);
			}
		}

		if(dominionAction)
		{
			d.getSiegeEvent().spawnAction(DominionSiegeEvent.TERRITORY_NPC, true);
			d.rewardSkills();
		}
	}

	public synchronized void unRegisterDominion(Dominion d)
	{
		if(!_registeredDominions.contains(d))
			return;

		_registeredDominions.remove(d);

		d.getSiegeEvent().spawnAction(DominionSiegeEvent.TERRITORY_NPC, false);
		d.getSiegeDate().setTimeInMillis(0);

		// если уже ток 1 доминион - чистим таймер
		if(_registeredDominions.size() == 1)
		{
			clearActions();

			_startTime.setTimeInMillis(0);
		}
	}

	private Calendar generateNextDate(int day)
	{
		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(Config.CASTLE_VALIDATION_DATE.getTimeInMillis());
		startTime.set(Calendar.DAY_OF_WEEK, day);
		if(startTime.before(Config.CASTLE_VALIDATION_DATE))
			startTime.add(Calendar.WEEK_OF_YEAR, 1);
		startTime.set(Calendar.HOUR_OF_DAY, 20);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		while(startTime.getTimeInMillis() < System.currentTimeMillis())
			startTime.add(Calendar.WEEK_OF_YEAR, 2);

		return startTime;
	}

	public List<Dominion> getRegisteredDominions()
	{
		return _registeredDominions;
	}

	//========================================================================================================================================================================
	//                                                        Dominion actions
	//========================================================================================================================================================================

	private void initDominions()
	{
		// овнеры являются дефендерами територий
		for(Dominion d : _registeredDominions)
		{
			DominionSiegeEvent siegeEvent = d.getSiegeEvent();
			SiegeClanObject ownerClan = new SiegeClanObject(SiegeEvent.DEFENDERS, siegeEvent.getResidence().getOwner(), 0);

			siegeEvent.addObject(SiegeEvent.DEFENDERS, ownerClan);
		}

		// проверка на 2х реги, и чиста ревардов от другой територии, если зареган был
		for(Dominion d : _registeredDominions)
		{
			List<SiegeClanObject> defenders = d.getSiegeEvent().getObjects(DominionSiegeEvent.DEFENDERS);
			for(SiegeClanObject siegeClan : defenders)
			{
				// листаем мемберов от клана
				for(UnitMember member : siegeClan.getClan())
				{
					for(Dominion d2 : _registeredDominions)
					{
						DominionSiegeEvent siegeEvent2 = d2.getSiegeEvent();
						List<Integer> defenderPlayers2 = siegeEvent2.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);

						defenderPlayers2.remove(Integer.valueOf(member.getObjectId()));

						// если у игрока есть реварды от другой з територии - обнуляем
						if(d != d2)
							siegeEvent2.clearReward(member.getObjectId());
					}
				}
			}

			List<Integer> defenderPlayers = d.getSiegeEvent().getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);
			for(Integer playerObjectId : defenderPlayers)
				for(Dominion d2 : _registeredDominions)
					if(d != d2)
					{
						DominionSiegeEvent siegeEvent2 = d2.getSiegeEvent();
						List<Integer> defenderPlayers2 = siegeEvent2.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);

						defenderPlayers2.remove(playerObjectId);

						// если у игрока есть реварды от другой з територии - обнуляем
						siegeEvent2.clearReward(playerObjectId);
					}
		}

		// переносим с другоим доминионов дефендеров - в аттакеры
		for(Dominion d : _registeredDominions)
		{
			DominionSiegeEvent ds = d.getSiegeEvent();
			for(Dominion d2 : _registeredDominions)
			{
				if(d2 == d)
					continue;

				DominionSiegeEvent ds2 = d2.getSiegeEvent();

				ds.addObjects(SiegeEvent.ATTACKERS, ds2.<Serializable>getObjects(SiegeEvent.DEFENDERS));
				ds.addObjects(DominionSiegeEvent.ATTACKER_PLAYERS, ds2.<Serializable>getObjects(DominionSiegeEvent.DEFENDER_PLAYERS));
			}

			// добавляем у всех кто онлайн евент, невызываем broadcastCharInfo - ибо ненужно, оно само вызовется если заюзается скрол
			for(Player player : ds.getOnlinePlayers())
			{
				player.sendPacket(ExDominionChannelSet.ACTIVE);
				player.addEvent(ds);
			}
		}
	}

	private void clearDominions()
	{
		broadcastToWorld(SystemMsg.THE_BATTLEFIELD_CHANNEL_HAS_BEEN_DEACTIVATED);
		for(Dominion d : _registeredDominions)
		{
			DominionSiegeEvent siegeEvent = d.getSiegeEvent();
			for(Player player : siegeEvent.getOnlinePlayers())
			{
				player.sendPacket(ExDominionChannelSet.DEACTIVE);
				if(siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(player.getObjectId()))
				{
					player.broadcastUserInfo(true);
					player.removeEvent(siegeEvent);

					if(player.isInParty())
						player.getParty().broadcastToPartyMembers(player, new ExPartyMemberRenamed(player), new PartySmallWindowUpdate(player));
				}
				else
					player.removeEvent(siegeEvent);
			}

			siegeEvent.removeObjects(DominionSiegeEvent.DISGUISE_PLAYERS);
			siegeEvent.removeObjects(DominionSiegeEvent.DEFENDER_PLAYERS);
			siegeEvent.removeObjects(DominionSiegeEvent.DEFENDERS);
			siegeEvent.removeObjects(DominionSiegeEvent.ATTACKER_PLAYERS);
			siegeEvent.removeObjects(DominionSiegeEvent.ATTACKERS);
		}
	}
}
