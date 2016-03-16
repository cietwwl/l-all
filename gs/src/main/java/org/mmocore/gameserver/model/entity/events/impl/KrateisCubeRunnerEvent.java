package org.mmocore.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.time.cron.SchedulingPattern;
import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventType;
import org.mmocore.gameserver.model.entity.events.objects.SpawnExObject;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.utils.ChatUtils;

/**
 * @author VISTALL
 * @date 21:42/10.12.2010
 */
public class KrateisCubeRunnerEvent extends Event
{
	private static final SchedulingPattern DATE_PATTERN = new SchedulingPattern("0,30 * * * *");

	public static final String MANAGER = "manager";
	public static final String REGISTRATION = "registration";

	private boolean _isInProgress;
	private boolean _isRegistrationOver;

	private List<KrateisCubeEvent> _cubes = new ArrayList<KrateisCubeEvent>(3);
	private Calendar _calendar = Calendar.getInstance();

	public KrateisCubeRunnerEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void initEvent()
	{
		super.initEvent();
		_cubes.add(EventHolder.getInstance().<KrateisCubeEvent>getEvent(EventType.PVP_EVENT, 2));
		_cubes.add(EventHolder.getInstance().<KrateisCubeEvent>getEvent(EventType.PVP_EVENT, 3));
		_cubes.add(EventHolder.getInstance().<KrateisCubeEvent>getEvent(EventType.PVP_EVENT, 4));
	}

	@Override
	public void startEvent()
	{
		super.startEvent();
		_isInProgress = true;
	}

	@Override
	public void stopEvent()
	{
		_isInProgress = false;

		super.stopEvent();

		reCalcNextTime(false);
	}

	@Override
	public void announce(int val)
	{
		NpcInstance npc = getNpc();
		if (npc == null)
			return; // для запуска сервера без npc
		switch(val)
		{
			case -600:
			case -300:
				ChatUtils.say(npc, NpcString.THE_MATCH_WILL_BEGIN_IN_S1_MINUTES, String.valueOf(- val / 60));
				break;
			case -540:
			case -330:
			case 60:
			case 360:
			case 660:
			case 960:
				ChatUtils.say(npc, NpcString.REGISTRATION_FOR_THE_NEXT_MATCH_WILL_END_AT_S1_MINUTES_AFTER_HOUR, String.valueOf(_calendar.get(Calendar.MINUTE) == 30 ? 57 : 27));
				break;
			case -480:
				ChatUtils.say(npc, NpcString.THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH);
				break;
			case -360:
				ChatUtils.say(npc, NpcString.THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH);
				break;
			case -240:
				ChatUtils.say(npc, NpcString.THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH);
				break;
			case -180:
			case -120:
			case -60:
				ChatUtils.say(npc, NpcString.THE_MATCH_WILL_BEGIN_SHORTLY);
				break;
			case 600:
				ChatUtils.say(npc, NpcString.THE_MATCH_WILL_BEGIN_IN_S1_MINUTES, String.valueOf(20));
				break;
		}
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		_calendar.setTimeInMillis(DATE_PATTERN.next(System.currentTimeMillis()));

		registerActions();
	}

	public NpcInstance getNpc()
	{
		SpawnExObject obj = getFirstObject(MANAGER);

		return obj.getFirstSpawned();
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress;
	}

	public boolean isRegistrationOver()
	{
		return _isRegistrationOver;
	}

	@Override
	protected long startTimeMillis()
	{
		return _calendar.getTimeInMillis();
	}

	@Override
	public void printInfo()
	{
		//
	}

	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(REGISTRATION))
			_isRegistrationOver = !start;
		else
			super.action(name, start);
	}

	public List<KrateisCubeEvent> getCubes()
	{
		return _cubes;
	}

	public boolean isRegistered(Player player)
	{
		for(KrateisCubeEvent cubeEvent : _cubes)
			if(cubeEvent.getRegisteredPlayer(player) != null && cubeEvent.getParticlePlayer(player) != null)
				return true;
		return false;
	}

	@Override
	public EventType getType()
	{
		return EventType.MAIN_EVENT;
	}
}
