package org.mmocore.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;

import net.sf.ehcache.CacheManager;

import org.mmocore.commons.lang.StatsUtils;
import org.mmocore.commons.listener.Listener;
import org.mmocore.commons.listener.ListenerList;
import org.mmocore.commons.net.nio.impl.SelectorStats;
import org.mmocore.commons.net.nio.impl.SelectorThread;
import org.mmocore.commons.versioning.Version;
import org.mmocore.gameserver.cache.CrestCache;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.dao.CharacterMinigameScoreDAO;
import org.mmocore.gameserver.dao.ItemsDAO;
import org.mmocore.gameserver.data.BoatHolder;
import org.mmocore.gameserver.data.xml.Parsers;
import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.data.xml.holder.StaticObjectHolder;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.handler.admincommands.AdminCommandHandler;
import org.mmocore.gameserver.handler.bbs.BbsHandlerHolder;
import org.mmocore.gameserver.handler.bypass.BypassHolder;
import org.mmocore.gameserver.handler.items.ItemHandler;
import org.mmocore.gameserver.handler.onshiftaction.OnShiftActionHolder;
import org.mmocore.gameserver.handler.usercommands.UserCommandHandler;
import org.mmocore.gameserver.handler.voicecommands.VoicedCommandHandler;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.instancemanager.AutoSpawnManager;
import org.mmocore.gameserver.instancemanager.BloodAltarManager;
import org.mmocore.gameserver.instancemanager.CastleManorManager;
import org.mmocore.gameserver.instancemanager.CoupleManager;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.instancemanager.DimensionalRiftManager;
import org.mmocore.gameserver.instancemanager.HellboundManager;
import org.mmocore.gameserver.instancemanager.PetitionManager;
import org.mmocore.gameserver.instancemanager.PlayerMessageStack;
import org.mmocore.gameserver.instancemanager.RaidBossSpawnManager;
import org.mmocore.gameserver.instancemanager.SoDManager;
import org.mmocore.gameserver.instancemanager.SoIManager;
import org.mmocore.gameserver.instancemanager.SpawnManager;
import org.mmocore.gameserver.instancemanager.games.FishingChampionShipManager;
import org.mmocore.gameserver.instancemanager.games.LotteryManager;
import org.mmocore.gameserver.instancemanager.games.MiniGameScoreManager;
import org.mmocore.gameserver.instancemanager.itemauction.ItemAuctionManager;
import org.mmocore.gameserver.instancemanager.naia.NaiaCoreManager;
import org.mmocore.gameserver.instancemanager.naia.NaiaTowerManager;
import org.mmocore.gameserver.listener.GameListener;
import org.mmocore.gameserver.listener.game.OnShutdownListener;
import org.mmocore.gameserver.listener.game.OnStartListener;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.entity.Hero;
import org.mmocore.gameserver.model.entity.MonsterRace;
import org.mmocore.gameserver.model.entity.SevenSigns;
import org.mmocore.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.GamePacketHandler;
import org.mmocore.gameserver.network.telnet.TelnetServer;
import org.mmocore.gameserver.scripts.Scripts;
import org.mmocore.gameserver.tables.CharTemplateTable;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.tables.EnchantHPBonusTable;
import org.mmocore.gameserver.tables.LevelUpTable;
import org.mmocore.gameserver.tables.PetSkillsTable;
import org.mmocore.gameserver.tables.SkillTreeTable;
import org.mmocore.gameserver.taskmanager.ItemsAutoDestroy;
import org.mmocore.gameserver.taskmanager.tasks.DeleteExpiredMailTask;
import org.mmocore.gameserver.taskmanager.tasks.DeleteExpiredVarsTask;
import org.mmocore.gameserver.taskmanager.tasks.OlympiadSaveTask;
import org.mmocore.gameserver.taskmanager.tasks.RecNevitResetTask;
import org.mmocore.gameserver.taskmanager.tasks.SoIStageUpdater;
import org.mmocore.gameserver.utils.Strings;
import org.mmocore.gameserver.utils.TradeHelper;
import org.mmocore.gameserver.utils.velocity.VelocityUtils;
import org.napile.primitive.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}

	public static GameServer _instance;

	private final SelectorThread<GameClient> _selectorThreads[];
	private final SelectorStats _selectorStats = new SelectorStats();
	private Version version;
	private TelnetServer statusServer;
	private final GameServerListenerList _listeners;

	private long _serverStartTimeMillis;

	public SelectorThread<GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public SelectorStats getSelectorStats()
	{
		return _selectorStats;
	}

	public long getServerStartTime()
	{
		return _serverStartTimeMillis;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		_instance = this;
		_serverStartTimeMillis = System.currentTimeMillis();
		_listeners = new GameServerListenerList();

		new File("./log/").mkdir();

		version = new Version(GameServer.class);

		_log.info("=================================================");
		_log.info("Revision: ................ " + version.getVersionRevision());
		_log.info("Build date: .............. " + version.getBuildDate());
		_log.info("=================================================");

		Variables.RETURN_LONG_VALUE_IF_NOT_FOUND = -1;

		// Initialize config
		Config.load();
		VelocityUtils.init();
		// Check binding address
		checkFreePorts();
		// Initialize database
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		DatabaseFactory.getInstance().getConnection().close();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		CacheManager.getInstance();

		ThreadPoolManager.getInstance();

		Scripts.getInstance();

		GeoEngine.load();

		Strings.reload();

		GameTimeController.getInstance();

		World.init();

		Parsers.parseAll();

		ItemsDAO.getInstance();

		CrestCache.getInstance();

		CharacterDAO.getInstance();

		ClanTable.getInstance();

		SkillTreeTable.getInstance();

		CharTemplateTable.getInstance();

		EnchantHPBonusTable.getInstance();

		LevelUpTable.getInstance();

		PetSkillsTable.getInstance();

		ItemAuctionManager.getInstance();

		SpawnManager.getInstance().spawnAll();
		BoatHolder.getInstance().spawnAll();

		StaticObjectHolder.getInstance().spawnAll();

		RaidBossSpawnManager.getInstance();

		Scripts.getInstance().init();

		DimensionalRiftManager.getInstance();

		Announcements.getInstance();

		LotteryManager.getInstance();

		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();

		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();

		AutoSpawnManager.getInstance();

		SevenSigns.getInstance().spawnSevenSignsNPC();

		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		PetitionManager.getInstance();

		CursedWeaponsManager.getInstance();

		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}

		ItemHandler.getInstance();

		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		BbsHandlerHolder.getInstance().log();
		BypassHolder.getInstance().log();
		OnShiftActionHolder.getInstance().log();

		// tasks
		if(Config.ENABLE_OLYMPIAD)
			new OlympiadSaveTask();
		if(Config.EX_JAPAN_MINIGAME)
			CharacterMinigameScoreDAO.getInstance().select();

		new DeleteExpiredVarsTask();
		new DeleteExpiredMailTask();
		new SoIStageUpdater();
		new RecNevitResetTask();

		ClanTable.getInstance().checkClans();

		_log.info("=[Events]=========================================");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		_log.info("==================================================");

		CastleManorManager.getInstance();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		CoupleManager.getInstance();

		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance();

		HellboundManager.getInstance();

		NaiaTowerManager.getInstance();
		NaiaCoreManager.getInstance();

		SoDManager.getInstance();
		SoIManager.getInstance();
		BloodAltarManager.getInstance();

		MiniGameScoreManager.getInstance();

		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);

		GamePacketHandler gph = new GamePacketHandler();

		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);

		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			_selectorThreads[i] = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, _selectorStats, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}

		getListeners().onStart();

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			_log.info("Restoring offline traders...");
			int count = TradeHelper.restoreOfflineTraders();
			_log.info("Restored " + count + " offline traders.");
		}

		_log.info("GameServer started.");

		AuthServerCommunication.getInstance().start();

		if(Config.IS_TELNET_ENABLED)
			statusServer = new TelnetServer();
		else
			_log.info("Telnet server is currently disabled.");

		_log.info("=================================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
			_log.info(line);
		_log.info("=================================================");

	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	public static void checkFreePorts()
	{
		boolean binded = false;
		while(!binded)
			for(int PORT_GAME : Config.PORTS_GAME)
				try
				{
					ServerSocket ss;
					if(Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
						ss = new ServerSocket(PORT_GAME);
					else
						ss = new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
					ss.close();
					binded = true;
				}
				catch(Exception e)
				{
					_log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
					binded = false;
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e2)
					{}
				}
	}

	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}

}