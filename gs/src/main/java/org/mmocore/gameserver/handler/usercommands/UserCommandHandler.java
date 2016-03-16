package org.mmocore.gameserver.handler.usercommands;

import gnu.trove.TIntObjectHashMap;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.handler.usercommands.impl.ClanPenalty;
import org.mmocore.gameserver.handler.usercommands.impl.ClanWarsList;
import org.mmocore.gameserver.handler.usercommands.impl.CommandChannel;
import org.mmocore.gameserver.handler.usercommands.impl.Escape;
import org.mmocore.gameserver.handler.usercommands.impl.InstanceZone;
import org.mmocore.gameserver.handler.usercommands.impl.Loc;
import org.mmocore.gameserver.handler.usercommands.impl.MyBirthday;
import org.mmocore.gameserver.handler.usercommands.impl.OlympiadStat;
import org.mmocore.gameserver.handler.usercommands.impl.PartyInfo;
import org.mmocore.gameserver.handler.usercommands.impl.Time;


public class UserCommandHandler extends AbstractHolder
{
	private static final UserCommandHandler _instance = new UserCommandHandler();

	public static UserCommandHandler getInstance()
	{
		return _instance;
	}

	private TIntObjectHashMap<IUserCommandHandler> _datatable = new TIntObjectHashMap<IUserCommandHandler>();

	private UserCommandHandler()
	{
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new CommandChannel());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new MyBirthday());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new InstanceZone());
		registerUserCommandHandler(new Time());
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for(int element : ids)
			_datatable.put(element, handler);
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(userCommand);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
