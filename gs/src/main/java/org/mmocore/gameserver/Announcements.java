package org.mmocore.gameserver;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.s2c.Say2;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Announcements
{
	public class Announce extends RunnableImpl
	{
		private Future<?> _task;
		private final int _time;
		private final String _announce;

		public Announce(int t, String announce)
		{
			_time = t;
			_announce = announce;
		}

		@Override
		public void runImpl() throws Exception
		{
			announceToAll(_announce);
		}

		public void showAnnounce(Player player)
		{
			Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, player.getName(), _announce, null);
			player.sendPacket(cs);
		}

		public void start()
		{
			if(_time > 0)
				_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, _time * 1000L, _time * 1000L);
		}

		public void stop()
		{
			if(_task != null)
			{
				_task.cancel(false);
				_task = null;
			}
		}

		public int getTime()
		{
			return _time;
		}

		public String getAnnounce()
		{
			return _announce;
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Announcements.class);

	private static final Announcements _instance = new Announcements();

	public static final Announcements getInstance()
	{
		return _instance;
	}

	private List<Announce> _announcements = new ArrayList<Announce>();

	private Announcements()
	{
		loadAnnouncements();
	}

	public List<Announce> getAnnouncements()
	{
		return _announcements;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();

		try
		{
			List<String> lines =  Arrays.asList(FileUtils.readFileToString(new File("config/announcements.txt"), "UTF-8").split("\n"));
			for(String line : lines)
			{
				if(StringUtils.isEmpty(line))
					continue;

				StringTokenizer token = new StringTokenizer(line, "\t");
				if(token.countTokens() > 1)
					addAnnouncement(Integer.parseInt(token.nextToken()), token.nextToken(), false);
				else
					addAnnouncement(0, line, false);
			}
		}
		catch(Exception e)
		{
			_log.error("Error while loading config/announcements.txt!");
		}
	}

	public void showAnnouncements(Player activeChar)
	{
		for(Announce announce : _announcements)
			announce.showAnnounce(activeChar);
	}

	public void addAnnouncement(int val, String text, boolean save)
	{
		Announce announce = new Announce(val, text);
		announce.start();

		_announcements.add(announce);
		if(save)
			saveToDisk();
	}

	public void delAnnouncement(int line)
	{
		Announce announce = _announcements.remove(line);
		if(announce != null)
			announce.stop();

		saveToDisk();
	}

	private void saveToDisk()
	{
		try
		{
			File f = new File("config/announcements.txt");
			FileWriter writer = new FileWriter(f, false);
			for(Announce announce : _announcements)
				writer.write(announce.getTime() + "\t" + announce.getAnnounce() + "\n");
			writer.close();
		}
		catch(Exception e)
		{
			_log.error("Error while saving config/announcements.txt!", e);
		}
	}

	public void announceToAll(String text)
	{
		announceToAll(text, ChatType.ANNOUNCEMENT);
	}

	public static void shout(Player activeChar, String text, ChatType type)
	{
		Say2 cs = new Say2(activeChar.getObjectId(), type, activeChar.getName(), text, null);
		ChatUtils.shout(activeChar, cs);
		activeChar.sendPacket(cs);
	}

	public void announceToAll(String text, ChatType type)
	{
		Say2 cs = new Say2(0, type, "", text, null);
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(cs);
	}

	public void announceToAllFromStringHolder(String add, Object... arg)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", String.format(StringHolder.getInstance().getString(add, player), arg), null));
	}

	public void announceToAll(CustomMessage cm)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(cm);
	}

	public void announceToAll(SystemMessage sm)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(sm);
	}

	public void announceToAll(IBroadcastPacket sm)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(sm);
	}
}