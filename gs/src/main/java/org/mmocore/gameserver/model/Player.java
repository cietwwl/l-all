package org.mmocore.gameserver.model;

import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_ALTERED_FLAG;
import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_PEACE_FLAG;
import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_PVP_FLAG;
import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_SIEGE_FLAG;
import static org.mmocore.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_SSQ_FLAG;

import java.awt.Color;
import static java.lang.Thread.sleep;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.chrono.ThaiBuddhistChronology;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.lang.reference.HardReferences;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.GameTimeController;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.ai.PlayableAI.nextAction;
import org.mmocore.gameserver.ai.PlayerAI;
import org.mmocore.gameserver.dao.*;
import org.mmocore.gameserver.data.xml.holder.EventHolder;
import org.mmocore.gameserver.data.xml.holder.InstantZoneHolder;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.data.xml.holder.RecipeHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.database.mysql;
import org.mmocore.gameserver.handler.items.IItemHandler;
import org.mmocore.gameserver.handler.onshiftaction.OnShiftActionHolder;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.instancemanager.DimensionalRiftManager;
import org.mmocore.gameserver.instancemanager.MatchingRoomManager;
import org.mmocore.gameserver.instancemanager.QuestManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.instancemanager.games.HandysBlockCheckerManager;
import org.mmocore.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import org.mmocore.gameserver.listener.actor.player.impl.SummonAnswerListener;
import org.mmocore.gameserver.model.GameObjectTasks.EndSitDownTask;
import org.mmocore.gameserver.model.GameObjectTasks.EndStandUpTask;
import org.mmocore.gameserver.model.GameObjectTasks.HourlyTask;
import org.mmocore.gameserver.model.GameObjectTasks.KickTask;
import org.mmocore.gameserver.model.GameObjectTasks.MountFeedTask;
import org.mmocore.gameserver.model.GameObjectTasks.PvPFlagTask;
import org.mmocore.gameserver.model.GameObjectTasks.RecomBonusTask;
import org.mmocore.gameserver.model.GameObjectTasks.UnJailTask;
import org.mmocore.gameserver.model.GameObjectTasks.WaterTask;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.model.Skill.AddedSkill;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.actor.instances.player.*;
import org.mmocore.gameserver.model.actor.instances.player.FriendList;
import org.mmocore.gameserver.model.actor.instances.player.tasks.EnableUserRelationTask;
import org.mmocore.gameserver.model.actor.instances.player.tasks.LectureInitialPeriodEndTask;
import org.mmocore.gameserver.model.actor.instances.player.tasks.ServitorSummonTask;
import org.mmocore.gameserver.model.actor.listener.PlayerListenerList;
import org.mmocore.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.model.base.PlayerAccess;
import org.mmocore.gameserver.model.base.Race;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.chat.chatfilter.ChatMsg;
import org.mmocore.gameserver.model.entity.DimensionalRift;
import org.mmocore.gameserver.model.entity.Hero;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.model.entity.boat.ClanAirShip;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.DuelEvent;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.SingleMatchEvent;
import org.mmocore.gameserver.model.entity.events.objects.TerritoryWardObject;
import org.mmocore.gameserver.model.entity.olympiad.CompType;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.model.entity.olympiad.OlympiadGame;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.entity.residence.ClanHall;
import org.mmocore.gameserver.model.entity.residence.Fortress;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.instances.*;
import org.mmocore.gameserver.model.items.*;
import org.mmocore.gameserver.model.items.Warehouse.WarehouseType;
import org.mmocore.gameserver.model.items.attachment.FlagItemAttachment;
import org.mmocore.gameserver.model.items.attachment.PickableAttachment;
import org.mmocore.gameserver.model.matching.MatchingRoom;
import org.mmocore.gameserver.model.petition.PetitionMainGroup;
import org.mmocore.gameserver.model.pledge.Alliance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.Privilege;
import org.mmocore.gameserver.model.pledge.Rank;
import org.mmocore.gameserver.model.pledge.RankPrivs;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestEventType;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SceneMovie;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.*;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.SkillEntryType;
import org.mmocore.gameserver.skills.TimeStamp;
import org.mmocore.gameserver.skills.effects.EffectCharge;
import org.mmocore.gameserver.skills.effects.EffectCubic;
import org.mmocore.gameserver.skills.effects.EffectFakeDeath;
import org.mmocore.gameserver.skills.effects.EffectTemplate;
import org.mmocore.gameserver.skills.skillclasses.Transformation;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.stats.funcs.FuncTemplate;
import org.mmocore.gameserver.tables.CharTemplateTable;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.tables.SkillTreeTable;
import org.mmocore.gameserver.taskmanager.AutoSaveManager;
import org.mmocore.gameserver.taskmanager.LazyPrecisionTaskManager;
import org.mmocore.gameserver.templates.FishTemplate;
import org.mmocore.gameserver.templates.Henna;
import org.mmocore.gameserver.templates.PlayerTemplate;
import org.mmocore.gameserver.templates.item.ArmorTemplate;
import org.mmocore.gameserver.templates.item.ArmorTemplate.ArmorType;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.mmocore.gameserver.templates.multisell.MultiSellListContainer;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.*;
import org.mmocore.gameserver.utils.Log.ItemLog;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.pair.primitive.impl.IntObjectPairImpl;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Player extends Playable implements PlayerGroup
{
	private static final Logger _log = LoggerFactory.getLogger(Player.class);                           
        
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	public static final int MAX_POST_FRIEND_SIZE = 100;
	public static final int MAX_TELEPORT_BOOKMARK_SIZE = 9;
	public static final int MAX_FRIEND_SIZE = 128;

	// lecture
	public static final int INITIAL_MARK = 1;
	public static final int EVANGELIST_MARK = 2;
	public static final int OFF_MARK = 3;

	public static final String HERO_AURA = "heroAura";
	public static final String NO_TRADERS_VAR = "notraders";
	public static final String ANIMATION_OF_CAST_RANGE_VAR = "buffAnimRange";
	public static final String TELEPORT_BOOKMARK = "teleport_bookmark";
	public static final String SNOOP_TARGET = "snoop_target";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	public static final String STOREMODE_VAR = "storemode";
	public static final String DISABLE_FOG_AND_RAIN = "disableFogAndRain";

	private static final String NOT_CONNECTED = "<not connected>";


	public Map<Integer, SubClass> _classlist = new HashMap<Integer, SubClass>(4);

	public final static int OBSERVER_NONE = 0;
	public final static int OBSERVER_STARTING = 1;
	public final static int OBSERVER_STARTED = 3;
	public final static int OBSERVER_LEAVING = 2;

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;

	public static final int[] EXPERTISE_LEVELS =
	{
			0,
			20,
			40,
			52,
			61,
			76,
			80,
			84,
			Integer.MAX_VALUE
	};

	private GameClient _connection;
	private String _login;

        //cooldown .warbuff .magebuff .mybuff [profile]
        public long time = 0;
        public long cooldown = 20000;
        public long cooldwonMessage = 0;      
        
	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _recomHave, _recomLeftToday, _fame;
	private int _recomLeft = 20;
	private int _recomBonusTime = 3600;
	private boolean _isHourglassEffected, _isRecomTimerActive;
	private int _deleteTimer;

	private long _createTime, _onlineTime, _leaveClanTime, _deleteClanTime, _NoChannel, _NoChannelBegin;
	private long _uptime;
	/**
	 * Time on login in game
	 */
	private long _lastAccess;

	/**
	 * The Color of players name / title (white is 0xFFFFFF)
	 */
	private int _nameColor, _titlecolor;

	private int _vitalityLevel = -1;
	private double _vitality = Config.VITALITY_LEVELS[4];

	boolean sittingTaskLaunched;

	private int _fakeDeath = 0;

	/**
	 * Time counter when L2Player is sitting
	 */
	private int _waitTimeWhenSit;

	public static final int AUTO_LOOT_NONE = 0;
	public static final int AUTO_LOOT_ALL = 1;
	public static final int AUTO_LOOT_ALL_EXCEPT_ARROWS = 2;

	private int _autoLoot = Config.AUTO_LOOT ? AUTO_LOOT_ALL : AUTO_LOOT_NONE;
	private boolean _autoLootHerbs = Config.AUTO_LOOT_HERBS;

	private final PcInventory _inventory = new PcInventory(this);
	private final Warehouse _warehouse = new PcWarehouse(this);
	private final ItemContainer _refund = new PcRefund(this);
	private final PcFreight _freight = new PcFreight(this);

	private final Deque<ChatMsg> _msgBucket = new LinkedList<ChatMsg>();

	/**
	 * The table containing all L2RecipeList of the L2Player
	 */
	private final Map<Integer, Recipe> _recipebook = new TreeMap<Integer, Recipe>();
	private final Map<Integer, Recipe> _commonrecipebook = new TreeMap<Integer, Recipe>();

	/**
	 * Premium Items
	 */
	private Map<Integer, PremiumItem> _premiumItems = new TreeMap<Integer, PremiumItem>();

	/**
	 * The table containing all Quests began by the L2Player
	 */
	private final IntObjectMap<QuestState> _quests = new HashIntObjectMap<QuestState>();

	/**
	 * The list containing all shortCuts of this L2Player
	 */
	private final ShortCutList _shortCuts = new ShortCutList(this);

	/**
	 * The list containing all macroses of this L2Player
	 */
	private final MacroList _macroses = new MacroList(this);

	/**
	 * The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5)
	 */
	private int _privatestore;
	private long _privateStoreStartTime = 0;

	/**
	 * Данные для магазина рецептов
	 */
	private String _manufactureName;
	private List<ManufactureItem> _createList = Collections.emptyList();
	/**
	 * Данные для магазина продажи
	 */
	private String _sellStoreName;
	private List<TradeItem> _sellList = Collections.emptyList();
	private List<TradeItem> _packageSellList = Collections.emptyList();
	/**
	 * Данные для магазина покупки
	 */
	private String _buyStoreName;
	private List<TradeItem> _buyList = Collections.emptyList();
	/**
	 * Данные для обмена
	 */
	private List<TradeItem> _tradeList = Collections.emptyList();

	/**
	 * hennas
	 */
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR, _hennaINT, _hennaDEX, _hennaMEN, _hennaWIT, _hennaCON;

	private Party _party;
	private Location _lastPartyPosition;

	private Clan _clan;
	private Rank _pledgeClass = Rank.VAGABOND;
	private int _pledgeType = Clan.SUBUNIT_NONE, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;

	/**
	 * GM Stuff
	 */
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();

	private boolean _messageRefusal = false, _tradeRefusal = false, _blockAll = false;

	/**
	 * The L2Summon of the L2Player
	 */
	private Servitor _servitor = null;
	private boolean _riding;

	private DecoyInstance _decoy = null;

	private Map<Integer, EffectCubic> _cubics = null;
	private int _agathionId = 0;

	private Request _request;

	private ItemInstance _arrowItem;

	/**
	 * The fists L2Weapon of the L2Player (used when no weapon is equipped)
	 */
	private WeaponTemplate _fistsWeaponItem;

	private IntObjectMap<AccountPlayerInfo> _playersOnAccount = new HashIntObjectMap<AccountPlayerInfo> (6);

	private int _usedInventoryPercents = 0;
	private int _weightPercents = 0;
	private int _weightPenaltyLevel = 0;
	private boolean _overloaded = false;

	/**
	 * The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
	 */
	private volatile int _expertiseIndex = 0;
	private volatile int _weaponExpertisePenaltyLevel = 0;
	private volatile int _armorExpertisePenaltyLevel = 0;

	private ItemInstance _enchantScroll = null;

	private WarehouseType _usingWHType;

	private boolean _isOnline = false;

	private AtomicBoolean _isLogout = new AtomicBoolean();

	/**
	 * The L2NpcInstance corresponding to the last Folk which one the player talked.
	 */
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	/**
	 * тут храним мультиселл с которым работаем
	 */
	private MultiSellListContainer _multisell = null;

	private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<Integer>();
	private long _spiritShotDischargeTimeStamp = 0;

	private ObservePoint _observePoint;
	private AtomicInteger _observerMode = new AtomicInteger(0);
	private boolean _olympiadObserverMode = false;

	public int _telemode = 0;

	private int _handysBlockCheckerEventArena = -1;

	public boolean entering = true;

	/**
	 * Эта точка проверяется при нештатном выходе чара, и если не равна null чар возвращается в нее
	 * Используется например для возвращения при падении с виверны
	 * Поле heading используется для хранения денег возвращаемых при сбое
	 */
	public Location _stablePoint = null;

	/**
	 * new loto ticket *
	 */
	public int _loto[] = new int[5];
	/**
	 * new race ticket *
	 */
	public int _race[] = new int[2];

	private final Map<Integer, String> _blockList = new ConcurrentSkipListMap<Integer, String>(); // characters blocked with '/block <charname>' cmd
	private final FriendList _friendList = new FriendList(this);

	private boolean _hero = false;
	private boolean _heroAura = false;

	/**
	 * True if the L2Player is in a boat
	 */
	private Boat _boat;
	private Location _inBoatPosition;

	protected int _baseClass = -1;
	protected SubClass _activeClass = null;

	private Bonus _bonus = new Bonus();
	private Future<?> _bonusExpiration;

	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;

	private boolean _noble = false;

	private boolean _inOlympiadMode = false;
	private boolean _isOlympiadCompStarted = false;
	private OlympiadGame _olympiadGame = null;
	private OlympiadGame _olympiadObserveGame = null;

	private int _olympiadSide = -1;

	/**
	 * ally with ketra or varka related wars
	 */
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

	private int _cursedWeaponEquippedId = 0;

	private final Fishing _fishing = new Fishing(this);
	private boolean _isFishing;

	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;

	private Future<?> _vitalityTask;
	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;

	private final Lock _storeLock = new ReentrantLock();

	private boolean _offline = false;

	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;

	private int _pcBangPoints;

	private IntObjectMap<SkillEntry> _transformationSkills = new CHashIntObjectMap<SkillEntry>(); // добавленные трансформацией скиллы
	private IntObjectMap<SkillEntry> _existTransformationSkills = new CHashIntObjectMap<SkillEntry>(); // уже имеющиеся скиллы, переписанные трансформацией

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;
	private int _lectureMark;
	private Future<?> _lectureEndTask;
	private int _tpBookmarkSize;

	private boolean _isTeleportBlocked;

	private SpecialEffectState _invisibleState = SpecialEffectState.FALSE;

	private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

	private List<String> _blockedActions = new ArrayList<String>();

	private BypassStorage _bypassStorage = new BypassStorage();

	private int _buffAnimRange = 1500;
	private boolean _notShowTraders = false;
	private boolean _disableFogAndRain = false;
	private boolean _canSeeAllShouts = false;
	private boolean _debug = false;

	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;

	private long _lastNpcInteractionTime = 0;
	private long _lastReviveTime = 0;

	private long _lastMailTime = 0;

	private IntObjectMap<TimeStamp> _sharedGroupReuses = new CHashIntObjectMap<TimeStamp>();
	private IntObjectPair<OnAnswerListener> _askDialog = null;

	// High Five: Navit's Bonus System
	private NevitSystem _nevitSystem = new NevitSystem(this);

	private boolean _matchingRoomWindowOpened = false;
	private MatchingRoom _matchingRoom;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<Integer, Long>();

	private final MultiValueSet<String> _vars = new MultiValueSet<String>();
	private List<TpBookMark> _tpBookMarks = Collections.emptyList();
	private List<TrapInstance> _traps = Collections.emptyList();
	private List<int[]> _savedServitors = Collections.emptyList();
	private Future<?> _enableRelationTask;

	private List<NpcInstance> _tamedBeasts = Collections.emptyList();

	/**
	 * Конструктор для L2Player. Напрямую не вызывается, для создания игрока используется PlayerManager.create
	 */
        
	public Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);                               
		_login = accountName;
		_nameColor = 0xFFFFFF;
		_titlecolor = 0xFFFF77;
		_baseClass = getClassId().getId();                            
	} 
        
	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to _inventory </li>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2Player</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PlayerTemplate to apply to the L2Player
	 */
	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_ai = new PlayerAI(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _login;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}

	/**
	 * Возвращает список персонажей на аккаунте, за исключением текущего
	 *
	 * @return Список персонажей
	 */
	public IntObjectMap<AccountPlayerInfo> getAccountChars()
	{
		return _playersOnAccount;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) _template;
	}

	@Override
	public PlayerTemplate getBaseTemplate()
	{
		return (PlayerTemplate) _baseTemplate;
	}

	public void changeSex()
	{
		boolean male = true;
		if(getSex() == 1)
			male = false;
		_template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
	}

	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public void sendReuseMessage(SkillEntry skill)
	{
		if(isCastingNow())
			return;
		TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;
		long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500)
			return;
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);
		if(hours > 0)
			sendPacket(new SystemMessage(SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage(SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
	}

	@Override
	public final int getLevel()
	{
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}

	public int getSex()
	{
		return getTemplate().isMale ? 0 : 1;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public void offline()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}

		setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
		setOnlineTime(getOnlineTime()); // сохраняем текущее общее время онлайна
		setUptime(0); // сбрасываем аптайм
		setOfflineMode(true);

		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1);

		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival."); //TODO [G1ta0] custom message
			leaveParty();
		}

		if(getServitor() != null)
			getServitor().unSummon(false, false);

		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode())
			Olympiad.logoutPlayer(this);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		broadcastCharInfo();

		//TODO [VISTALL] call stopAllTimers() ?
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopLectureTask();
		stopQuestTimers();
		stopEnableUserRelationTask();
		getNevitSystem().stopTasksOnLogout();

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * Соединение закрывается, клиент закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void kick()
	{
		if(_connection != null)
		{
			_connection.close(LeaveWorld.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	/**
	 * Соединение не закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void restart()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	/**
	 * Соединение закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void logout()
	{
		if(_connection != null)
		{
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	private void prepareToLogout()
	{
		if(_isLogout.getAndSet(true))
			return;

		setNetConnection(null);
		setIsOnline(false);

		getListeners().onExit();

		if(isFlying() && !checkLandingState())
			_stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);

		if(isCastingNow())
			abortCast(true, true);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival."); //TODO [G1ta0] custom message
			leaveParty();
		}

		CursedWeaponsManager.getInstance().doLogout(this);

		if(_olympiadObserveGame != null)
			_olympiadObserveGame.removeSpectator(this);

		if(isInOlympiadMode())
			Olympiad.logoutPlayer(this);

		stopFishing();

		Servitor pet = getServitor();
		if(pet != null)
		{
			pet.unSummon(true, true);

			CharacterServitorDAO.getInstance().insert(this, pet);
		}

		if (isMounted())
			PetDAO.getInstance().updateMount(getMountObjId(), getMountCurrentFed());

		_friendList.notifyFriends(false);

		if(isProcessingRequest())
			getRequest().cancel();

		stopAllTimers();

		if(isInBoat())
			getBoat().removePlayer(this);

		SubUnit unit = getSubUnit();
		UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessage(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(this));
				else if(clanMember.getObjectId() == apprentice)
					clanMember.sendPacket(new SystemMessage(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_OUT).addName(this));
			}
			member.setPlayerInstance(this, true);
		}

		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onLogout(this);

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);

		MatchingRoom room = getMatchingRoom();
		if(room != null)
		{
			if(room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		}
		setMatchingRoom(null);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		destroyAllTraps();

		if(_decoy != null)
		{
			_decoy.deleteMe();
			_decoy = null;
		}

		for(NpcInstance npc : _tamedBeasts)
			npc.deleteMe();

		stopPvPFlag();

		Reflection ref = getReflection();

		if(ref != ReflectionManager.DEFAULT)
		{
			if(ref.getReturnLoc() != null)
				_stablePoint = ref.getReturnLoc();

			ref.removeObject(this);
		}

		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR><BR>
	 */
	public Collection<Recipe> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<Recipe> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final Recipe id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void registerRecipe(final Recipe recipe, boolean saveDB)
	{
		if(recipe == null)
			return;
		if(recipe.isDwarvenRecipe())
			_recipebook.put(recipe.getId(), recipe);
		else
			_commonrecipebook.put(recipe.getId(), recipe);
		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
	}

	// ------------------- Quest Engine ----------------------

	public QuestState getQuestState(int questId)
	{
		questRead.lock();
		try
		{
			return _quests.get(questId);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Quest quest)
	{
		return getQuestState(quest.getId());
	}

	public boolean isQuestCompleted(int quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public void addQuestState(QuestState qs)
	{
		questWrite.lock();
		try
		{
			_quests.put(qs.getQuest().getId(), qs);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(int quest)
	{
		questWrite.lock();
		try
		{
			_quests.remove(quest);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<Quest>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.values())
				if(qs.isStarted())
					quests.add(qs.getQuest());
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values().toArray(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
	{
		List<QuestState> states = new ArrayList<QuestState>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		QuestState qs;
		if(quests != null)
			for(Quest quest : quests)
			{
				qs = getQuestState(quest);
				if(qs != null && !qs.isCompleted())
					states.add(getQuestState(quest));
			}
		return states;
	}

	public void processQuestEvent(int questId, String event, NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(questId);
		if(qs == null)
		{
			Quest q = QuestManager.getQuest(questId);
			if(q == null)
			{
				_log.warn("Quest " + questId + " not found!");
				return;
			}
			qs = q.newQuestState(this, Quest.CREATED);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	/**
	 * Проверка на переполнение инвентаря и перебор в весе для квестов и эвентов
	 *
	 * @return true если ве проверки прошли успешно
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(getWeightPercents() >= 80 || getUsedInventoryPercents() >= 90 || Config.QUEST_INVENTORY_MAXIMUM * 0.9 < getInventory().getQuestSize())
		{
			if(msg)
				sendPacket(SystemMsg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	/**
	 * Останавливаем и запоминаем все квестовые таймеры
	 */
	public void stopQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
				qs.pauseQuestTimers();
			else
				qs.stopQuestTimers();
	}

	/**
	 * Восстанавливаем все квестовые таймеры
	 */
	public void resumeQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			qs.resumeQuestTimers();
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	/**
	 * Проверяет является ли этот персонаж владельцем крепости
	 *
	 * @param fortressId
	 * @return true если владелец
	 */
	public boolean isFortressLord(int fortressId)
	{
		return _clan != null && isClanLeader() && _clan.getHasFortress() == fortressId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(int value)
	{
		if(value > 255)
			_recomHave = 255;
		else if(value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}

	public int getRecomBonusTime()
	{
		if(_recomBonusTask != null)
			return (int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS));
		return _recomBonusTime;
	}

	public void setRecomBonusTime(int val)
	{
		_recomBonusTime = val;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public boolean isHourglassEffected()
	{
		return _isHourglassEffected;
	}

	public void setHourlassEffected(boolean val)
	{
		_isHourglassEffected = val;
	}

	public void startHourglassEffect()
	{
		setHourlassEffected(true);
		stopRecomBonusTask(true);
		sendVoteSystemInfo();
	}

	public void stopHourglassEffect()
	{
		setHourlassEffected(false);
		startRecomBonusTask();
		sendVoteSystemInfo();
	}

	public int addRecomLeft()
	{
		int recoms = 0;
		if(getRecomLeftToday() < 20)
			recoms = 10;
		else
			recoms = 1;
		setRecomLeft(getRecomLeft() + recoms);
		setRecomLeftToday(getRecomLeftToday() + recoms);
		sendUserInfo(true);
		return recoms;
	}

	public int getRecomLeftToday()
	{
		return _recomLeftToday;
	}

	public void setRecomLeftToday(final int value)
	{
		_recomLeftToday = value;
		setVar("recLeftToday", String.valueOf(_recomLeftToday), -1);
	}

	public void giveRecom(final Player target)
	{
		int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
			target.addRecomHave(1);
		if(getRecomLeft() > 0)
			setRecomLeft(getRecomLeft() - 1);

		sendUserInfo(true);
	}

	public void addRecomHave(final int val)
	{
		setRecomHave(getRecomHave() + val);
		broadcastUserInfo(true);
		sendVoteSystemInfo();
	}

	public int getRecomBonus()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoBonus(this);
		return 0;
	}

	public double getRecomBonusMul()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoMultiplier(this);
		return 1;
	}

	public void sendVoteSystemInfo()
	{
		sendPacket(new ExVoteSystemInfo(this));
	}

	public boolean isRecomTimerActive()
	{
		return _isRecomTimerActive;
	}

	public void setRecomTimerActive(boolean val)
	{
		if(_isRecomTimerActive == val)
			return;

		_isRecomTimerActive = val;

		if(val)
			startRecomBonusTask();
		else
			stopRecomBonusTask(true);

		sendVoteSystemInfo();
	}

	private ScheduledFuture<?> _recomBonusTask;

	public void startRecomBonusTask()
	{
		if(_recomBonusTask == null && getRecomBonusTime() > 0 && isRecomTimerActive() && !isHourglassEffected())
			_recomBonusTask = ThreadPoolManager.getInstance().schedule(new RecomBonusTask(this), getRecomBonusTime() * 1000);
	}

	public void stopRecomBonusTask(boolean saveTime)
	{
		if(_recomBonusTask != null)
		{
			if(saveTime)
				setRecomBonusTime((int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS)));
			_recomBonusTask.cancel(false);
			_recomBonusTask = null;
		}
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma, boolean updateSelf)
	{
		if(karma < 0)
			karma = 0;

		if(_karma == karma)
			return;

		_karma = karma;

		if(updateSelf)
			sendChanges();

		if(getServitor() != null)
			getServitor().broadcastCharInfo();
	}

	@Override
	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		// Fitted exponential curve to the data
		int con = getCON();
		if(con < 1)
			return (int) (31000 * Config.MAXLOAD_MODIFIER);
		else if(con > 59)
			return (int) (176000 * Config.MAXLOAD_MODIFIER);
		else
			return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null);
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	private void updateEffectIconsImpl()
	{
		Effect[] effects = getEffectList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());

		PartySpelled ps = null;
		if (_party != null)
			ps = new PartySpelled(this, false);

		AbnormalStatusUpdate mi = new AbnormalStatusUpdate();

		for(Effect effect : effects)
			if(effect.isInUse())
			{
				if(effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
					sendPacket(new ShortBuffStatusUpdate(effect));
				else
					effect.addIcon(mi);
				if(ps != null)
					effect.addPartySpelledIcon(ps);
			}

		sendPacket(mi);
		if(_party != null)
			_party.broadCast(ps);

		if(isInOlympiadMode() && isOlympiadCompStarted())
		{
			OlympiadGame olymp_game = _olympiadGame;
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfo olympiadSpelledInfo = new ExOlympiadSpelledInfo();

				for(Effect effect : effects)
					if(effect != null && effect.isInUse())
						effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);

				if(olymp_game.getType() == CompType.CLASSED || olymp_game.getType() == CompType.NON_CLASSED)
					for(Player member : olymp_game.getTeamMembers(this))
						member.sendPacket(olympiadSpelledInfo);

				for(Player member : olymp_game.getSpectators())
					member.sendPacket(olympiadSpelledInfo);
			}
		}

		final SingleMatchEvent event = getEvent(SingleMatchEvent.class);
		if (event != null)
			event.onEffectIconsUpdate(this, effects);
	}

	public int getUsedInventoryPercents()
	{
		return _usedInventoryPercents;
	}

	public int getWeightPercents()
	{
		return _weightPercents;
	}

	public int getWeightPenalty()
	{
		return _weightPenaltyLevel; //return getSkillLevel(4270, 0);
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted())
			return;

		final int maxLoad = getMaxLoad();
		if (maxLoad <= 0)
			return;

		int inventoryLimit = getInventoryLimit();
		if (inventoryLimit <= 0)
			inventoryLimit = 1;
		_usedInventoryPercents = (int)(100. * (double)getInventory().getSize() / inventoryLimit);

		int weightPercents = (int)(100. * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0, this, null)) / maxLoad);
		if (weightPercents < 0)
			weightPercents = 0;
		_weightPercents = weightPercents;

		int newWeightPenalty = 0;
		if (weightPercents > 100)
		{
			setOverloaded(true);
			newWeightPenalty = 4;
		}
		else
		{
			setOverloaded(false);
			if(weightPercents < 50)
				newWeightPenalty = 0;
			else if(weightPercents < 67)
				newWeightPenalty = 1;
			else if(weightPercents < 80)
				newWeightPenalty = 2;
			else
				newWeightPenalty = 3;
		}

		if (_weightPenaltyLevel == newWeightPenalty)
			return;
		_weightPenaltyLevel = newWeightPenalty;

		if(newWeightPenalty > 0)
			super.addSkill(SkillTable.getInstance().getSkillEntry(4270, newWeightPenalty));
		else
			super.removeSkill(getKnownSkill(4270));

		sendPacket(new SkillList(this));
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}

	public int getArmorsExpertisePenalty()
	{
		return _armorExpertisePenaltyLevel; //getSkillLevel(6213, 0);
	}

	public int getWeaponsExpertisePenalty()
	{
		return _weaponExpertisePenaltyLevel; //getSkillLevel(6209, 0);
	}

	public int getExpertisePenalty(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			return getWeaponsExpertisePenalty();
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public void refreshExpertisePenalty()
	{
		if(isLogoutStarted())
			return;

		// Calculate the current higher Expertise of the L2Player
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for(i = 0; i < EXPERTISE_LEVELS.length; i++)
			if(level < EXPERTISE_LEVELS[i + 1])
				break;

		boolean skillUpdate = false; // Для того, чтобы лишний раз не посылать пакеты
		// Add the Expertise skill corresponding to its Expertise level
		if(_expertiseIndex != i)
		{
			_expertiseIndex = i;
			if(_expertiseIndex > 0)
			{
				addSkill(SkillTable.getInstance().getSkillEntry(239, _expertiseIndex), false);
				skillUpdate = true;
			}
		}

		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		ItemInstance[] items = getInventory().getPaperdollItems();
		for(ItemInstance item : items)
			if(item != null)
			{
				int penalty = item.getCrystalType().ordinal() - _expertiseIndex;
				if (penalty <= 0)
					continue;

				switch (item.getTemplate().getType2())
				{
					case ItemTemplate.TYPE2_WEAPON:
						if(penalty > newWeaponPenalty)
							newWeaponPenalty = penalty;
						break;
					case ItemTemplate.TYPE2_SHIELD_ARMOR:
					case ItemTemplate.TYPE2_ACCESSORY:
						newArmorPenalty++;
						break;
				}
			}

		if(newWeaponPenalty > 4)
			newWeaponPenalty = 4;

		if(newArmorPenalty > 4)
			newArmorPenalty = 4;

		if(_weaponExpertisePenaltyLevel != newWeaponPenalty)
		{
			_weaponExpertisePenaltyLevel = newWeaponPenalty;
			if(newWeaponPenalty > 0)
				super.addSkill(SkillTable.getInstance().getSkillEntry(6209, _weaponExpertisePenaltyLevel));
			else
				super.removeSkill(getKnownSkill(6209));
			skillUpdate = true;
		}
		if(_armorExpertisePenaltyLevel != newArmorPenalty)
		{
			_armorExpertisePenaltyLevel = newArmorPenalty;
			if(newArmorPenalty > 0)
				super.addSkill(SkillTable.getInstance().getSkillEntry(6213, _armorExpertisePenaltyLevel));
			else
				super.removeSkill(getKnownSkill(6213));
			skillUpdate = true;
		}

		if(skillUpdate)
		{
			getInventory().validateItemsSkills();

			sendPacket(new SkillList(this));
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public ClassId getClassId()
	{
		return getTemplate().classId;
	}

	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 2)
			_clan.incReputation(100, true, "Academy");
		else if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 3)
		{
			int earnedPoints = 0;
			if(getLvlJoinedAcademy() <= 16)
				earnedPoints = 650;
			else if(getLvlJoinedAcademy() >= 39)
				earnedPoints = 190;
			else
				earnedPoints = 650 - (getLvlJoinedAcademy() - 16) * 20;

			_clan.removeClanMember(getObjectId());

			SystemMessage sm = new SystemMessage(SystemMsg.CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS);
			sm.addName(this);
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);

			setClan(null);
			setTitle("");
			sendPacket(SystemMsg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN);
			setLeaveClanTime(0);

			broadcastCharInfo();

			sendPacket(PledgeShowMemberListDeleteAll.STATIC);

			ItemFunctions.addItem(this, 8181, 1);
		}
	}

	/**
	 * Set the template of the L2Player.
	 *
	 * @param id The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	public synchronized void setClassId(final int id, boolean noban, boolean fromQuest)
	{
		if(!noban && !(ClassId.VALUES[id].equalsOrChildOf(ClassId.VALUES[getActiveClassId()]) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS))
		{
			Thread.dumpStack();
			return;
		}

		//Если новый ID не принадлежит имеющимся классам значит это новая профа
		if(!getSubClasses().containsKey(id))
		{
			final SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if(cclass.isBase())
			{
				setBaseClass(id);
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if(ClassId.VALUES[id].getLevel() == 2)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8869);
					unsetVar("newbieweapon");
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
					unsetVar("ng1");
					unsetVar("ng2");
					unsetVar("ng3");
					unsetVar("ng4");
				}
				else if(ClassId.VALUES[id].getLevel() == 3)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8870);
					unsetVar("newbiearmor");
					unsetVar("dd1"); // удаляем отметки о выдаче дименшен даймондов
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}

				if(coupons != null)
				{
					coupons.setCount(15);
					sendPacket(SystemMessage.obtainItems(coupons));
					getInventory().addItem(coupons);
				}
			}

			// Выдача Holy Pomander
			switch(ClassId.VALUES[id])
			{
				case cardinal:
					ItemFunctions.addItem(this, 15307, 1);
					break;
				case evaSaint:
					ItemFunctions.addItem(this, 15308, 1);
					break;
				case shillienSaint:
					ItemFunctions.addItem(this, 15309, 4);
					break;
			}

			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			rewardSkills(true);
			storeCharSubClasses();

			if(fromQuest)
			{
				// Социалка при получении профы
				broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
				sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			}
			broadcastCharInfo();
		}

		PlayerTemplate t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1);
		if(t == null)
		{
			_log.error("Missing template for classId: " + id);
			// do not throw error - only print error
			return;
		}

		// Set the template of the L2Player
		_template = t;

		// Update class icon in party and clan
		if(isInParty())
			getParty().broadCast(new PartySmallWindowUpdate(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);
	}

	public long getExp()
	{
		return _activeClass == null ? 0 : _activeClass.getExp();
	}

	public long getMaxExp()
	{
		return _activeClass == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setFistsWeaponItem(final WeaponTemplate weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	public WeaponTemplate getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	public WeaponTemplate findFistsWeaponItem(final int classId)
	{
		//human fighter fists
		if(classId >= 0x00 && classId <= 0x09)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(246);

		//human mage fists
		if(classId >= 0x0a && classId <= 0x11)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(251);

		//elven fighter fists
		if(classId >= 0x12 && classId <= 0x18)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(244);

		//elven mage fists
		if(classId >= 0x19 && classId <= 0x1e)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(249);

		//dark elven fighter fists
		if(classId >= 0x1f && classId <= 0x25)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(245);

		//dark elven mage fists
		if(classId >= 0x26 && classId <= 0x2b)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(250);

		//orc fighter fists
		if(classId >= 0x2c && classId <= 0x30)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(248);

		//orc mage fists
		if(classId >= 0x31 && classId <= 0x34)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(252);

		//dwarven fists
		if(classId >= 0x35 && classId <= 0x39)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(247);

		return null;
	}

	public void addExpAndCheckBonus(MonsterInstance mob, final double noRateExp, double noRateSp, double partyVitalityMod)
	{
		if(_activeClass == null)
			return;

		// Начисление душ камаэлям
		double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0, mob, null);
		if(neededExp > 0 && noRateExp > neededExp)
		{
			mob.broadcastPacket(new SpawnEmitter(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000);
		}

		double vitalityBonus = 0.;
		int npcLevel = mob.getLevel();
		if(Config.ALT_VITALITY_ENABLED)
		{
			boolean blessActive = getNevitSystem().isBlessingActive();
			vitalityBonus = mob.isRaid() ? 0. : getVitalityLevel(blessActive) / 2.;
			vitalityBonus *= Config.ALT_VITALITY_RATE;
			if(noRateExp > 0)
			{
				if(!mob.isRaid())
				{
					// TODO: Разобратся, нельзя предметы использовать, или предметы не будут давать эффекта?
					// (Все предметы для восполнения или поддержания энергии не действуют во время действия Нисхождения Невитта)
					if(!(getVarB("NoExp") && getExp() == Experience.LEVEL[getLevel() + 1] - 1))
					{
						double points = ((noRateExp / (npcLevel * npcLevel)) * 100) / 9;
						points *= Config.ALT_VITALITY_CONSUME_RATE;

						if(blessActive || getEffectList().getEffectByType(EffectType.Vitality) != null)
							points = -points;

							setVitality(getVitality() - points * partyVitalityMod);
					}
				}
				else
					setVitality(getVitality() + Config.ALT_VITALITY_RAID_BONUS);
			}
		}

		//При первом вызове, активируем таймеры бонусов.
		if(!isInPeaceZone())
		{
			setRecomTimerActive(true);
			getNevitSystem().startAdventTask();
			if((getLevel() - npcLevel) <= 9)
			{
				int nevitPoints = (int) Math.round(((noRateExp / (npcLevel * npcLevel)) * 100) / 20); //TODO: Формула от балды.
				getNevitSystem().addPoints(nevitPoints);
			}
		}

		long normalExp = (long)  (noRateExp * ((Config.RATE_XP * getRateExp() + vitalityBonus) * getRecomBonusMul()));
		long normalSp = (long)  (noRateSp * (Config.RATE_SP * getRateSp() + vitalityBonus));

		long expWithoutBonus = (long)  (noRateExp * Config.RATE_XP * getRateExp());
		long spWithoutBonus = (long)  (noRateSp * Config.RATE_SP * getRateSp());

		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true);
	}

	@Override
	public void addExpAndSp(long exp, long sp)
	{
		addExpAndSp(exp, sp, 0, 0, false, false);
	}

	public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet)
	{
		if(_activeClass == null)
			return;

		if(applyRate)
		{
			addToExp *= Config.RATE_XP * getRateExp();
			addToSp *= Config.RATE_SP * getRateSp();
		}

		Servitor pet = getServitor();
		if(addToExp > 0)
		{
			if(applyToPet)
			{
				if(pet != null && !pet.isDead() && !PetDataTable.isVitaminPet(pet.getNpcId()))
					// Sin Eater забирает всю экспу у персонажа
					if(pet.getNpcId() == PetDataTable.SIN_EATER_ID)
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.isPet() && pet.getExpPenalty() > 0f)
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1. - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.), 0);
							addToExp *= 1. - pet.getExpPenalty() / 5.;
						}
					else if(pet.isSummon())
						addToExp *= 1. - pet.getExpPenalty();
			}

			// Remove Karma when the player kills L2MonsterInstance
			//TODO [G1ta0] двинуть в метод начисления наград при убйистве моба
			if(!isCursedWeaponEquipped() && addToSp > 0 && _karma > 0)
				setKarma(_karma -= addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP), false);

			long max_xp = getVarB("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}

		int oldLvl = _activeClass.getLevel();

		_activeClass.addExp(addToExp);
		_activeClass.addSp(addToSp);

		if(addToExp > 0 && addToSp > 0 && (bonusAddExp > 0 || bonusAddSp > 0))
			sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addNumber(addToExp).addNumber(bonusAddExp).addNumber(addToSp).addNumber((int)bonusAddSp));
		else if(addToSp > 0 && addToExp == 0)
			sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_ACQUIRED_S1_SP).addNumber(addToSp));
		else if(addToSp > 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));
		else if(addToSp == 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1_EXPERIENCE).addNumber(addToExp));

		int level = _activeClass.getLevel();
		if(level != oldLvl)
		{
			int levels = level - oldLvl;
			if(levels > 0)
				getNevitSystem().addPoints(1950);
			levelSet(levels);
		}

		if(pet != null && pet.isPet() && PetDataTable.isVitaminPet(pet.getNpcId()))
		{
			PetInstance _pet = (PetInstance) pet;
			_pet.setLevel(getLevel());
			_pet.setExp(_pet.getExpForNextLevel());
			_pet.broadcastStatusUpdate();
		}

		updateStats();
	}


	/**
	 * Give Expertise skill of this level.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2Player </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 * @param send
	 */
	private void rewardSkills(boolean send)
	{
		boolean update = false;
		if(Config.AUTO_LEARN_SKILLS)
		{
			int unLearnable = 0;
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			while(skills.size() > unLearnable)
			{
				unLearnable = 0;
				for(SkillLearn s : skills)
				{
					SkillEntry sk = SkillTable.getInstance().getSkillEntry(s.getId(), s.getLevel());
					if(sk == null || !sk.getTemplate().getCanLearn(getClassId()) || !s.canAutoLearn())
					{
						unLearnable++;
						continue;
					}
					addSkill(sk, true);
				}
				skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			}
			update = true;
		}
		else
			// Скиллы дающиеся бесплатно не требуют изучения
			for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL))
				if(skill.getCost() == 0 && skill.getItemId() == 0)
				{
					SkillEntry sk = SkillTable.getInstance().getSkillEntry(skill.getId(), skill.getLevel());
					addSkill(sk, true);
					if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
						for(ShortCut sc : getAllShortCuts())
							if(sc.getId() == sk.getId() && sc.getType() == ShortCut.TYPE_SKILL)
							{
								ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
								sendPacket(new ShortCutRegister(this, newsc));
								registerShortCut(newsc);
							}
					update = true;
				}

		if(send && update)
			sendPacket(new SkillList(this));

		updateStats();
	}

	public Race getRace()
	{
		return getBaseTemplate().race;
	}

	public int getIntSp()
	{
		return (int) getSp();
	}

	public long getSp()
	{
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(long sp)
	{
		if(_activeClass != null)
			_activeClass.setSp(sp);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
	}

	/** Общее время пребывания в игре в мс */
	public long getOnlineTime()
	{
		return _onlineTime + getUptime();
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	/** Текущее время пребывания в игре в мс */
	public long getUptime()
	{
		return _uptime == 0 ? 0 : System.currentTimeMillis() - _uptime;
	}

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
			_NoChannel = -1;

		if(_NoChannel > 0)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0;
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
	}

	public void updateNoChannel(final long time)
	{
		setNoChannel(time);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.error("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		sendPacket(new EtcStatusUpdate(this));
	}

	public boolean canTalkWith(Player player)
	{
		return _NoChannel >= 0 || player == this;
	}

	public Deque<ChatMsg> getMessageBucket()
	{
		return _msgBucket;
	}

	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Clan.JOIN_PLEDGE_PENALTY)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Clan.CREATE_PLEDGE_PENALTY)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IBroadcastPacket canJoinParty(Player inviter)
	{
		Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY; // занят
		if(isBlockAll() || getMessageRefusal()) // всех нафиг
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE;
		if(isInParty()) // уже
			return new SystemMessage(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(inviter.getReflection() != getReflection()) // в разных инстантах
			if(inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT)
				return SystemMsg.INVALID_TARGET;
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) // зарич
			return SystemMsg.INVALID_TARGET;
		if(inviter.isInOlympiadMode() || isInOlympiadMode()) // олимпиада
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS;
		return null;
	}

	public IBroadcastPacket canUseItem(ItemInstance item, boolean force)
	{
		if (item.getTemplate().isForPet())
			return SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM;

		if (isInStoreMode())
			return PetDataTable.isPetControlItem(item) ? SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE : SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP;

		if (isInTrade())
			return SystemMsg.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING;

		IBroadcastPacket result;
		for (Event e : getEvents())
		{
			result = e.canUseItem(this, item);
			if (result != null)
				return result;
		}

		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight()
	{
		return _freight;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(StaticObjectInstance throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving)
		{
			getAI().setNextAction(nextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

		if(throne == null)
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
		else
			broadcastPacket(new ChairSit(this, throne));

		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead())
			return;

		//FIXME [G1ta0] эффект сам отключается во время действия, если персонаж не сидит, возможно стоит убрать
		getEffectList().stopAllSkillEffects(EffectType.Relax);

		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));

		_sittingObject = null;
		setSitting(false);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500);
	}

	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
			_waitTimeWhenSit += 2;
	}

	public int getWaitSitTime()
	{
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime()
	{
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public ItemContainer getRefund()
	{
		return _refund;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena)
	{
		return reduceAdena(adena, false);
	}

	/**
	 * Забирает адену у игрока.<BR><BR>
	 *
	 * @param adena  - сколько адены забрать
	 * @param notify - отображать системное сообщение
	 * @return true если сняли
	 */
	public boolean reduceAdena(long adena, boolean notify)
	{
		if(adena < 0)
			return false;
		if(adena == 0)
			return true;
		boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
			sendPacket(SystemMessage.removeItems(ItemTemplate.ITEM_ID_ADENA, adena));
		return result;
	}

	public ItemInstance addAdena(long adena)
	{
		return addAdena(adena, false);
	}

	/**
	 * Добавляет адену игроку.<BR><BR>
	 *
	 * @param adena  - сколько адены дать
	 * @param notify - отображать системное сообщение
	 * @return L2ItemInstance - новое количество адены
	 */
	public ItemInstance addAdena(long adena, boolean notify)
	{
		if(adena < 1)
			return null;
		ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
			sendPacket(SystemMessage.obtainItems(ItemTemplate.ITEM_ID_ADENA, adena, 0));
		return item;
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		GameClient conn = getNetConnection();
		return conn != null && conn.isConnected();
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, Player.class, this, true))
			return;

		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), 0)); // The color to display in the select window is White
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			if(!isInRangeZ(player, INTERACTION_DISTANCE) && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				else
					player.sendPacket(ActionFail.STATIC);
			}
			else
				player.doInteract(this);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player != this)
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else
					player.sendPacket(ActionFail.STATIC);
			}
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else
			player.sendPacket(ActionFail.STATIC);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate()) //По идее еше должно срезать траффик. Будут глюки с отображением - убрать это условие.
			return;

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.MAX_CP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP, StatusUpdate.CUR_CP);
		sendPacket(su);

		// Check if a party is in progress
		if(isInParty())
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		SingleMatchEvent duelEvent = getEvent(SingleMatchEvent.class);
		if(duelEvent != null)
			duelEvent.onStatusUpdate(this);

		if(isInOlympiadMode() && isOlympiadCompStarted())
		{
			if(_olympiadGame != null)
				_olympiadGame.broadcastInfo(this, null, false);
		}
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastUserInfo(false);
	}

	/**
	 * Отправляет UserInfo даному игроку и CharInfo всем окружающим.<BR><BR>
	 * <p/>
	 * <B><U> Концепт</U> :</B><BR><BR>
	 * Сервер шлет игроку UserInfo.
	 * Сервер вызывает метод {@link Creature#broadcastPacketToOthers(org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket...)} для рассылки CharInfo<BR><BR>
	 * <p/>
	 * <B><U> Действия</U> :</B><BR><BR>
	 * <li>Отсылка игроку UserInfo(личные и общие данные)</li>
	 * <li>Отсылка другим игрокам CharInfo(Public data only)</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Внимание</U> : НЕ ПОСЫЛАЙТЕ UserInfo другим игрокам либо CharInfo даному игроку.<BR>
	 * НЕ ВЫЗЫВАЕЙТЕ ЭТОТ МЕТОД КРОМЕ ОСОБЫХ ОБСТОЯТЕЛЬСТВ(смена сабкласса к примеру)!!! Траффик дико кушается у игроков и начинаются лаги.<br>
	 * Используйте метод {@link Player#sendChanges()}</B></FONT><BR><BR>
	 */
	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(!isVisible() || isInvisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
			force = true;

		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	private int _polyNpcId;

	public void setPolyId(int polyid)
	{
		_polyNpcId = polyid;

		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	public boolean isPolymorphed()
	{
		return _polyNpcId != 0;
	}

	public int getPolyId()
	{
		return _polyNpcId;
	}

	private void broadcastCharInfoImpl()
	{
		if(!isVisible() || isInvisible())
			return;

		L2GameServerPacket ci = isPolymorphed() ? new NpcInfo(this) : new CharInfo(this);
		L2GameServerPacket exCi = new ExBR_ExtraUserInfo(this);
		L2GameServerPacket dominion = null;
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		// пакетка розсыдается в том случаии, если идет ТВ, или ТВ неначалось а игрок изменил имья(Защитик Замка)
		if(siegeEvent != null && (siegeEvent.isInProgress() || siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			dominion = new ExDominionWarStart(this);

		for(Player target : World.getAroundObservers(this))
		{
			target.sendPacket(ci, exCi);
			target.sendPacket(new RelationChanged().add(this, target));
			if(dominion != null)
				target.sendPacket(dominion);
		}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;

		sendPacket(new EtcStatusUpdate(this));
	}

	private Future<?> _userInfoTask;

	private class UserInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UserInfo(this), new ExBR_ExtraUserInfo(this));
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null && (dominionSiegeEvent.isInProgress() || dominionSiegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			sendPacket(new ExDominionWarStart(this));
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	@Override
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for(int field : fields)
			switch(field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.CUR_LOAD:
					su.addAttribute(field, getCurrentLoad());
					break;
				case StatusUpdate.MAX_LOAD:
					su.addAttribute(field, getMaxLoad());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, _pvpFlag);
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
			}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdate su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;

		List<IBroadcastPacket> packets = new ArrayList<IBroadcastPacket>(withPet ? 2 : 1);
		if(withPet && getServitor() != null)
			packets.add(getServitor().makeStatusUpdate(fields));

		packets.add(su);

		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IBroadcastPacket p)
	{
		if(!isConnected() || p == null)
			return;

		L2GameServerPacket gsp = p.packet(this);
		if(gsp == null)
			return;

		_connection.sendPacket(gsp);
	}

	@Override
	public void sendPacket(IBroadcastPacket... packets)
	{
		if(!isConnected())
			return;

		for(IBroadcastPacket p : packets)
		{
			if(p == null)
				continue;
			L2GameServerPacket gsp = p.packet(this);
			if(gsp == null)
				continue;

			_connection.sendPacket(gsp);
		}
	}

	@Override
	public void sendPacket(List<? extends IBroadcastPacket> packets)
	{
		if(!isConnected())
			return;

		for(IBroadcastPacket p : packets)
		{
			if(p == null)
				continue;
			L2GameServerPacket gsp = p.packet(this);
			if(gsp == null)
				continue;

			_connection.sendPacket(gsp);
		}
	}

	public void doInteract(GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(isInRangeZ(target, INTERACTION_DISTANCE))
			{
				Player temp = (Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc, boolean forceAutoLoot)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced() || forceAutoLoot;

		if((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		// Herbs
		if(item.isHerb())
		{
			if(!_autoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			SkillEntry[] skills = item.getTemplate().getAttachedSkills();
			if(skills.length > 0)
				for(SkillEntry skill : skills)
				{
					altUseSkill(skill, this);
					if(getServitor() != null && getServitor().isSummon() && !getServitor().isDead())
						getServitor().altUseSkill(skill, getServitor());
				}
			item.deleteMe();
			return;
		}

		if(_autoLoot == AUTO_LOOT_NONE && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(!pickupItem(item, ItemLog.Pickup))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
		}
		else
			getParty().distributeItem(this, item, fromNpc);

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		sendActionFailed();
		stopMove();

		ItemInstance item = (ItemInstance) object;

		synchronized(item)
		{
			if(!item.isVisible())
				return;

			// Check if me not owner of item and, if in party, not in owner party and nonowner pickup delay still active
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					sm = new SystemMessage(SystemMsg.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMsg.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			// Herbs
			if(item.isHerb())
			{
				SkillEntry[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
					for(SkillEntry skill : skills)
					{
						altUseSkill(skill, this);
						if(getServitor() != null && getServitor().isSummon() && !getServitor().isDead())
							getServitor().altUseSkill(skill, getServitor());
					}

				broadcastPacket(new GetItem(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;

			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, ItemLog.Pickup))
				{
					broadcastPacket(new GetItem(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
				getParty().distributeItem(this, item, null);
		}
	}

	public boolean pickupItem(ItemInstance item, ItemLog logType)
	{
		if (isInTrade())
		{
			sendPacket(SystemMsg.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING);
			return false;
		}

		if(!ItemFunctions.canAddItem(this, item))
			return false;

		if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA || item.getItemId() == 6353)//FIXME [G1ta0] хардкод
			processQuestEvent(255, "CE" + item.getItemId(), null);


		Log.LogItem(this, logType, item);
		sendPacket(SystemMessage.obtainItems(item));
		getInventory().addItem(item);

		if(item.getAttachment() instanceof PickableAttachment)
			((PickableAttachment) item.getAttachment()).pickUp(this);

		sendChanges();
		return true;
	}

	public void setObjectTarget(GameObject target)
	{
		setTarget(target);
		if(target == null)
			return;

		if(target == getTarget())
		{
			if(target.isNpc())
			{
				NpcInstance npc = (NpcInstance) target;
				sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
				sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
				sendPacket(new ValidateLocation(npc), ActionFail.STATIC);
			}
			else
				sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		}
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;

		// Can't target and attack festival monsters if not participant
		if(newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant())
			newTarget = null;

		Party party = getParty();

		// Can't target and attack rift invaders if not in the same room
		if(party != null && party.isInDimensionalRift())
		{
			int riftType = party.getDimensionalRift().getType();
			int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if(newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				newTarget = null;
		}

		GameObject oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
				return;

			// Remove the L2Player from the _statusListener of the old target if it was a L2Character
			if(oldTarget.isCreature())
				((Creature) oldTarget).removeStatusListener(this);

			broadcastPacket(new TargetUnselected(this));
		}

		if(newTarget != null)
		{
			// Add the L2Player to the _statusListener of the new target if it's a L2Character
			if(newTarget.isCreature())
				((Creature) newTarget).addStatusListener(this);

			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
		}

		super.setTarget(newTarget);
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		return (WeaponTemplate) weapon.getTemplate();
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		final ItemTemplate item = weapon.getTemplate();

		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;

		return null;
	}

	public boolean isWearingArmor(final ArmorType armorType)
	{
		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(chest == null)
			return armorType == ArmorType.NONE;

		if(chest.getItemType() != armorType)
			return false;

		if(chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return true;

		final ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);

		return legs == null ? armorType == ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, SkillEntry skill, int poleHitCount, boolean crit, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(damage, attacker, skill, poleHitCount, crit, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, SkillEntry skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(standUp)
		{
			standUp();
			if(isFakeDeath())
				breakFakeDeath();
		}

		if(attacker.isPlayable())
		{
			if(!directHp && getCurrentCp() > 0)
			{
				double cp = getCurrentCp();
				if(cp >= damage)
				{
					cp -= damage;
					damage = 0;
				}
				else
				{
					damage -= cp;
					cp = 0;
				}

				setCurrentCp(cp);
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	public void onMagicUseTimer(Creature aimingTarget, SkillEntry skillEntry, boolean forceUse)
	{
		if(!(skillEntry.getTemplate().isHandler() || skillEntry.getTemplate().isTrigger() || isGM()))
		{
			if (!getAllSkills().contains(skillEntry))
			{
				onCastEndTime(skillEntry);
				return;
			}
		}

		super.onMagicUseTimer(aimingTarget, skillEntry, forceUse);
	}

	@Override
	public boolean isAlikeDead()
	{
		return _fakeDeath == EffectFakeDeath.FAKE_DEATH_ON || super.isAlikeDead();
	}

	@Override
	public boolean isMovementDisabled()
	{
		return isFakeDeath() || super.isMovementDisabled();
	}

	@Override
	public boolean isActionsDisabled()
	{
		return isFakeDeath() || super.isActionsDisabled();
	}

	@Override
	public void doAttack(Creature target)
	{
		if (isFakeDeath() || isInMountTransform())
			return;

		super.doAttack(target);
	}

	@Override
	public void onHitTimer(Creature target, int damage, int poleHitCount, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isFakeDeath())
		{
			sendActionFailed();
			return;
		}

		super.onHitTimer(target, damage, poleHitCount, crit, miss, soulshot, shld, unchargeSS);
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath != EffectFakeDeath.FAKE_DEATH_OFF;
	}

	public void setFakeDeath(int value)
	{
		_fakeDeath = value;
	}

	public void breakFakeDeath()
	{
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
	}

	private void altDeathPenalty(final Creature killer)
	{
		// Reduce the Experience of the L2Player in function of the calculated Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle())
			return;
		if(getNevitSystem().isBlessingActive())
			return;
		deathPenalty(killer);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final Player killer)
	{
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

		// Calculate the level difference Multiplier between attacker and killed L2Player
		//final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // * lvlDiffMulti);
		killer.setPkKills(killer.getPkKills() + 1);
	}

	public final void doKillInPeace(final Player killer) // Check if the L2Player killed haven't Karma
	{
		final SiegeEvent<?, ?> evt = killer.getEvent(SiegeEvent.class);
		if (evt != null && !evt.canPK(this, killer))
			return;

		if(_karma <= 0)
			doPurePk(killer);
		else
			killer.setPvpKills(killer.getPvpKills() + 1);
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); i++)
			array.add(items.remove(Rnd.get(items.size())));
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer)
	{
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onDeath(this, killer);

		if(killer == null || killer == _servitor || killer == this)
			return;

		if(isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof Servitor && (killer = killer.getPlayer()) == null)
			return;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer())
		{
			final Player pk = (Player) killer;
			final int repValue = getLevel() - pk.getLevel() >= 20 ? 2 : 1;
			boolean war = atMutualWarWith(pk);

			//TODO [VISTALL] fix it
			if(war /*|| _clan.getSiege() != null && _clan.getSiege() == pk.getClan().getSiege() && (_clan.isDefender() && pk.getClan().isAttacker() || _clan.isAttacker() && pk.getClan().isDefender())*/)
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					//_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMsg.YOUR_CLAN_MEMBER_C1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDICTED_FROM_YOUR_CLAN_REPUTATION_SCORE).addName(this).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					//pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMsg.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
					_clan.incReputation(-repValue, true, "ClanWar");
					pk.getClan().incReputation(repValue, true, "ClanWar");
				}

			if(isOnSiegeField())
				return;

			if(_pvpFlag > 0 || war)
				pk.setPvpKills(pk.getPvpKills() + 1);
			else
				doKillInPeace(pk);

			pk.sendChanges();
		}

		int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);

		// в нормальных условиях вещи теряются только при смерти от гварда или игрока
		// кроме того, альт на потерю вещей при сметри позволяет терять вещи при смтери от монстра
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(killer.isMonster() && !Config.DROP_ITEMS_ON_DIE // если убил монстр и альт выключен
				|| isPvP // если убил игрок или гвард и
				&& (_pkKills < Config.MIN_PK_TO_ITEMS_DROP // количество пк слишком мало
				|| karma == 0 && Config.KARMA_NEEDED_TO_DROP) // кармы нет
				|| isFestivalParticipant() // в фестивале вещи не теряются
				|| !killer.isMonster() && !isPvP) // в прочих случаях тоже
			return;

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return;

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // базовый шанс в процентах
		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;

		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					dropItemCount++;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					dropEquipCount++;
				else
					dropWeaponCount++;
			}

		List<ItemInstance> drop = new LazyArrayList<ItemInstance>(), // общий массив с результатами выбора
				dropItem = new LazyArrayList<ItemInstance>(), dropEquip = new LazyArrayList<ItemInstance>(), dropWeapon = new LazyArrayList<ItemInstance>(); // временные

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
					continue;

				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
					dropWeapon.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					dropEquip.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_OTHER)
					dropItem.add(item);
			}

			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);

			// Dropping items, if present
			if(drop.isEmpty())
				return;

			for(ItemInstance item : drop)
			{
				item = getInventory().removeItem(item);
				Log.LogItem(this, ItemLog.PvPDrop, item);

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_DROPPED_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				if(killer.isPlayable() && ((!Config.AUTO_LOOT && Config.AUTO_LOOT_PK) || this.isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item);
					Log.LogItem(this, ItemLog.Pickup, item);

					killer.getPlayer().sendPacket(SystemMessage.obtainItems(item));
				}
				else
					item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		//Check for active charm of luck for death penalty
		getDeathPenalty().checkCharmOfLuck();

		if(isInStoreMode())
			setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				sendPacket(SendTradeDone.FAIL);
				parthner.sendPacket(SendTradeDone.FAIL);
			}
			request.cancel();
		}

		setAgathion(0);

		boolean checkPvp = true;
		if(Config.ALLOW_CURSED_WEAPONS)
		{
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			}
			else if(killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}
		}

		if(checkPvp)
		{
			doPKPVPManage(killer);

			altDeathPenalty(killer);
		}

		//And in the end of process notify death penalty that owner died :)
		getDeathPenalty().notifyDead(killer);

		setIncreasedForce(0);

		if(isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift)
			((DimensionalRift) getParty().getReflection()).memberDead(this);

		stopWaterTask();
		stopMountFeedTask();

		if(!isSalvation() && isOnSiegeField() && isCharmOfCourage())
		{
			ask(new ConfirmDlg(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(100, false, 60000));
			setCharmOfCourage(false);
		}

		if(getLevel() < 6)
			processQuestEvent(255, "CE30", null);

		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		int lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100), 0);
	}

	public void deathPenalty(Creature killer)
	{
		if(killer == null)
			return;
		final boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());

		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if(deathPenaltyBonus < 2)
			deathPenaltyBonus = 1;
		else
			deathPenaltyBonus = deathPenaltyBonus / 2;

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = 8.0;

		int level = getLevel();
		if(level >= 79)
			percentLost = 1.0;
		else if(level >= 78)
			percentLost = 1.5;
		else if(level >= 76)
			percentLost = 2.0;
		else if(level >= 40)
			percentLost = 4.0;

		if(Config.ALT_DEATH_PENALTY)
			percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;

		if(isFestivalParticipant() || atwar)
			percentLost = percentLost / 4.0;

		// Calculate the Experience loss
		int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100);
		lostexp *= deathPenaltyBonus;

		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);

		// На зарегистрированной осаде нет потери опыта, на чужой осаде - как при обычной смерти от *моба*
		if(isOnSiegeField())
		{
			SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null)
				lostexp = 0;

			if(siegeEvent != null)
			{
				List<Effect> effect = getEffectList().getEffectsBySkillId(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
				if(effect != null)
				{
					int syndromeLvl = effect.get(0).getSkill().getLevel();
					if(syndromeLvl < 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						SkillEntry skill = SkillTable.getInstance().getSkillEntry(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
						skill.getEffects(this, this, false, false);
					}
					else if(syndromeLvl == 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						SkillEntry skill = SkillTable.getInstance().getSkillEntry(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
						skill.getEffects(this, this, false, false);
					}
				}
				else
				{
					SkillEntry skill = SkillTable.getInstance().getSkillEntry(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
					skill.getEffects(this, this, false, false);
				}
			}
		}

		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();

		if(lost > 0)
			setVar("lostexp", String.valueOf(lost), -1);
	}

	public void setRequest(Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	/**
	 * Проверка, занят ли игрок для ответа на зарос
	 *
	 * @return true, если игрок не может ответить на запрос
	 */
	public boolean isBusy()
	{
		return isProcessingRequest() || isOutOfControl() || getMessageRefusal() || isBlockAll() || isInvisible();
	}

	public boolean isProcessingRequest()
	{
		if(_request == null)
			return false;
		if(!_request.isInProgress())
			return false;
		return true;
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
	}

	public List<L2GameServerPacket> addVisibleObject(GameObject object, Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible() || object.isObservePoint())
			return Collections.emptyList();

		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(isInvisible() && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();

		if(isInStoreMode() && forPlayer.getVarB(NO_TRADERS_VAR))
			return Collections.emptyList();

		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		list.add(isPolymorphed() ? new NpcInfo(this) : new CharInfo(this));

		list.add(new ExBR_ExtraUserInfo(this));

		if(isSitting() && _sittingObject != null)
			list.add(new ChairSit(this, _sittingObject));

		if(isInStoreMode())
			list.add(getPrivateStoreMsgPacket(forPlayer));

		if(isCastingNow())
		{
			Creature castingTarget = getCastingTarget();
			SkillEntry castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
		}

		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));

		list.add(new RelationChanged().add(this, forPlayer));
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null && (dominionSiegeEvent.isInProgress() || dominionSiegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			list.add(new ExDominionWarStart(this));

		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		else
		{
			if(isMoving || isFollow)
				list.add(movePacket());
		}

		// VISTALL: во время ездовой трансформы, нужно послать второй раз при появлении обьекта
		// DS: для магазина то же самое, иначе иногда не виден после входа в игру
		if(isInMountTransform() || (isInStoreMode() && entering))
		{
			list.add(new CharInfo(this));
			list.add(new ExBR_ExtraUserInfo(this));
		}

		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || object.isObservePoint()) // FIXME  || isTeleporting()
			return Collections.emptyList();

		if (object == getServitor() && isTeleporting()) // не удаляем пета у игрока при телепорте
			return Collections.emptyList();

		List<L2GameServerPacket> result = list == null ? object.deletePacketList(this) : list;

		if(!isInObserverMode())
			getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);

		return result;
	}

	private void levelSet(int levels)
	{
		if(levels > 0)
		{
			sendPacket(SystemMsg.YOUR_LEVEL_HAS_INCREASED);
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());

			processQuestEvent(255, "CE40", null);
		}
		else if(levels < 0)
			if(Config.ALT_REMOVE_SKILLS_ON_DELEVEL)
				checkSkills();

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));

		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);

		// Give Expertise skill of this level
		rewardSkills(true);
	}

	/**
	 * Удаляет все скиллы, которые учатся на уровне большем, чем текущий+maxDiff
	 */
	public void checkSkills()
	{
		for(SkillEntry sk : getAllSkillsArray())
			SkillTreeTable.checkSkill(this, sk);
	}

	public void startTimers()
	{
		startAutoSaveTask();
		startPcBangPointsTask();
		startHourlyTask();
		startBonusTask();
		getInventory().startTimers();
		resumeQuestTimers();
	}

	public void stopAllTimers()
	{
		setAgathion(0);
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopKickTask();
		stopMountFeedTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		getInventory().stopAllTimers();
		stopQuestTimers();
		stopLectureTask();
		stopEnableUserRelationTask();
		getNevitSystem().stopTasksOnLogout();

		Future<?> task = _increasedForceCleanupTask;
		if (task != null)
		{
			task.cancel(false);
			_increasedForceCleanupTask = null;
		}

		task = _consumedSoulsCleanupTask;
		if (task != null)
		{
			task.cancel(false);
			_consumedSoulsCleanupTask = null;
		}
	}

	@Override
	public Servitor getServitor()
	{
		return _servitor;
	}

	public void setServitor(Servitor servitor)
	{
		boolean isPet = false;
		if(_servitor != null && _servitor.isPet())
			isPet = true;

		_servitor = servitor;
		autoShot();
		if(servitor == null)
		{
			if(isPet)
				setPetControlItem(null);

			getEffectList().stopEffect(4140);
		}
	}

	public void scheduleDelete()
	{
		long time = 0L;

		if(Config.SERVICES_ENABLE_NO_CARRIER)
			time = NumberUtils.toInt(getVar("noCarrier"), 0);

		scheduleDelete(time * 1000L);
	}

	/**
	 * Удалит персонажа из мира через указанное время, если на момент истечения времени он не будет присоединен.
	 * <br><br>
	 * TODO: через минуту делать его неуязвимым.<br>
	 * TODO: сделать привязку времени к контексту, для зон с лимитом времени оставлять в игре на все время в зоне.<br>
	 * <br>
	 *
	 * @param time время в миллисекундах
	 */
	public void scheduleDelete(long time)
	{
		if(isLogoutStarted() || isInOfflineMode())
			return;

		broadcastCharInfo();

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if(!isConnected())
				{
					prepareToLogout();
					deleteMe();
				}
			}
		}, time);
	}

	@Override
	protected void onDelete()
	{
		super.onDelete();

		// Убираем фэйк в точке наблюдения
		if(_observePoint != null)
			_observePoint.deleteMe();

		//Send friendlists to friends that this player has logged off
		_friendList.notifyFriends(false);

		_tpBookMarks.clear();
		_inventory.clear();
		_warehouse.clear();
		_servitor = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_playersOnAccount = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_observePoint = null;
	}

	public void setTradeList(List<TradeItem> list)
	{
		_tradeList = list;
	}

	public List<TradeItem> getTradeList()
	{
		return _tradeList;
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setSellStoreName(String name)
	{
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, List<TradeItem> list)
	{
		if (packageSell)
			_packageSellList = list;
		else
			_sellList = list;
	}

	public List<TradeItem> getSellList()
	{
		return getSellList(_privatestore == STORE_PRIVATE_SELL_PACKAGE);
	}

	public List<TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public void setBuyStoreName(String name)
	{
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public void setBuyList(List<TradeItem> list)
	{
		_buyList = list;
	}

	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}

	public void setManufactureName(String name)
	{
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public List<ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
		if(type != STORE_PRIVATE_NONE)
			setVar(STOREMODE_VAR, String.valueOf(type), -1);
		else
			unsetVar(STOREMODE_VAR);
	}

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE;
	}

	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	public long getPrivateStoreStartTime()
	{
		return _privateStoreStartTime;
	}

	public void setPrivateStoreStartTime(long t)
	{
		_privateStoreStartTime = t;
	}

	public L2GameServerPacket getPrivateStoreMsgPacket(Player forPlayer)
	{
		switch (getPrivateStoreType())
		{
			case STORE_PRIVATE_BUY:
				return new PrivateStoreMsgBuy(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_SELL:
			case STORE_PRIVATE_SELL_PACKAGE:
				return new PrivateStoreMsgSell(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_MANUFACTURE:
				return new RecipeShopMsg(this, canTalkWith(forPlayer));
		}

		return null;
	}

	public void broadcastPrivateStoreInfo()
	{
		if (!isVisible() || _privatestore == STORE_PRIVATE_NONE)
			return;

		sendPacket(getPrivateStoreMsgPacket(this));
		List<Player> players = World.getAroundObservers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(getPrivateStoreMsgPacket(target));
		}
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 *
	 * @param clan the clat to set
	 */
	public void setClan(Clan clan)
	{
		if(_clan != clan && _clan != null)
			unsetVar("canWhWithdraw");

		Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(SkillEntry skill : getAllSkills())
				if (skill.getEntryType() == SkillEntryType.CLAN)
					removeSkill(skill, false);

		_clan = clan;

		if(clan == null)
		{
			_pledgeType = Clan.SUBUNIT_NONE;
			_pledgeClass = Rank.VAGABOND;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().validateItems();
			return;
		}

		if(!clan.isAnyMember(getObjectId()))
		{
			setClan(null);
			if(!isNoble())
				setTitle("");
		}
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Fortress getFortress()
	{
		int id = _clan != null ? _clan.getHasFortress() : 0;
		return ResidenceHolder.getInstance().getResidence(Fortress.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
			_arrowItem = null;
		}
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2Player then return True.
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null)
			{
				if(activeWeapon.getItemType() == WeaponType.BOW)
					_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
				else if(activeWeapon.getItemType() == WeaponType.CROSSBOW)
					_arrowItem = getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
			}

			// Equip arrows needed in left hand
			if(_arrowItem != null)
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
		}
		else
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		return _arrowItem != null;
	}

	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(final Party party)
	{
		if(party != null)
			party.addPartyMember(this);
	}

	public void leaveParty()
	{
		if(isInParty())
			_party.removePartyMember(this, false, false);
	}

	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isGM()
	{
		return _playerAccess != null && _playerAccess.IsGM;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
			_playerAccess = pa;
		else
			_playerAccess = new PlayerAccess();

		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	/**
	 * Update Stats of the L2Player client side by sending Server->Client packet UserInfo/StatusUpdate to this L2Player and CharInfo/StatusUpdate to all players around (broadcast).<BR><BR>
	 */
	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;

		refreshOverloaded();
		refreshExpertisePenalty();
		super.updateStats();
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, true, StatusUpdate.KARMA);
		if(flagChanged)
			broadcastRelation();
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline)
	{
		setIsOnline(isOnline);
		updateOnlineStatus();
	}

	private void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000L);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void increaseKarma(final long add_karma)
	{
		boolean flagChanged = _karma == 0;
		long new_karma = _karma + add_karma;

		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;

		if(_karma == 0 && new_karma > 0)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);
			}

			_karma = (int) new_karma;
		}
		else
			_karma = (int) new_karma;

		updateKarma(flagChanged);
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void decreaseKarma(final int i)
	{
		boolean flagChanged = _karma > 0;
		_karma -= i;
		if(_karma <= 0)
		{
			_karma = 0;
			updateKarma(flagChanged);
		}
		else
			updateKarma(false);
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and	the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param accountName The name of the L2Player
	 * @param name		The name of the L2Player
	 * @param hairStyle   The hair style Identifier of the L2Player
	 * @param hairColor   The hair color Identifier of the L2Player
	 * @param face		The face type Identifier of the L2Player
	 * @return The L2Player added to the database or null
	 */
	public static Player create(int classId, int sex, String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);

		// Create a new L2Player with an account name
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		// Add the player in the characters table of the database
		if(!CharacterDAO.getInstance().insert(player))
			return null;

		return player;
	}

	/**
	 * Retrieve a L2Player from the characters table of the database and add it in _allObjects of the L2World
	 *
	 * @return The L2Player loaded from the database
	 */
	public static Player restore(final int objectId)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		PreparedStatement statement3 = null;
		ResultSet rset = null;
		ResultSet rset3 = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");

			if(rset.next())
			{
				final int classId = CharacterDAO.getInstance().getBaseClassIdByObjectId(objectId);
				if (classId < 0)
					return null;

				final boolean female = rset.getInt("sex") == 1;
				final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);

				player = new Player(objectId, template);

				CharacterVariablesDAO.getInstance().loadVariables(objectId, player.getVars());
				player.loadInstanceReuses();
				player.loadPremiumItemList();
				player.setTpBookmarkSize(rset.getInt("bookmarks"));
				player._tpBookMarks = CharacterTPBookmarkDAO.getInstance().select(player);
				player._friendList.restore();
				player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
				player._savedServitors = CharacterServitorDAO.getInstance().select(player);
				CharacterGroupReuseDAO.getInstance().select(player);

				player.setBaseClass(classId);
				player._login = rset.getString("account_name");
				player.setName(rset.getString("char_name"));

				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setHeading(0);

				player.setKarma(rset.getInt("karma"), false);
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
					player.setLeaveClanTime(0);
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);

				player.setNoChannel(rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
					player.setNoChannel(0);

				player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

				final int clanId = rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}

				player.setCreateTime(rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(rset.getInt("deletetime"));

				player.setTitle(rset.getString("title"));

				if(player.getVar("titlecolor") != null)
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));

				if(player.getVar("namecolor") == null)
					if(player.isGM())
						player.setNameColor(Config.GM_NAME_COLOUR);
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));

				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player.setAutoLoot(player.getVarInt("AutoLoot", Config.AUTO_LOOT ? AUTO_LOOT_ALL : AUTO_LOOT_NONE));
					player.setAutoLootHerbs(player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS));
				}

				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));

				player.setLastAccess(rset.getLong("lastAccess"));

				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player.setRecomBonusTime(rset.getInt("rec_bonus_time"));

				if(player.getVar("recLeftToday") != null)
					player.setRecomLeftToday(Integer.parseInt(player.getVar("recLeftToday")));
				else
					player.setRecomLeftToday(0);

				player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));

				player.setKeyBindings(rset.getBytes("key_bindings"));
				player.setPcBangPoints(rset.getInt("pcBangPoints"));

				player.setFame(rset.getInt("fame"));

				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
				{
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
					player.setNoble(Olympiad.isNoble(player.getObjectId()));
				}

				player.updatePledgeClass();

				int reflection = 0;

				if(player.getVar("jailed") != null && System.currentTimeMillis() / 1000 < Integer.parseInt(player.getVar("jailed")) + 60)
				{
					player.setXYZ(-114648, -249384, -2984);
					/*NOTUSED*String[] re = player.getVar("jailedFrom").split(";");
					Location loc = new Location(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));*/
					player.sitDown(null);
					player.block();
					player._unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), Integer.parseInt(player.getVar("jailed")) * 1000L);
				}
				else
				{
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection > 0) // не портаем назад из ГХ, парнаса, джайла
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}

				player.setReflection(reflection);

				EventHolder.getInstance().findEvent(player);

				//TODO [G1ta0] запускать на входе
				CharacterQuestDAO.getInstance().select(player);

				player.getInventory().restore();

				restoreCharSubClasses(player);

				// Активируем CW после загрузки сабов, иначе скиллы будут удалены
				for (ItemInstance item : player.getInventory().getItems())
					if (item.isCursed())
						CursedWeaponsManager.getInstance().checkPlayer(player, item);

				// 4 очка в минуту оффлайна
				player.setVitality(rset.getInt("vitality") + (int) ((System.currentTimeMillis() / 1000L - rset.getLong("lastAccess")) / 15.));

				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(ANIMATION_OF_CAST_RANGE_VAR);
					if(var != null)
						player.setBuffAnimRange(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(HERO_AURA);
					if (var != null)
						player.setHeroAura(true);
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_TRADERS_VAR);
					if(var != null)
						player.setNotShowTraders(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(DISABLE_FOG_AND_RAIN);
					if(var != null)
						player.setDisableFogAndRain(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				statement3 = con.prepareStatement("SELECT obj_Id, char_name, createtime FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while(rset3.next())
					player._playersOnAccount.put(rset3.getInt("obj_Id"), new AccountPlayerInfo(rset3.getInt("createtime"), rset3.getString("char_name")));

				DbUtils.close(statement3, rset3);

				player.setLectureMark(Config.EX_LECTURE_MARK ? AccountLectureMarkDAO.getInstance().select(player.getAccountName()) : 0, false);

				//if(!player.isGM())
				{
					LazyArrayList<Zone> zones = LazyArrayList.newInstance();

					World.getZones(zones, player.getLoc(), player.getReflection());

					if(!zones.isEmpty())
						for(Zone zone : zones)
							if(zone.getType() == ZoneType.no_restart)
							{
								if(System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
								{
									player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.EnterWorld.TeleportedReasonNoRestart"));
									player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
								}
							}
							else if(zone.getType() == ZoneType.SIEGE)
							{
								SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
								if(siegeEvent != null)
									player.setLoc(siegeEvent.getEnterLoc(player, zone));
								else
								{
									Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
									player.setLoc(r.getNotOwnerRestartPoint(player));
								}
							}

					LazyArrayList.recycle(zones);

					if(DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false))
						player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
				}

				player.restoreBlockList();
				player._macroses.restore();

				//FIXME [VISTALL] нужно ли?
				player.refreshExpertisePenalty();
				player.refreshOverloaded();

				player.getWarehouse().restore();
				player.getFreight().restore();

				player.restoreTradeList();
				if(player.getVar(STOREMODE_VAR) != null)
				{
					player.setPrivateStoreType(Integer.parseInt(player.getVar(STOREMODE_VAR)));
					player.setSitting(true);
				}

				player.updateKetraVarka();
				player.updateRam();
				player.checkRecom();

				if (!player._savedServitors.isEmpty())
					for(int[] ar : player._savedServitors)
						if (ar[0] == Servitor.PET_TYPE)
						{
							ItemInstance controlItem = player.getInventory().getItemByObjectId(ar[1]);
							if (controlItem != null && PetDataTable.isPetControlItem(controlItem))
								player.setPetControlItem(controlItem);
						}
			}
		}
		catch(final Exception e)
		{
			_log.error("Could not restore char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}

	private void loadPremiumItemList()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int itemNum = rs.getInt("itemNum");
				int itemId = rs.getInt("itemId");
				long itemCount = rs.getLong("itemCount");
				String itemSender = rs.getString("itemSender");
				PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
				_premiumItems.put(itemNum, item);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deletePremiumItem(int itemNum)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Map<Integer, PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		if(!_storeLock.tryLock())
			return;

		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
						"UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?" + //
								",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,clanid=?,deletetime=?," + //
								"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
								"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,vitality=?,fame=?,bookmarks=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				if(_stablePoint == null) // если игрок находится в точке в которой его сохранять не стоит (например на виверне) то сохраняются последние координаты
				{
					statement.setInt(4, getX());
					statement.setInt(5, getY());
					statement.setInt(6, getZ());
				}
				else
				{
					statement.setInt(4, _stablePoint.x);
					statement.setInt(5, _stablePoint.y);
					statement.setInt(6, _stablePoint.z);
				}
				statement.setInt(7, getKarma());
				statement.setInt(8, getPvpKills());
				statement.setInt(9, getPkKills());
				statement.setInt(10, getRecomHave());
				statement.setInt(11, getRecomLeft());
				statement.setInt(12, getRecomBonusTime());
				statement.setInt(13, getNevitSystem().getPoints());
				statement.setInt(14, getNevitSystem().getTime());
				statement.setInt(15, getClanId());
				statement.setInt(16, getDeleteTimer());
				statement.setString(17, _title);
				statement.setInt(18, _accessLevel);
				statement.setInt(19, isOnline() ? 1 : 0);
				statement.setLong(20, getLeaveClanTime() / 1000L);
				statement.setLong(21, getDeleteClanTime() / 1000L);
				statement.setLong(22, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(23, (int)(getOnlineTime() / 1000L));
				statement.setInt(24, getPledgeType());
				statement.setInt(25, getPowerGrade());
				statement.setInt(26, getLvlJoinedAcademy());
				statement.setInt(27, getApprentice());
				statement.setBytes(28, getKeyBindings());
				statement.setInt(29, getPcBangPoints());
				statement.setString(30, getName());
				statement.setInt(31, (int) getVitality());
				statement.setInt(32, getFame());
				statement.setInt(33, getTpBookmarkSize());
				statement.setInt(34, getObjectId());

				statement.executeUpdate();
				GameStats.increaseUpdatePlayerBase();

				if(!fast)
				{
					CharacterEffectDAO.getInstance().insert(this);
					CharacterGroupReuseDAO.getInstance().insert(this);
					storeDisableSkills();
					storeBlockList();
				}

				storeCharSubClasses();
			}
			catch(Exception e)
			{
				_log.error("Could not store char data: " + this + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public SkillEntry addSkill(final SkillEntry newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;

		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		SkillEntry oldSkill = super.addSkill(newSkill);

		if(newSkill.equals(oldSkill))
			return oldSkill;

		// Add or update a L2Player skill in the character_skills table of the database
		if(store)
			CharacterSkillDAO.getInstance().replace(this, newSkill);

		return oldSkill;
	}

	public SkillEntry removeSkill(SkillEntry skill, boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill removed
	 */
	public SkillEntry removeSkill(int id, boolean fromDB)
	{
		SkillEntry oldSkill = super.removeSkillById(id);

		if(fromDB && oldSkill != null)
			CharacterSkillDAO.getInstance().delete(this, oldSkill);

		return oldSkill;
	}

	public void disableSkill(SkillEntry skill, TimeStamp t)
	{
		_skillReuses.put(skill.hashCode(), t);
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());

			if(_skillReuses.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized(_skillReuses)
			{
				StringBuilder sb;
				for(TimeStamp timeStamp : _skillReuses.values())
				{
					if(timeStamp.hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		long curTime = System.currentTimeMillis();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");

				SkillEntry skill = SkillTable.getInstance().getSkillEntry(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
					disableSkill(skill, new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.error("Could not restore active skills data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		for(int i = 0; i < 3; i++)
			if(_henna[i] != null)
				totalSlots--;

		if(totalSlots <= 0)
			return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;

		slot--;

		if(_henna[slot] == null)
			return false;

		final Henna henna = _henna[slot];
		final int dyeID = henna.getDyeId();

		_henna[slot] = null;

		CharacterHennaDAO.getInstance().delete(this, slot + 1);

		recalcHennaStats();
		sendPacket(new HennaInfo(this));
		sendUserInfo(true);
		ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2);

		return true;
	}

	/**
	 * Add a Henna to the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 *
	 * @param henna L2Henna РґР»СЏ РґРѕР±Р°РІР»РµРЅРёСЏ
	 */
	public boolean addHenna(Henna henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}

		for(int i = 0; i < 3; i++)
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				recalcHennaStats();

				CharacterHennaDAO.getInstance().insert(this, i + 1, henna.getSymbolId());

				sendPacket(new HennaInfo(this));
				sendUserInfo(true);

				return true;
			}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for(int i = 0; i < 3; i++)
		{
			Henna henna = _henna[i];
			if(henna == null)
				continue;
			if(!henna.isForThisClass(this))
				continue;

			_hennaINT += henna.getStatINT();
			_hennaSTR += henna.getStatSTR();
			_hennaMEN += henna.getStatMEN();
			_hennaCON += henna.getStatCON();
			_hennaWIT += henna.getStatWIT();
			_hennaDEX += henna.getStatDEX();
		}

		if(_hennaINT > 5)
			_hennaINT = 5;
		if(_hennaSTR > 5)
			_hennaSTR = 5;
		if(_hennaMEN > 5)
			_hennaMEN = 5;
		if(_hennaCON > 5)
			_hennaCON = 5;
		if(_hennaWIT > 5)
			_hennaWIT = 5;
		if(_hennaDEX > 5)
			_hennaDEX = 5;
	}

	/**
	 * @param slot id слота у перса
	 * @return the Henna of this L2Player corresponding to the selected slot.<BR><BR>
	 */
	public Henna getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
	}

	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		if(getInventory().destroyItemByItemId(itemConsumeId, itemCount))
		{
			if (!ItemHolder.getInstance().getTemplate(itemConsumeId).isHideConsumeMessage())
				sendPacket(SystemMessage.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp)
	{
		for(ItemInstance item : getInventory().getPaperdollItems())
			if(item != null && item.getItemId() == itemId)
			{
				final int newMp = item.getLifeTime() - (Config.ALT_OLY_TALISMANS_MP_CONSUME || !isInOlympiadMode() || !item.getTemplate().isTalisman() ? mp : 1);
				if(newMp >= 0)
				{
					item.setLifeTime(newMp);
					sendPacket(new InventoryUpdate().addModifiedItem(item));
					return true;
				}
				break;
			}
		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR><BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return _template.baseMAtk > 3;
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final void setRiding(boolean mode)
	{
		_riding = mode;
	}

	/**
	 * Проверяет, можно ли приземлиться в этой зоне.
	 *
	 * @return можно ли приземлится
	 */
	public boolean checkLandingState()
	{
		if(isInZone(ZoneType.no_landing))
			return false;

		SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			Residence unit = siege.getResidence();
			if(unit != null && getClan() != null && isClanLeader() && (getClan().getCastle() == unit.getId() || getClan().getHasFortress() == unit.getId()))
				return true;
			return false;
		}

		return true;
	}

	public void setMount(int npcId, int controlObjectId, int level, int currentFed)
	{
		if(isCursedWeaponEquipped())
			return;

		final PetData info = PetDataTable.getInstance().getInfo(npcId, level);
		if (info == null)
			return;

		switch(npcId)
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				setRiding(true);
				if(isNoble())
					addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_STRIDER_ASSAULT, 1), false);
				break;
			case PetDataTable.WYVERN_ID:
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_WYVERN_BREATH, 1), false);
				break;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				setRiding(true);
				break;
			default:
				_log.warn("Unknown mount:" + npcId);
				return;
		}

		if(npcId > 0)
			unEquipWeapon();

		_mountNpcId = npcId;
		_mountObjId = controlObjectId;
		_mountLevel = level;
		_mountCurrentFed = currentFed;
		_mountMaxFed = info.getFeedMax();

		broadcastUserInfo(true); // нужно послать пакет перед Ride для корректного снятия оружия с заточкой
		if (_mountCurrentFed >= 0)
		{
			sendPacket(new SetupGauge(this, SetupGauge.GREEN, _mountCurrentFed*10000, _mountMaxFed*10000));
			startMountFeedTask();
		}
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true); // нужно послать пакет после Ride для корректного отображения скорости

		sendPacket(new SkillList(this));
	}

	public void dismount()
	{
		if (!isMounted())
			return;

		setFlying(false);
		setRiding(false);
		if(getTransformation() > 0)
			setTransformation(0);

		removeSkillById(Skill.SKILL_STRIDER_ASSAULT);
		removeSkillById(Skill.SKILL_WYVERN_BREATH);
		getEffectList().stopEffect(Skill.SKILL_HINDER_STRIDER);
		sendPacket(new SetupGauge(this, SetupGauge.GREEN, 0, 0));

		stopMountFeedTask();
		PetDAO.getInstance().updateMount(_mountObjId, _mountCurrentFed);

		_mountNpcId = 0;
		_mountObjId = 0;
		_mountLevel = 0;
		_mountCurrentFed = -1;

		broadcastPacket(new Ride(this));
		broadcastUserInfo(true);

		sendPacket(new SkillList(this));
	}

	public void unEquipWeapon()
	{
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		abortAttack(true, true);
		abortCast(true, true);
	}

	@Override
	public int getSpeed(int baseSpeed)
	{
		if(isMounted())
		{
			PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if(petData != null)
				speed = petData.getSpeed();
			double mod = 1.;
			int level = getLevel();
			if(_mountLevel > level && level - _mountLevel > 10)
				mod = 0.5; // Штраф на разницу уровней между игроком и петом
			baseSpeed = (int) (mod * speed);
		}
		return super.getSpeed(baseSpeed);
	}

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;
	private int _mountCurrentFed;
	private int _mountMaxFed;
	private ScheduledFuture<?> _mountFeedTask = null;

	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	public int getMountObjId()
	{
		return _mountObjId;
	}

	public int getMountLevel()
	{
		return _mountLevel;
	}

	public int getMountCurrentFed()
	{
		return _mountCurrentFed;
	}

	public void setMountCurrentFed(int currentFed)
	{
		_mountCurrentFed = currentFed;
	}

	public int getMountMaxFed()
	{
		return _mountMaxFed;
	}

	private void startMountFeedTask()
	{
		stopMountFeedTask();
		if (isMounted() && getMountCurrentFed() >= 0)
			_mountFeedTask = ThreadPoolManager.getInstance().schedule(new MountFeedTask(this), 10000L);
	}

	private void stopMountFeedTask()
	{
		final ScheduledFuture<?> task = _mountFeedTask;
		if (task != null)
		{
			task.cancel(false);
			_mountFeedTask = null;
		}
	}

	public void updateMountFed()
	{
		if (isDead() || !isMounted())
			return;
		if (_mountCurrentFed < 0)
			return;
		if (_mountCurrentFed == 0)
		{
			sendPacket(SystemMsg.YOU_ARE_OUT_OF_FEED);
			dismount();
			return;
		}

		if (_mountCurrentFed * 100 <= getMountMaxFed() * 55) // меньше или равно 55%
			for (ItemInstance food : getInventory().getItems())
			{
				if (food.getTemplate().isPetFood() && food.getTemplate().testCondition(this, food, false))
					if (food.getTemplate().getHandler().useItem(this, food, false))
						break;
			}

		_mountCurrentFed = Math.max(_mountCurrentFed - (isInCombat() ? 10 : 5), 0); // TODO: DS: нормальное потребление еды
		sendPacket(new SetupGauge(this, SetupGauge.GREEN, _mountCurrentFed * 10000, _mountMaxFed * 10000));
		startMountFeedTask();
	}

	public void sendDisarmMessage(ItemInstance wpn)
	{
		SystemMessage sm;
		if(wpn.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(SystemMsg.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
		}
		else
			sm = new SystemMessage(SystemMsg.S1_HAS_BEEN_DISARMED);
		sm.addItemName(wpn.getItemId());
		sendPacket(sm);
	}

	/**
	 * Устанавливает тип используемого склада.
	 *
	 * @param type тип склада:<BR>
	 *             <ul>
	 *             <li>WarehouseType.PRIVATE
	 *             <li>WarehouseType.CLAN
	 *             <li>WarehouseType.CASTLE
	 *             </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ С‚РёРї РёСЃРїРѕР»СЊР·СѓРµРјРѕРіРѕ СЃРєР»Р°РґР°.
	 *
	 * @return null РёР»Рё С‚РёРї СЃРєР»Р°РґР°:<br>
	 *         <ul>
	 *         <li>WarehouseType.PRIVATE
	 *         <li>WarehouseType.CLAN
	 *         <li>WarehouseType.CASTLE
	 *         </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.<EffectCubic>emptyList() : _cubics.values();
	}

	public void addCubic(EffectCubic cubic)
	{
		if(_cubics == null)
			_cubics = new ConcurrentHashMap<Integer, EffectCubic>(3);
		_cubics.put(cubic.getId(), cubic);
	}

	public void removeCubic(int id)
	{
		if(_cubics != null)
			_cubics.remove(id);
	}

	public EffectCubic getCubic(int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[" + getObjectId() + "]";
	}

	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();

		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
			_lastNpc = HardReferences.emptyRef();
		else
			_lastNpc = npc.getRef();
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	/**
	 * @return True if L2Player is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return getReflection() instanceof DarknessFestival;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
		{
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			_spiritShotDischargeTimeStamp = System.currentTimeMillis();
		}
		else
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(Integer shotId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoSoulShot(shotId);
				continue;
			}
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null)
				continue;
			handler.useItem(this, item, false);
		}
	}

	public boolean getChargedFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT;
	}

	@Override
	public int getChargedSpiritShot(boolean first)
	{
		if (first && System.currentTimeMillis() - _spiritShotDischargeTimeStamp < Config.ALT_SPIRITSHOT_DISCHARGE_CORRECTION)
			return 0;
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return 0;
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	public void setInvisible(SpecialEffectState vis)
	{
		_invisibleState = vis;
	}

	@Override
	public SpecialEffectState getInvisible()
	{
		return _invisibleState;
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
	}

	public void teleToCastle()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
	}

	public void teleToFortress()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_FORTRESS), ReflectionManager.DEFAULT);
	}

	public void teleToClanhall()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendPacket(message);
	}

	@Override
	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(isDeleted())
			return;

		final Boat boat = getBoat();
		if (boat != null && !boat.isTeleporting())
			boat.oustPlayer(this, getLoc(), false); // телепорт с корабля

		super.teleToLocation(x, y, z, r);
		if(getServitor() != null)
			getServitor().teleportToOwner();
	}

	@Override
	public boolean onTeleported()
	{
		if (!super.onTeleported())
			return false;

		if(isFakeDeath())
			breakFakeDeath();

		if(isInBoat())
			setLoc(getBoat().getLoc());

		// 15 секунд после телепорта на персонажа не агрятся мобы
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

		spawnMe();

		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());

		if(isPendingRevive())
			doRevive();

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

		if(isLockedTarget() && getTarget() != null)
			sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));

		sendUserInfo(true);
		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;

		if (!_observerMode.compareAndSet(OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		_olympiadObserverMode = false;
		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this, false);

		_observePoint = new ObservePoint(this);
		_observePoint.setLoc(loc);
		_observePoint.startImmobilized();

		// Отображаем надпись над головой
		broadcastCharInfo();

		// Переходим в режим обсервинга
		sendPacket(new ObserverStart(loc));

		return true;
	}

	public void appearObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTING, OBSERVER_STARTED))
				return;

		_observePoint.spawnMe();

		//World.showObjectsToPlayer(this, true);

		if (_olympiadObserverMode)
		{
			final OlympiadGame game = getOlympiadObserveGame();
			if(game != null)
			{
				game.addSpectator(this);
				game.broadcastInfo(null, this, true);
			}
		}
	}

	public void leaveObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		// Очищаем все видимые обьекты
		//World.removeObjectsFromPlayer(this, true);

		_observePoint.deleteMe();
		_observePoint = null;

		setTarget(null);
		stopMove();

		// Выходим из режима обсервинга
		sendPacket(new ObserverEnd(getLoc()));
	}

	public void returnFromObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_LEAVING, OBSERVER_NONE))
			return;

		_olympiadObserverMode = false;

		// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
		setLastClientPosition(null);
		setLastServerPosition(null);

		standUp();
		setFlying(false);

		broadcastCharInfo();

		World.showObjectsToPlayer(this, false);
	}

	public void enterOlympiadObserverMode(Location loc, OlympiadGame game, Reflection reflect)
	{
		final WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return;

		if (!_observerMode.compareAndSet(_olympiadObserverMode ? OBSERVER_STARTED : OBSERVER_NONE, OBSERVER_STARTING))
			return;

		setTarget(null);
		stopMove();

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this, false);

		if (_olympiadObserverMode)
		{
			final OlympiadGame oldGame = getOlympiadObserveGame();
			if(oldGame != null)
				oldGame.removeSpectator(this);

			sendPacket(ExOlympiadMatchEnd.STATIC);

			_observePoint.decayMe();
		}
		else
		{
			// Отображаем надпись над головой
			broadcastCharInfo();

			// Меняем интерфейс
			sendPacket(new ExOlympiadMode(3));

			_observePoint = new ObservePoint(this);
			_olympiadObserverMode = true;
		}

		_observePoint.setLoc(loc);
		_observePoint.setReflection(reflect);

		setOlympiadObserveGame(game);

		// "Телепортируемся"
		sendPacket(new TeleportToLocation(this, loc));
	}

	public void leaveOlympiadObserverMode(boolean removeFromGame)
	{
		if (!_olympiadObserverMode)
			return;
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		if (removeFromGame)
		{
			final OlympiadGame game = getOlympiadObserveGame();
			if (game != null)
				game.removeSpectator(this);
		}

		setOlympiadObserveGame(null);
		_olympiadObserverMode = false;

		// Очищаем все видимые обьекты
		//World.removeObjectsFromPlayer(this, true);

		_observePoint.deleteMe();
		_observePoint = null;

		setTarget(null);
		stopMove();

		// Меняем интерфейс
		sendPacket(new ExOlympiadMode(0));
		sendPacket(ExOlympiadMatchEnd.STATIC);

		// "Телепортируемся"
		sendPacket(new TeleportToLocation(this, getLoc()));
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public boolean isInObserverMode()
	{
		return getObserverMode() > 0;
	}

	public boolean isInOlympiadObserverMode()
	{
		return _olympiadObserverMode;
	}

	public int getObserverMode()
	{
		return _observerMode.get();
	}

	public ObservePoint getObservePoint()
	{
		return _observePoint;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void addToBlockList(final String charName)
	{
		if(charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName))
		{
			// уже в списке
			sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		Player block_target = World.getPlayer(charName);

		if(block_target != null)
		{
			if(block_target.isGM())
			{
				sendPacket(SystemMsg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(SystemMsg.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addName(block_target));
			block_target.sendPacket(new SystemMessage(SystemMsg.S1_HAS_PLACED_YOU_ON_HISHER_IGNORE_LIST).addName(this));
			return;
		}

		int charId = CharacterDAO.getInstance().getObjectIdByName(charName);

		if(charId == 0)
		{
			// чар не существует
			sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		if(Config.gmlist.containsKey(charId) && Config.gmlist.get(charId).IsGM)
		{
			sendPacket(SystemMsg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
			return;
		}
		_blockList.put(charId, charName);
		sendPacket(new SystemMessage(SystemMsg.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(charName));
	}

	public void removeFromBlockList(final String charName)
	{
		int charId = 0;
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
			{
				charId = blockId;
				break;
			}
		if(charId == 0)
		{
			sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_YOUR_IGNORE_LIST);
			return;
		}
		sendPacket(new SystemMessage(SystemMsg.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST).addString(_blockList.remove(charId)));
		Player block_target = GameObjectsStorage.getPlayer(charId);
		if(block_target != null)
			block_target.sendMessage(getName() + " has removed you from his/her Ignore List."); //В системных(619 == 620) мессагах ошибка ;)
	}

	public boolean isInBlockList(final Player player)
	{
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(final int charId)
	{
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(final String charName)
	{
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
				return true;
		return false;
	}

	private void restoreBlockList()
	{
		_blockList.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int targetId = rs.getInt("target_Id");
				String name = rs.getString("char_name");
				if(name == null)
					continue;
				_blockList.put(targetId, name);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Can't restore player blocklist " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void storeBlockList()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());

			if(_blockList.isEmpty())
				return;

			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");

			synchronized(_blockList)
			{
				StringBuilder sb;
				for(Entry<Integer, String> e : _blockList.entrySet())
				{
					sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(")");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.warn("Can't store player blocklist " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
	}

	public Collection<String> getBlockList()
	{
		return _blockList.values();
	}

	public Map<Integer, String> getBlockListMap()
	{
		return _blockList;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	public void setHeroAura(final boolean heroAura)
	{
		_heroAura = heroAura;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public boolean isHeroAura()
	{
		return _hero || _heroAura;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public void setOlympiadCompStarted(final boolean b)
	{
		_isOlympiadCompStarted = b;
	}

	public boolean isOlympiadCompStarted()
	{
		return _isOlympiadCompStarted;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			if(isClanLeader() && getClan().getCastle() > 0)
				super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_WYVERN_AEGIS, 1));
			super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_NOBLESSE_BLESSING, 1));
			super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_SUMMON_CP_POTION, 1));
			super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_FORTUNE_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_HARMONY_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getSkillEntry(Skill.SKILL_SYMPHONY_OF_NOBLESSE, 1));
		}
		else
		{
			super.removeSkillById(Skill.SKILL_WYVERN_AEGIS);
			super.removeSkillById(Skill.SKILL_NOBLESSE_BLESSING);
			super.removeSkillById(Skill.SKILL_SUMMON_CP_POTION);
			super.removeSkillById(Skill.SKILL_FORTUNE_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_HARMONY_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_SYMPHONY_OF_NOBLESSE);
		}
	}

	public void setNoble(boolean noble)
	{
		if(noble) //broadcast skill animation: Presentation - Attain Noblesse
			broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));
		_noble = noble;
	}

	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0)
			_ketra = 5;
		else if(ItemFunctions.getItemCount(this, 7214) > 0)
			_ketra = 4;
		else if(ItemFunctions.getItemCount(this, 7213) > 0)
			_ketra = 3;
		else if(ItemFunctions.getItemCount(this, 7212) > 0)
			_ketra = 2;
		else if(ItemFunctions.getItemCount(this, 7211) > 0)
			_ketra = 1;
		else if(ItemFunctions.getItemCount(this, 7225) > 0)
			_varka = 5;
		else if(ItemFunctions.getItemCount(this, 7224) > 0)
			_varka = 4;
		else if(ItemFunctions.getItemCount(this, 7223) > 0)
			_varka = 3;
		else if(ItemFunctions.getItemCount(this, 7222) > 0)
			_varka = 2;
		else if(ItemFunctions.getItemCount(this, 7221) > 0)
			_varka = 1;
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(ItemFunctions.getItemCount(this, 7247) > 0)
			_ram = 2;
		else if(ItemFunctions.getItemCount(this, 7246) > 0)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public Rank getPledgeClass()
	{
		return _pledgeClass;
	}

	public void updatePledgeClass()
	{
		_pledgeClass = Rank.getPledgeClass(this);
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	public int getNameColor()
	{
		if(isInObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor), -1);
		else
			unsetVar("namecolor");
	}

	public void setVar(String name, String value, long expirationTime)
	{
		_vars.put(name, value);
		CharacterVariablesDAO.getInstance().setVar(getObjectId(), name, value, expirationTime);
	}

	public void setVar(String name, int value, long expirationTime)
	{
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void setVar(String name, long value, long expirationTime)
	{
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void unsetVar(String name)
	{
		if(name == null)
			return;

		if(_vars.remove(name) != null)
			CharacterVariablesDAO.getInstance().deleteVar(getObjectId(), name);
	}

	public String getVar(String name)
	{
		return _vars.getString(name, null);
	}

	public boolean getVarB(String name, boolean defaultVal)
	{
		String var = _vars.getString(name, null);
		if(var == null)
			return defaultVal;
		return !(var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public boolean getVarB(String name)
	{
		String var = _vars.getString(name, null);
		return !(var == null || var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(String name, long defaultVal)
	{
		long result = defaultVal;
		String var = getVar(name);
		if(var != null)
			result = Long.parseLong(var);
		return result;
	}

	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultVal)
	{
		int result = defaultVal;
		String var = getVar(name);
		if(var != null)
			result = Integer.parseInt(var);
		return result;
	}

	public MultiValueSet<String> getVars()
	{
		return _vars;
	}

	public Language getLanguage()
	{
            /*
		final String lang = getVar("lang@");
		if (lang != null)
		{
			if (lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng"))
				return Language.ENGLISH;
			if (lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus"))
				return Language.RUSSIAN;
		}               
		return _connection == null ? Language.RUSSIAN : _connection.getLanguage();            
            */
            return Language.RUSSIAN;
	}

	@Deprecated
	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN;
	}

	public boolean isAtWarWith(final int id)
	{
		return _clan != null && _clan.isAtWarWith(id);
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (calcStat(Stats.BREATH, 86, null, null) * 1000L);
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, timeinwater));
			if(getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped())
				setTransformation(0);
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setAgathionRes(false);
		unsetVar("lostexp");
		startMountFeedTask();
		updateEffectIcons();
		autoShot();
		_lastReviveTime = System.currentTimeMillis();
	}

	public long getLastReviveTime()
	{
		return _lastReviveTime;
	}

	public void reviveRequest(Player reviver, double percent, boolean pet)
	{
		ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)_askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(SystemMsg.A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(SystemMsg.YOU_CANNOT_RESURRECT_THE_OWNER_OF_A_PET_WHILE_THEIR_PET_IS_BEING_RESURRECTED);
				return;
			}
		}

		if(pet && getServitor() != null && getServitor().isDead() || !pet && isDead())
		{
			ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addString(Math.round(percent) + "%");

			ask(pkt, new ReviveAnswerListener(percent, pet, 0));
		}
	}

	public void summonCharacterRequest(final Player summoner, int itemConsumeId, int itemConsumeCount)
	{
		ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 30000);
		cd.addName(summoner).addZoneName(summoner.getLoc());

		ask(cd, new SummonAnswerListener(summoner, this, itemConsumeId, itemConsumeCount, 30000));
	}

	private void checkRecom()
	{
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.HOUR_OF_DAY, 6);
		temp.set(Calendar.MINUTE, 30);
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		long count = Math.round((System.currentTimeMillis() / 1000 - _lastAccess) / 86400);
		if(count == 0 && _lastAccess < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis())
			count++;

		for(int i = 1; i < count; i++)
			setRecomHave(getRecomHave() - 20);

		if(count > 0)
			restartRecom();
	}

	public void restartRecom()
	{
		setRecomBonusTime(3600);
		setRecomLeftToday(0);
		setRecomLeft(20);
		setRecomHave(getRecomHave() - 20);
		stopRecomBonusTask(false);
		startRecomBonusTask();
		sendUserInfo(true);
		sendVoteSystemInfo();
	}

	@Override
	public boolean isInBoat()
	{
		return _boat != null;
	}

	public Boat getBoat()
	{
		return _boat;
	}

	public void setBoat(Boat boat)
	{
		_boat = boat;
	}

	public Location getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc)
	{
		_inBoatPosition = loc;
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		return _classlist;
	}

	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}

	public int getBaseClassId()
	{
		return _baseClass;
	}

	public void setActiveClass(SubClass activeClass)
	{
		_activeClass = activeClass;
	}

	public SubClass getActiveClass()
	{
		return _activeClass;
	}

	public int getActiveClassId()
	{
		return _activeClass == null ? 0 : _activeClass.getClassId();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished professional quests
	 *
	 * @param oldclass
	 * @param newclass
	 */
	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_effects WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_effects SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(final SQLException e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Сохраняет информацию о классах в БД
	 */
	public void storeCharSubClasses()
	{
		SubClass main = getActiveClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			//main.setExp(getExp());
			//main.setLevel(getLevel());
			//main.setSp(getSp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
			main.setActive(true);
			getSubClasses().put(getActiveClassId(), main);
		}
		else
			_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);

		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			StringBuilder sb;
			for(SubClass subClass : getSubClasses().values())
			{
				sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("exp=").append(subClass.getExp()).append(",");
				sb.append("sp=").append(subClass.getSp()).append(",");
				sb.append("curHp=").append(subClass.getHp()).append(",");
				sb.append("curMp=").append(subClass.getMp()).append(",");
				sb.append("curCp=").append(subClass.getCp()).append(",");
				sb.append("level=").append(subClass.getLevel()).append(",");
				sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
				sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
				sb.append("death_penalty=").append(subClass.getDeathPenalty(this).getLevelOnSaveDB()).append(",");
				sb.append("certification='").append(subClass.getCertification()).append("'");
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
				statement.executeUpdate(sb.toString());
			}

			sb = new StringBuilder("UPDATE character_subclasses SET ");
			sb.append("maxHp=").append(getMaxHp()).append(",");
			sb.append("maxMp=").append(getMaxMp()).append(",");
			sb.append("maxCp=").append(getMaxCp());
			sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
			statement.executeUpdate(sb.toString());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store char sub data: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Restore list of character professions and set up active proof
	 * Used when character is loading
	 */
	public static void restoreCharSubClasses(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty,certification FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();

			SubClass activeSubclass = null;
			while(rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
				subClass.setCertification(rset.getInt("certification"));

				boolean active = rset.getInt("active") != 0;
				if(active)
					activeSubclass = subClass;
				player.getSubClasses().put(subClass.getClassId(), subClass);
			}

			if(player.getSubClasses().size() == 0)
				throw new Exception("There are no one subclass for player: " + player);

			int BaseClassId = player.getBaseClassId();
			if(BaseClassId == -1)
				throw new Exception("There are no base subclass for player: " + player);

			if(activeSubclass != null)
				player.setActiveSubClass(activeSubclass.getClassId(), false);

			if(player.getActiveClass() == null)
			{
				//если из-за какого-либо сбоя ни один из сабкласов не отмечен как активный помечаем базовый как активный
				final SubClass subClass = player.getSubClasses().get(BaseClassId);
				subClass.setActive(true);
				player.setActiveSubClass(subClass.getClassId(), false);
			}
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore char sub-classes: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Добавить класс, используется только для сабклассов
	 *
	 * @param storeOld
	 * @param certification
	 */
	public boolean addSubClass(final int classId, boolean storeOld, int certification)
	{
		if(_classlist.size() >= 4)
			return false;

		final ClassId newId = ClassId.VALUES[classId];

		final SubClass newClass = new SubClass();
		newClass.setBase(false);
		if(newId.getRace() == null)
			return false;

		newClass.setClassId(classId);
		newClass.setCertification(certification);

		_classlist.put(classId, newClass);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			// Store the basic info about this new sub-class.
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, Experience.LEVEL[40]);
			statement.setInt(4, 0);
			statement.setDouble(5, getCurrentHp());
			statement.setDouble(6, getCurrentMp());
			statement.setDouble(7, getCurrentCp());
			statement.setDouble(8, getCurrentHp());
			statement.setDouble(9, getCurrentMp());
			statement.setDouble(10, getCurrentCp());
			statement.setInt(11, 40);
			statement.setInt(12, 0);
			statement.setInt(13, 0);
			statement.setInt(14, 0);
			statement.setInt(15, certification);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("Could not add character sub-class: " + e, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		setActiveSubClass(classId, storeOld);

		boolean countUnlearnable = true;
		int unLearnable = 0;

		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			for(final SkillLearn s : skills)
			{
				final SkillEntry sk = SkillTable.getInstance().getSkillEntry(s.getId(), s.getLevel());
				if(sk == null || !sk.getTemplate().getCanLearn(newId))
				{
					if(countUnlearnable)
						unLearnable++;
					continue;
				}
				addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		}

		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		return true;
	}

	/**
	 * Удаляет всю информацию о классе и добавляет новую, только для сабклассов
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final SubClass originalClass = _classlist.get(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;

		final int certification = originalClass.getCertification();

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.warn("Could not delete char sub-class: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_classlist.remove(oldClassId);

		return newClassId <= 0 || addSubClass(newClassId, false, certification);
	}

	/**
	 * РЈСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ Р°РєС‚РёРІРЅС‹Р№ СЃР°Р±РєР»Р°СЃСЃ
	 * <p/>
	 * <li>Retrieve from the database all skills of this L2Player and add them to _skills </li>
	 * <li>Retrieve from the database all macroses of this L2Player and add them to _macroses</li>
	 * <li>Retrieve from the database all shortCuts of this L2Player and add them to _shortCuts</li><BR><BR>
	 */
	public void setActiveSubClass(final int subId, final boolean store)
	{
		final SubClass sub = getSubClasses().get(subId);
		if(sub == null)
			return;

		final SubClass oldsub = getActiveClass();
		if(oldsub != null)
		{
			CharacterEffectDAO.getInstance().insert(this);
			storeDisableSkills();

			QuestState qs = getQuestState(422);
			if(qs != null)
				qs.exitCurrentQuest(true);

			oldsub.setActive(false);
			if(store)
			{
				oldsub.setCp(getCurrentCp());
				//oldsub.setExp(getExp());
				//oldsub.setLevel(getLevel());
				//oldsub.setSp(getSp());
				oldsub.setHp(getCurrentHp());
				oldsub.setMp(getCurrentMp());
			}
		}

		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);

		setClassId(subId, false, false);

		removeAllSkills();

		getEffectList().stopAllEffects();

		final Servitor servitor = getServitor();
		if (servitor != null)
		{
			if (servitor.isPet())
			{
				final ItemInstance controlItem = ((PetInstance)servitor).getControlItem();
				if (!controlItem.getTemplate().testCondition(this, controlItem, false))
					servitor.unSummon(false, false);
			}
			else
				servitor.unSummon(false, false);
		}

		setAgathion(0);

		CharacterSkillDAO.getInstance().select(this);
		rewardSkills(false);
		checkSkills();
		sendPacket(new ExStorageMaxCount(this));

		refreshExpertisePenalty();

		sendPacket(new SkillList(this));

		getInventory().refreshEquip();
		getInventory().validateItems();

		for(int i = 0; i < 3; i++)
			_henna[i] = null;

		CharacterHennaDAO.getInstance().select(this);
		recalcHennaStats();
		sendPacket(new HennaInfo(this));

		CharacterEffectDAO.getInstance().select(this);
		restoreDisableSkills();

		setCurrentHpMp(sub.getHp(), sub.getMp());
		setCurrentCp(sub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		sendPacket(new SkillCoolTime(this));

		broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

		getDeathPenalty().restore(this);

		setIncreasedForce(0);

		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
	}

	/**
	 * Через delay миллисекунд выбросит игрока из игры
	 */
	public void startKickTask(long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask()
	{
		if(Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS)
		{
			int bonusExpire = getNetConnection().getBonusExpire();
			double bonus = getNetConnection().getBonus();
			if(bonusExpire > System.currentTimeMillis() / 1000L)
			{
				getBonus().setRateXp(bonus);
				getBonus().setRateSp(bonus);
				getBonus().setDropAdena(bonus);
				getBonus().setDropItems(bonus);
				getBonus().setDropSpoil(bonus);

				getBonus().setBonusExpire(bonusExpire);

				if(_bonusExpiration == null)
					_bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
			}
			else if(bonus > 0 && Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
				AccountBonusDAO.getInstance().delete(getAccountName());
		}
	}

	public void stopBonusTask()
	{
		if(_bonusExpiration != null)
		{
			_bonusExpiration.cancel(false);
			_bonusExpiration = null;
		}
	}

	/**
	 * Добавляем/удаляем Премиум вещи, в зависимости от статуса Премиума
	 */
	public void updatePremiumItems()
	{
		if(Config.SERVICES_PREMIUM_ITEMS.length == 0)
			return;

		if(!hasBonus())
		{
			boolean removed = false;
			for(int itemId : Config.SERVICES_PREMIUM_ITEMS)
				if(ItemFunctions.deleteItem(this, itemId, 1))
					removed = true;

			if(removed)
				sendPacket(new SystemMessage(SystemMsg.THE_REMIUM_ACCOUNT_HAS_BEEN_TERMINATED));
		}
		else
		{
			int total = 0;
			for(int itemId : Config.SERVICES_PREMIUM_ITEMS)
			{
				if(ItemFunctions.getItemCount(this,  itemId) == 0)
				{
					if(getWeightPercents() >= 80 || getUsedInventoryPercents() >= 90)
					{
						sendPacket(new SystemMessage(SystemMsg.THE_PREMIUM_ITEM_CANNOT_BE_RECEIVED_BECAUSE_THE_INVENTORY_WEIGHTQUANTITY_LIMIT_HAS_BEEN_EXCEEDED));
						return;
					}

					ItemFunctions.addItem(this, itemId, 1);
					total++;
				}
			}

			if(total > 0)
				sendPacket(new SystemMessage(SystemMsg.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED));
		}
	}

	@Override
	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0, null, null);
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	/**
	 * Возвращает тип атакующего элемента
	 * Использовать только для отображения, не для вычислений !
	 * 
	 */
	public Element getAttackElement()
	{
		return Formulas.getAttackElement(this, null);
	}

	/**
	 * Возвращает силу атаки элемента
	 *  Использовать только для отображения, не для вычислений !
	 *
	 * @return значение атаки
	 */
	public int getAttack(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getAttack(), 0., null, null);
	}

	/**
	 * Возвращает защиту от элемента
	 * Использовать только для отображения, не для вычислений !
	 *
	 * @return значение защиты
	 */
	public int getDefence(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getDefence(), 0., null, null);
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		else
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return false;
		}
	}

	public long getLastMailTime()
	{
		return _lastMailTime;
	}

	public void setLastMailTime(long timeInMillis)
	{
		_lastMailTime = timeInMillis;
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		GameObject target = null;

		if(getTargetId() == id)
			target = getTarget();

		if(target == null && _party != null)
			for(Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}

		if(target == null)
			target = World.getAroundObjectById(this, id);

		return target == null || (target.isCreature() && ((Creature)target).isInvisible()) ? null : target;
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? (isMageClass() ? 3 : 4) : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getPDef(final Creature target)
	{
		double init = 4.; //empty cloak and underwear slots

		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
			init += isMageClass() ? ArmorTemplate.EMPTY_BODY_MYSTIC : ArmorTemplate.EMPTY_BODY_FIGHTER;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != ItemTemplate.SLOT_FULL_ARMOR))
			init += isMageClass() ? ArmorTemplate.EMPTY_LEGS_MYSTIC : ArmorTemplate.EMPTY_LEGS_FIGHTER;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
			init += ArmorTemplate.EMPTY_HELMET;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
			init += ArmorTemplate.EMPTY_GLOVES;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
			init += ArmorTemplate.EMPTY_BOOTS;

		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final Creature target, final SkillEntry skill)
	{
		double init = 0.;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null)
			init += ArmorTemplate.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) == null)
			init += ArmorTemplate.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) == null)
			init += ArmorTemplate.EMPTY_NECKLACE;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null)
			init += ArmorTemplate.EMPTY_RING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null)
			init += ArmorTemplate.EMPTY_RING;

		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	@Override
	public int getPAtkSpd(boolean applyLimit)
	{
		if (isMounted())
		{
			final int feed = getMountCurrentFed();
			if (feed >= 0 && feed * 55 < getMountMaxFed() * 100) // у голодного маунта скорость атаки в два раза меньше
				return super.getPAtkSpd(applyLimit) / 2;
		}
		return super.getPAtkSpd(applyLimit);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR)
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		else
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}
        
        public void setTitleColor(final int red, final int green, final int blue)
        {
            _titlecolor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);		
            setVar("titlecolor", Integer.toHexString(_titlecolor), -1);		    
        }

	@Override
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _isFishing;
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean value)
	{
		_isFishing = value;
	}

	public void startFishing(FishTemplate fish, int lureId)
	{
		_fishing.setFish(fish);
		_fishing.setLureId(lureId);
		_fishing.startFishing();
	}

	public void stopFishing()
	{
		_fishing.stopFishing();
	}

	public Location getFishLoc()
	{
		return _fishing.getFishLoc();
	}

	public Bonus getBonus()
	{
		return _bonus;
	}

	public boolean hasBonus()
	{
		return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000L;
	}

	@Override
	public double getRateAdena()
	{
		return _party == null ? _bonus.getDropAdena() : _party._rateAdena;
	}

	@Override
	public double getRateItems()
	{
		return _party == null ? _bonus.getDropItems() : _party._rateDrop;
	}

	@Override
	public double getRateExp()
	{
		return calcStat(Stats.EXP, (_party == null ? _bonus.getRateXp() : _party._rateExp), null, null);
	}

	@Override
	public double getRateSp()
	{
		return calcStat(Stats.SP, (_party == null ? _bonus.getRateSp() : _party._rateSp), null, null);
	}

	@Override
	public double getRateSpoil()
	{
		return _party == null ? _bonus.getDropSpoil() : _party._rateSpoil;
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	/**
	 * Сброс реюза всех скилов персонажа.
	 */
	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public DeathPenalty getDeathPenalty()
	{
		return _activeClass == null ? null : _activeClass.getDeathPenalty(this);
	}

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		if(!val)
			getEffectList().stopEffect(Skill.SKILL_CHARM_OF_COURAGE);

		sendEtcStatusUpdate();
	}

	private int _increasedForce = 0;
	private long _increasedForceLastUpdateTimeStamp = 0;
	private Future<?> _increasedForceCleanupTask = null;

	private int _consumedSouls = 0;
	private long _consumedSoulsLastUpdateTimeStamp = 0;
	private Future<?> _consumedSoulsCleanupTask = null;

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;

		int max = (int) calcStat(Stats.SOULS_LIMIT, 0, monster, null);

		if(i > max)
			i = max;

		if(i <= 0)
		{
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}

		if(_consumedSouls != i)
		{
			int diff = i - _consumedSouls;
			if(diff > 0)
			{
				_consumedSoulsLastUpdateTimeStamp = System.currentTimeMillis();
				if (_consumedSoulsCleanupTask == null)
					_consumedSoulsCleanupTask = ThreadPoolManager.getInstance().schedule(new SoulsCleanupTask(), 600000L);

				sendPacket(new SystemMessage(SystemMsg.YOUR_SOUL_COUNT_HAS_INCREASED_BY_S1_ITS_NOW_S2).addNumber(diff).addNumber(i));
			}
		}
		else if(max == i)
		{
			_consumedSoulsLastUpdateTimeStamp = System.currentTimeMillis();
			if (_consumedSoulsCleanupTask == null)
				_consumedSoulsCleanupTask = ThreadPoolManager.getInstance().schedule(new SoulsCleanupTask(), 600000L);

			sendPacket(SystemMsg.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		_consumedSouls = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		if (_increasedForce == i)
			return;

		i = Math.min(i, EffectCharge.MAX_CHARGE);
		i = Math.max(i, 0);

		if(i != 0 && i > _increasedForce)
		{
			_increasedForceLastUpdateTimeStamp = System.currentTimeMillis();
			if (_increasedForceCleanupTask == null)
				_increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), 600000L);

			sendPacket(new SystemMessage(SystemMsg.YOUR_FORCE_HAS_INCREASED_TO_LEVEL_S1).addNumber(i));
		}

		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	private class ForceCleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			final long nextDelay = 600000L - (System.currentTimeMillis() - _increasedForceLastUpdateTimeStamp);
			if (nextDelay > 1000L)
			{
				_increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), nextDelay);
				return;
			}

			_increasedForce = 0;
			sendEtcStatusUpdate();
			_increasedForceCleanupTask = null;
		}
	}

	private class SoulsCleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			final long nextDelay = 600000L - (System.currentTimeMillis() - _consumedSoulsLastUpdateTimeStamp);
			if (nextDelay > 1000L)
			{
				_consumedSoulsCleanupTask = ThreadPoolManager.getInstance().schedule(new SoulsCleanupTask(), nextDelay);
				return;
			}

			_consumedSouls = 0;
			sendEtcStatusUpdate();
			_consumedSoulsCleanupTask = null;
		}
	}

	private long _lastFalling;

	public boolean isFalling()
	{
		return System.currentTimeMillis() - _lastFalling < 5000;
	}

	public void falling(int height)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return;
		_lastFalling = System.currentTimeMillis();
		int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000 * height, null, null);
		if(damage > 0)
		{
			int curHp = (int) getCurrentHp();
			if(curHp - damage < 1)
				setCurrentHp(1, false);
			else
				setCurrentHp(curHp - damage, false);
			sendPacket(new SystemMessage(SystemMsg.YOU_RECEIVED_S1_FALLING_DAMAGE).addNumber(damage));
		}
	}

	/**
	 * Системные сообщения о текущем состоянии хп
	 * TODO: DS: переделать весь этот бред
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		//сюда пасивные скиллы
		final int[] hp = {
				30,
				30
		};
		final int[] skills = {
				290,
				291
		};

		//сюда активные эффекты
		final int[] effects_skills_id = {
				139,
				176,
				292,
				292
		};
		final int[] effects_hp = {
				30,
				30,
				30,
				60
		};

		double percent = getMaxHp() / 100;
		double curHpPercent = curHp / percent;
		double newHpPercent = newHp / percent;
		boolean needsUpdate = false;
		int hpcalc;

		//check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			int level = getSkillLevel(skills[i]);
			if(level > 0)
			{
				hpcalc = hp[i];
				if(curHpPercent > hpcalc)
				{
					if(newHpPercent <= hpcalc)
					{
						sendPacket(new SystemMessage(SystemMsg.SINCE_YOUR_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
						needsUpdate = true;
					}
				}
				else if(newHpPercent > hpcalc)
				{
					sendPacket(new SystemMessage(SystemMsg.SINCE_YOUR_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
			}
		}

		//check for active effects
		for(int i = 0; i < effects_skills_id.length; i++)
			if(getEffectList().getEffectsBySkillId(effects_skills_id[i]) != null)
			{
				hpcalc = effects_hp[i];
				if(curHpPercent > hpcalc)
				{
					if(newHpPercent <= hpcalc)
					{
						sendPacket(new SystemMessage(SystemMsg.SINCE_YOUR_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(effects_skills_id[i], 1));
						needsUpdate = true;
					}
				}
				else if(newHpPercent > hpcalc)
				{
					sendPacket(new SystemMessage(SystemMsg.SINCE_YOUR_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(effects_skills_id[i], 1));
					needsUpdate = true;
				}
			}

		if(needsUpdate)
			sendChanges();
	}

	/**
	 * Системные сообщения для темных эльфов о вкл/выкл ShadowSence (skill id = 294)
	 */
	public void checkDayNightMessages()
	{
		int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(SystemMsg.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(SystemMsg.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
		sendChanges();
	}

	//TODO [G1ta0] переработать в лисенер?
	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;

		boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		boolean lastInDangerArea = (_zoneMask & ZONE_ALTERED_FLAG) == ZONE_ALTERED_FLAG;
		boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;
		boolean lastInPeaceZone = (_zoneMask & ZONE_PEACE_FLAG) == ZONE_PEACE_FLAG;
		//FIXME G1ta0 boolean lastInSSQZone = (_zoneMask & ZONE_SSQ_FLAG) == ZONE_SSQ_FLAG;

		boolean isInCombatZone = isInCombatZone();
		boolean isInDangerArea = isInDangerArea();
		boolean isOnSiegeField = isOnSiegeField();
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();

		// обновляем компас, только если персонаж в мире
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;

		if(isInCombatZone)
			_zoneMask |= ZONE_PVP_FLAG;
		if(isInDangerArea)
			_zoneMask |= ZONE_ALTERED_FLAG;
		if(isOnSiegeField)
			_zoneMask |= ZONE_SIEGE_FLAG;
		if(isInPeaceZone)
			_zoneMask |= ZONE_PEACE_FLAG;
		if(isInSSQZone)
			_zoneMask |= ZONE_SSQ_FLAG;

		if(lastZoneMask != _zoneMask)
			sendPacket(new ExSetCompassZoneCode(this));

		if(lastInCombatZone != isInCombatZone || lastOnSiegeField != isOnSiegeField)
			broadcastRelation();

		if(lastInDangerArea != isInDangerArea)
			sendPacket(new EtcStatusUpdate(this));

		if(lastOnSiegeField != isOnSiegeField)
		{
			if(isOnSiegeField)
				sendPacket(SystemMsg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			else
			{
				sendPacket(SystemMsg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				if(!isTeleporting() && getPvpFlag() == 0)
					startPvPFlag(null);
			}
		}

		if(lastInPeaceZone != isInPeaceZone)
			if(isInPeaceZone)
			{
				setRecomTimerActive(false);
				if(getNevitSystem().isActive())
					getNevitSystem().stopAdventTask(true);
				startVitalityTask();
			}
			else
				stopVitalityTask();

		if(isInWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	public void startAutoSaveTask()
	{
		if(!Config.AUTOSAVE)
			return;
		if(_autoSaveTask == null)
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
			_autoSaveTask.cancel(false);
		_autoSaveTask = null;
	}

	public void startVitalityTask()
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;
		if(_vitalityTask == null)
			_vitalityTask = LazyPrecisionTaskManager.getInstance().addVitalityRegenTask(this);
	}

	public void stopVitalityTask()
	{
		if(_vitalityTask != null)
			_vitalityTask.cancel(false);
		_vitalityTask = null;
	}

	public void startPcBangPointsTask()
	{
		if(!Config.ALT_PCBANG_POINTS_ENABLED || Config.ALT_PCBANG_POINTS_DELAY <= 0)
			return;
		if(_pcCafePointsTask == null)
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
	}

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
			_pcCafePointsTask.cancel(false);
		_pcCafePointsTask = null;
	}

	public void startUnjailTask(Player player, int time)
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), time * 60000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = null;
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(SystemMsg.S1).addString(message));
	}

	private Location _lastClientPosition;
	private Location _lastServerPosition;

	public void setLastClientPosition(Location position)
	{
		_lastClientPosition = position;
	}

	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}

	public void setLastServerPosition(Location position)
	{
		_lastServerPosition = position;
	}

	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	@Override
	public int getRelation(Player target)
	{
		int result = 0;

		if(getClan() != null)
		{
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
				result |= RelationChanged.RELATION_CLAN_MATE;
			if(getClan().getAllyId() != 0)
				result |= RelationChanged.RELATION_ALLY_MEMBER;
		}

		if(isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChanged.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChanged.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChanged.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChanged.RELATION_PARTY1; // 0x1
					break;
			}
		}

		Clan clan1 = getClan();
		Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY)
				if(clan2.isAtWarWith(clan1.getClanId()))
				{
					result |= RelationChanged.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2.getClanId()))
						result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			if(getBlockCheckerArena() != -1)
			{
				result |= RelationChanged.RELATION_IN_SIEGE;
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
				if(holder.getPlayerTeam(this) == 0)
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}

		for(Event e : getEvents())
			result =  e.getRelation(this, target, result);

		return result;
	}

	/**
	 * 0=White, 1=Purple, 2=PurpleBlink
	 */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		if(_karma > 0)
			return;

		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
			startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;

		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(_handysBlockCheckerEventArena != -1)
			return;
		if(_pvpFlag == value)
			return;

		setPvpFlag(value);

		sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);

		broadcastRelation();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
	}

	public List<NpcInstance> getTamedBeasts()
	{
		return _tamedBeasts;
	}

	public void addTamedBeast(NpcInstance tamedBeast)
	{
		if(_tamedBeasts == Collections.<NpcInstance>emptyList())
			_tamedBeasts = new CopyOnWriteArrayList<NpcInstance>();

		_tamedBeasts.add(tamedBeast);
	}

	public void removeTamedBeast(NpcInstance b)
	{
		_tamedBeasts.remove(b);
	}

	private long _lastAttackPacket = 0;

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	private long _lastMovePacket = 0;

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_keyBindings = keyBindings;
	}

	/**
	 * Устанавливает режим трансформаии<BR>
	 *
	 * @param transformationId идентификатор трансформации
	 *                         Известные режимы:<BR>
	 *                         <li>0 - стандартный вид чара
	 *                         <li>1 - Onyx Beast
	 *                         <li>2 - Death Blader
	 *                         <li>etc.
	 */
	public void setTransformation(int transformationId)
	{
		if(transformationId == _transformationId || _transformationId != 0 && transformationId != 0)
			return;

		SkillEntry currentSkill;
		// Для каждой трансформации свой набор скилов
		if(transformationId == 0) // Обычная форма
		{
			// Останавливаем текущий эффект трансформации
			for(Effect e : getEffectList().getAllEffects())
				if(e.getEffectType() == EffectType.Transformation)
				{
					if (e.calc() == 0) // Не обрываем Dispel
						continue;
					e.exit();
					preparateToTransform(e.getSkill().getTemplate());
					break;
				}

			// Удаляем скилы трансформации
			if(!_transformationSkills.isEmpty())
			{
				for(SkillEntry s : _transformationSkills.values())
				{
					currentSkill = _existTransformationSkills.get(s.getId());
					if (currentSkill == null) // такого скилла не было вообще, удаляем
						super.removeSkill(s);
					else if (currentSkill != s) // такой скилл был, но другой, возвращаем на место
					{
						super.removeSkill(s);
						addSkill(currentSkill, false);
					}
				}
				_transformationSkills.clear();
			}
		}
		else
		{
			if(!isCursedWeaponEquipped())
			{
				// Добавляем скилы трансформации
				for(Effect effect : getEffectList().getAllEffects())
					if(effect != null && effect.getEffectType() == EffectType.Transformation)
					{
						if(effect.getSkill().getTemplate() instanceof Transformation && ((Transformation) effect.getSkill().getTemplate()).isDisguise)
						{
							for(SkillEntry s : getAllSkills())
								if(s != null && (s.getTemplate().isActive() || s.getTemplate().isToggle()))
								{
									_transformationSkills.put(s.getId(), s);
									_existTransformationSkills.put(s.getId(), s);
								}
						}
						else
							for(AddedSkill s : effect.getSkill().getTemplate().getAddedSkills())
								if(s.level == 0) // трансформация позволяет пользоваться обычным скиллом
								{
									currentSkill = getKnownSkill(s.id);
									if(currentSkill != null)
									{
										_transformationSkills.put(s.id, currentSkill);
										_existTransformationSkills.put(s.id, currentSkill);										
									}
								}
								else if(s.level == -2) // XXX: дикий изжоп для скиллов зависящих от уровня игрока
								{
									int learnLevel = Math.max(effect.getSkill().getTemplate().getMagicLevel(), 40);
									int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
									int curSkillLevel = 1;
									if(maxLevel > 3)
										curSkillLevel += getLevel() - learnLevel;
									else
										curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel); // не спрашивайте меня что это такое
									curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
									currentSkill = getKnownSkill(s.id);
									if (currentSkill != null && currentSkill.getLevel() == curSkillLevel) // точно такой скилл уже есть
									{
										_transformationSkills.put(s.id, currentSkill);
										_existTransformationSkills.put(s.id, currentSkill);										
									}
									else
										_transformationSkills.put(s.id, SkillTable.getInstance().getSkillEntry(s.id, curSkillLevel));
								}
								else
								{
									currentSkill = getKnownSkill(s.id);
									if (currentSkill != null && currentSkill.getLevel() == s.level) // точно такой скилл уже есть
									{
										_transformationSkills.put(s.id, currentSkill);
										_existTransformationSkills.put(s.id, currentSkill);										
									}
									else
										_transformationSkills.put(s.id, s.getSkill());
								}
						preparateToTransform(effect.getSkill().getTemplate());
						break;
					}
			}
			else
				preparateToTransform(null);

			for(SkillEntry s : _transformationSkills.values())
			{
				currentSkill = _existTransformationSkills.get(s.getId());
				if (currentSkill != s)
					addSkill(s, false);
			}
		}

		_transformationId = transformationId;

		sendPacket(new ExBasicActionList(this));
		sendPacket(new SkillList(this));
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		broadcastUserInfo(true);
	}

	private void preparateToTransform(Skill transSkill)
	{
		if(transSkill == null || !transSkill.isBaseTransformation())
		{
			// Останавливаем тугл скиллы
			for(Effect e : getEffectList().getAllEffects())
				if(e.getSkill().getTemplate().isToggle())
					e.exit();
		}
	}

	public boolean isInFlyingTransform()
	{
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	public boolean isInMountTransform()
	{
		return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
	}

	/**
	 * Возвращает режим трансформации
	 *
	 * @return ID режима трансформации
	 */
	public int getTransformation()
	{
		return _transformationId;
	}

	/**
	 * Возвращает имя трансформации
	 *
	 * @return String
	 */
	public String getTransformationName()
	{
		return _transformationName;
	}

	/**
	 * Устанавливает имя трансформаии
	 *
	 * @param name имя трансформации
	 */
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	/**
	 * Устанавливает шаблон трансформации, используется для определения коллизий
	 *
	 * @param template ID шаблона
	 */
	public void setTransformationTemplate(int template)
	{
		_transformationTemplate = template;
	}

	/**
	 * Возвращает шаблон трансформации, используется для определения коллизий
	 *
	 * @return NPC ID
	 */
	public int getTransformationTemplate()
	{
		return _transformationTemplate;
	}

	/**
	 * Возвращает коллекцию скиллов, с учетом текущей трансформации
	 */
	@Override
	public final Collection<SkillEntry> getAllSkills()
	{
		// Трансформация неактивна
		if(_transformationId == 0)
			return super.getAllSkills();

		// Трансформация активна
		IntObjectMap< SkillEntry> tempSkills = new HashIntObjectMap<SkillEntry>();
		for(SkillEntry s : super.getAllSkills())
			if(s != null && !s.getTemplate().isActive() && !s.getTemplate().isToggle())
				tempSkills.put(s.getId(), s);
		tempSkills.putAll(_transformationSkills); // Добавляем к пассивкам скилы текущей трансформации
		return tempSkills.values();
	}

	public void setAgathion(int id)
	{
		if(_agathionId == id)
			return;

		_agathionId = id;
		broadcastCharInfo();
	}

	public int getAgathionId()
	{
		return _agathionId;
	}

	/**
	 * Возвращает количество PcBangPoint'ов даного игрока
	 *
	 * @return количество PcCafe Bang Points
	 */
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	/**
	 * Устанавливает количество Pc Cafe Bang Points для даного игрока
	 *
	 * @param val новое количество PcCafeBangPoints
	 */
	public void setPcBangPoints(int val)
	{
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, boolean doublePoints)
	{
		if(doublePoints)
			count *= 2;

		_pcBangPoints += count;

		sendPacket(new SystemMessage(doublePoints ? SystemMsg.DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT : SystemMsg.YOU_ACQUIRED_S1_PC_BANG_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(int count)
	{
		if(_pcBangPoints < count)
			return false;

		_pcBangPoints -= count;
		sendPacket(new SystemMessage(SystemMsg.YOU_ARE_USING_S1_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	/**
	 * Персонаж в процессе выхода из игры
	 *
	 * @return возвращает true если процесс выхода уже начался
	 */
	public boolean isLogoutStarted()
	{
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val)
	{
		if(!val)
			unsetVar("offline");
		_offline = val;
	}

	public boolean isInOfflineMode()
	{
		return _offline;
	}

	public void saveTradeList()
	{
		String val = "";

		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(TradeItem i : _sellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("selllist", val, -1);
			val = "";
			if(getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_packageSellList == null || _packageSellList.isEmpty())
			unsetVar("packageselllist");
		else
		{
			for(TradeItem i : _packageSellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("packageselllist", val, -1);
			val = "";
			if(getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(TradeItem i : _buyList)
				val += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("buylist", val, -1);
			val = "";
			if(getBuyStoreName() != null)
				setVar("buystorename", getBuyStoreName(), -1);
		}

		if(_createList == null || _createList.isEmpty())
			unsetVar("createlist");
		else
		{
			for(ManufactureItem i : _createList)
				val += i.getRecipeId() + ";" + i.getCost() + ":";
			setVar("createlist", val, -1);
			if(getManufactureName() != null)
				setVar("manufacturename", getManufactureName(), -1);
		}
	}

	public void restoreTradeList()
	{
		String var;
		var = getVar("selllist");
		if(var != null)
		{
			_sellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_sellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("packageselllist");
		if(var != null)
		{
			_packageSellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_packageSellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("buylist");
		if(var != null)
		{
			_buyList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;
				TradeItem i = new TradeItem();
				i.setItemId(Integer.parseInt(values[0]));
				i.setCount(Long.parseLong(values[1]));
				i.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i);
			}
			var = getVar("buystorename");
			if(var != null)
				setBuyStoreName(var);
		}
		var = getVar("createlist");
		if(var != null)
		{
			_createList = new CopyOnWriteArrayList<ManufactureItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 2)
					continue;
				int recId = Integer.parseInt(values[0]);
				long price = Long.parseLong(values[1]);
				if(findRecipe(recId))
					_createList.add(new ManufactureItem(recId, price));
			}
			var = getVar("manufacturename");
			if(var != null)
				setManufactureName(var);
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				Recipe recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public DecoyInstance getDecoy()
	{
		return _decoy;
	}

	public void setDecoy(DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				return 1;
			case PetDataTable.WYVERN_ID:
				return 2;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				return 3;
		}
		return 0;
	}

	@Override
	public double getColRadius()
	{
		if (getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if (template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if (npcTemplate != null)
					return npcTemplate.collisionRadius;
			}
		}
		else if (isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.collisionRadius;
			}
		}
		return getBaseTemplate().collisionRadius;
	}

	@Override
	public double getColHeight()
	{
		if (getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if (template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if (npcTemplate != null)
					return npcTemplate.collisionHeight;
			}
		}
		else if (isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.collisionHeight;
			}
		}
		return getBaseTemplate().collisionHeight;
	}

	@Override
	public void setReflection(Reflection reflection)
	{
		if(getReflection() == reflection)
			return;

		super.setReflection(reflection);

		if(_servitor != null && !_servitor.isDead())
			_servitor.setReflection(reflection);

		if(reflection != ReflectionManager.DEFAULT && !isInOlympiadMode())
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
				setVar("reflection", String.valueOf(reflection.getId()), -1);
		}
		else
			unsetVar("reflection");

		if(getActiveClass() != null)
		{
			getInventory().validateItems();
			// Для квеста _129_PailakaDevilsLegacy
			if(getServitor() != null && (getServitor().getNpcId() == 14916 || getServitor().getNpcId() == 14917))
				getServitor().unSummon(false, false);
		}
	}

	public boolean isTerritoryFlagEquipped()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getAttachment() instanceof TerritoryWardObject;
	}

	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getFame()
	{
		return _fame;
	}

	private void setFame(int fame)
	{
		_fame = fame;
	}

	public void setFame(int fame, String log)
	{
		fame = Math.min(Config.LIM_FAME, fame);
		if(log != null && !log.isEmpty())
			Log.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		if(fame > _fame)
			sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_ACQUIRED_S1_REPUTATION).addNumber(fame - _fame));
		setFame(fame);
		sendChanges();
	}

	public int getVitalityLevel(boolean blessActive)
	{
		return Config.ALT_VITALITY_ENABLED ? (blessActive ? 4 : _vitalityLevel) : 0;
	}

	public double getVitality()
	{
		return Config.ALT_VITALITY_ENABLED ? _vitality : 0;
	}

	public void addVitality(double val)
	{
		setVitality(getVitality() + val);
	}

	public void setVitality(double newVitality)
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;

		newVitality = Math.max(Math.min(newVitality, Config.VITALITY_LEVELS[4]), 0);

		if(newVitality >= _vitality || getLevel() >= 10)
		{
			if(newVitality != _vitality)
				if(newVitality == 0)
					sendPacket(SystemMsg.YOUR_VITALITY_IS_FULLY_EXHAUSTED);
				else if(newVitality == Config.VITALITY_LEVELS[4])
					sendPacket(SystemMsg.YOUR_VITALITY_IS_AT_MAXIMUM);

			_vitality = newVitality;
		}

		int newLevel = 0;
		if(_vitality >= Config.VITALITY_LEVELS[3])
			newLevel = 4;
		else if(_vitality >= Config.VITALITY_LEVELS[2])
			newLevel = 3;
		else if(_vitality >= Config.VITALITY_LEVELS[1])
			newLevel = 2;
		else if(_vitality >= Config.VITALITY_LEVELS[0])
			newLevel = 1;

		if(_vitalityLevel > newLevel)
			getNevitSystem().addPoints(1500); //TODO: Количество от балды.

		if(_vitalityLevel != newLevel)
		{
			if(_vitalityLevel != -1) // при ините чара сообщения не шлём
				sendPacket(newLevel < _vitalityLevel ? SystemMsg.YOUR_VITALITY_HAS_DECREASED : SystemMsg.YOUR_VITALITY_HAS_INCREASED);
			_vitalityLevel = newLevel;
		}

		sendPacket(new ExVitalityPointInfo((int) _vitality));
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse)
	{
		_expandWarehouse = warehouse;
	}

	public int buffAnimRange()
	{
		return _buffAnimRange;
	}

	public void setBuffAnimRange(int value)
	{
		_buffAnimRange = value;
	}

	public boolean disableFogAndRain()
	{
		return _disableFogAndRain;
	}

	public void setDisableFogAndRain(boolean b)
	{
		_disableFogAndRain = b;
	}

	public boolean canSeeAllShouts()
	{
		return _canSeeAllShouts;
	}

	public void setCanSeeAllShouts(boolean b)
	{
		_canSeeAllShouts = b;
	}

	public void enterMovieMode()
	{
		if(isInMovie()) //already in movie
			return;

		setTarget(null);
		stopMove();
		setIsInMovie(true);
		//sendPacket(new CameraMode(1));
	}

	// DS: CameraMode(0) молча выключает режим EnterChat на клиенте, временно убрано поскольку работает и без этого
	// TODO: отсниффить и сделать как должно быть
	public void leaveMovieMode()
	{
		if(!isInMovie())
			return;

		setIsInMovie(false);
		//sendPacket(new CameraMode(0));
		//broadcastCharInfo();
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen ,unk));
	}

	private int _movieId = 0;
	private boolean _isInMovie;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _isInMovie;
	}

	public void setIsInMovie(boolean state)
	{
		_isInMovie = state;
	}

	public void showQuestMovie(SceneMovie movie)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		setIsInMovie(true);
		sendPacket(movie);
	}

	public void showQuestMovie(int movieId)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		setIsInMovie(true);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void setAutoLoot(int value)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = value;
			setVar("AutoLoot", String.valueOf(value), -1);
		}
	}

	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
	}

	public int isAutoLootEnabled()
	{
		return _autoLoot;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return _autoLootHerbs;
	}

	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			CharacterDAO.getInstance().updateName(getObjectId(), name);
		broadcastCharInfo();
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	public BypassStorage getBypassStorage()
	{
		return _bypassStorage;
	}

	public int getTalismanCount()
	{
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0, null, null);
	}

	public boolean getOpenCloak()
	{
		if(Config.ALT_OPEN_CLOAK_SLOT)
			return true;
		return (int) calcStat(Stats.CLOAK_SLOT, 0, null, null) > 0;
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	private ItemInstance _petControlItem = null;

	public void setPetControlItem(ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	private AtomicBoolean isActive = new AtomicBoolean();

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		setNonAggroTime(0);

		if(isActive.getAndSet(true))
			return;

		onActive();
	}

	private void onActive()
	{
		setNonAggroTime(0);
		sendPacket(SystemMsg.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);

		if(!_savedServitors.isEmpty())
			ThreadPoolManager.getInstance().execute(new ServitorSummonTask(this));
	}

	public void summonPet(ItemInstance controlItem, Location loc)
	{
		if(getServitor() != null)
			return;

		if(controlItem == null)
			return;

		int npcId = PetDataTable.getSummonId(controlItem);
		if(npcId == 0)
			return;

		NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(petTemplate == null)
			return;

		PetInstance pet = PetDAO.getInstance().select(this, controlItem, petTemplate);
		if(pet == null)
			return;

		setServitor(pet);

		if(!pet.isExistsInDatabase())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed());
			pet.updateControlItem();
		}

		pet.getInventory().restore();

		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setReflection(getReflection());
		pet.spawnMe(loc);
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();

		if(pet instanceof PetBabyInstance)
			((PetBabyInstance) pet).startBuffTask();

		PetEffectDAO.getInstance().select(pet);

		getListeners().onSummonServitor(pet);
	}

	public void summonSummon(int skillId)
	{
		if(getServitor() != null)
			return;

		SummonInstance summon = SummonDAO.getInstance().select(this, skillId);
		if(summon == null)
			return;

		setServitor(summon);

		summon.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		summon.setHeading(getHeading());
		summon.setReflection(getReflection());
		summon.spawnMe(Location.findPointToStay(this, 10, 20));
		summon.setRunning();
		summon.setFollowMode(true);

		if(summon.getSkillLevel(4140) > 0)
			summon.altUseSkill(SkillTable.getInstance().getSkillEntry(4140, summon.getSkillLevel(4140)), this);

		SummonEffectDAO.getInstance().select(summon);

		SiegeEvent<?,?> siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent != null)
			siegeEvent.updateSiegeSummon(this, summon);

		getListeners().onSummonServitor(summon);
	}

	public List<TrapInstance> getTraps()
	{
		return _traps;
	}

	public void addTrap(TrapInstance trap)
	{
		if(_traps == Collections.<TrapInstance>emptyList())
			_traps = new CopyOnWriteArrayList<TrapInstance>();

		_traps.add(trap);
	}

	public void removeTrap(TrapInstance trap)
	{
		_traps.remove(trap);
	}

	public void destroyAllTraps()
	{
		for(TrapInstance t : _traps)
			t.deleteMe();
	}

	public void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}

	public int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
			synchronized(this)
			{
				if(listeners == null)
					listeners = new PlayerListenerList(this);
			}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized(this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new PlayerStatsChangeRecorder(this);
			}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	private Future<?> _hourlyTask;
	private int _hoursInGame = 0;

	public int getHoursInGame()
	{
		_hoursInGame++;
		return _hoursInGame;
	}

	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), 3600000L, 3600000L);
	}

	public void stopHourlyTask()
	{
		if(_hourlyTask != null)
		{
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	//TODO: Сделать, если в конфиге указаны поинты аккаунта, изьятие поинтов с ДБ ЛСа.
	public long getPremiumPoints()
	{
		if(Config.GAME_POINT_ITEM_ID > 0)
			return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
		return 0;
	}

	public void reducePremiumPoints(final int val)
	{
		if(Config.GAME_POINT_ITEM_ID > 0)
			ItemFunctions.deleteItem(this, Config.GAME_POINT_ITEM_ID, val);
	}

	private boolean _agathionResAvailable = false;

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(boolean val)
	{
		_agathionResAvailable = val;
	}

	public boolean isClanAirShipDriver()
	{
		return isInBoat() && getBoat().isClanAirShip() && ((ClanAirShip) getBoat()).getDriver() == this;
	}

	/**
	 * _userSession - испольюзуется для хранения временных переменных.
	 */
	private Map<String, String> _userSession;

	public String getSessionVar(String key)
	{
		if(_userSession == null)
			return null;
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val)
	{
		if(_userSession == null)
			_userSession = new ConcurrentHashMap<String, String>();

		if(val == null || val.isEmpty())
			_userSession.remove(key);
		else
			_userSession.put(key, val);
	}

	public FriendList getFriendList()
	{
		return _friendList;
	}

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	public boolean isDebug()
	{
		return _debug;
	}

	public void setDebug(boolean b)
	{
		_debug = b;
	}

	public void sendItemList(boolean show)
	{
		ItemInstance[] items = getInventory().getItems();
		LockType lockType = getInventory().getLockType();
		int[] lockItems = getInventory().getLockItems();

		int allSize = items.length;
		int questItemsSize = 0;
		int agathionItemsSize = 0;
		for(ItemInstance item : items)
		{
			if(item.getTemplate().isQuest())
				questItemsSize++;
			if(item.getTemplate().getAgathionEnergy() > 0)
				agathionItemsSize ++;
		}

		sendPacket(new ItemList(allSize - questItemsSize, items, show, lockType, lockItems));
		if(questItemsSize > 0)
			sendPacket(new ExQuestItemList(questItemsSize, items, lockType, lockItems));
		if(agathionItemsSize > 0)
			sendPacket(new ExBR_AgathionEnergyInfo(agathionItemsSize, items));
	}

	public int getBeltInventoryIncrease()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT);
		if(item != null && item.getTemplate().getAttachedSkills() != null)
			for(SkillEntry skill : item.getTemplate().getAttachedSkills())
				for(FuncTemplate func : skill.getTemplate().getAttachedFuncs())
					if(func._stat == Stats.INVENTORY_LIMIT)
						return (int) func._value;
		return 0;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(Player target)
	{
		if(target.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isInCombat())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isCursedWeaponEquipped())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isOnSiegeField())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.getTransformation() != 0)
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addName(target));
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Servitor servitor = getServitor();
		if(servitor != null)
			servitor.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
		if(crit)
			if(magic)
				sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
			else
				sendPacket(new SystemMessage(SystemMsg.C1_LANDED_A_CRITICAL_HIT).addName(this));

		if(miss)
			sendPacket(new SystemMessage(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(this));
	}

	@Override
	public final void displayReceiveDamageMessage(Creature attacker, int damage, int transfered, int reflected, boolean toTargetOnly)
	{
		super.displayReceiveDamageMessage(attacker, damage, transfered, reflected, toTargetOnly);

		if(attacker != this && !isDead())
		{
			sendPacket(new SystemMessage(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addNumber(damage));
			if (reflected > 0)
				sendPacket(new SystemMessage(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(attacker).addName(this).addNumber(reflected));
		}
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public boolean isSharedGroupDisabled(int groupId)
	{
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId)
	{
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp)
	{
		_sharedGroupReuses.put(group, stamp);
	}

	public Collection<IntObjectPair<TimeStamp>> getSharedGroupReuses()
	{
		return _sharedGroupReuses.entrySet();
	}

	public void sendReuseMessage(ItemInstance item)
	{
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);

		if(hours > 0)
			sendPacket(new SystemMessage(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addNumber(seconds));
	}

	public NevitSystem getNevitSystem()
	{
		return _nevitSystem;
	}

	public void ask(ConfirmDlg dlg, OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		int rnd = Rnd.nextInt();
		_askDialog = new IntObjectPairImpl<OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public IntObjectPair<OnAnswerListener> getAskListener(boolean clear)
	{
		if(!clear)
			return _askDialog;
		else
		{
			IntObjectPair<OnAnswerListener> ask = _askDialog;
			_askDialog = null;
			return ask;
		}
	}

	@Override
	public int getAgathionEnergy()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		return item == null ? 0 : item.getAgathionEnergy();
	}

	@Override
	public void setAgathionEnergy(int val)
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null)
			return;
		item.setAgathionEnergy(val);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendPacket(new ExBR_AgathionEnergyInfo(1, item));
	}

	public boolean hasPrivilege(Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
		if (matchingRoom == null)
			_matchingRoomWindowOpened = false;
	}

	public boolean isMatchingRoomWindowOpened()
	{
		return _matchingRoomWindowOpened;
	}

	public void setMatchingRoomWindowOpened(boolean b)
	{
		_matchingRoomWindowOpened = b;
	}

	public void setInstanceReuse(int id, long time)
	{
		sendPacket(new SystemMessage(SystemMsg.INSTANT_ZONE_S1_ENTRY_HAS_BEEN_RESTRICTED).addName(this));
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id)
	{
		if(_instancesReuses.remove(id) != null)
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId)
	{
		for(int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
				removeInstanceReuse(i);
	}

	public Long getInstanceReuse(int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	private void loadInstanceReuses()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public Reflection getActiveReflection()
	{
		for(Reflection r : ReflectionManager.getInstance().getAll())
			if(r != null && ArrayUtils.contains(r.getVisitors(), getObjectId()))
				return r;
		return null;
	}

	@Override
	public void broadCast(IBroadcastPacket... packet)
	{
		sendPacket(packet);
	}

	@Override
	public int getMemberCount()
	{
		return 1;
	}

	@Override
	public Player getGroupLeader()
	{
		return this;
	}

	@Override
	public Iterator<Player> iterator()
	{
		return Collections.singleton(this).iterator();
	}

	public PlayerGroup getPlayerGroup()
	{
		if(getParty() != null)
		{
			if(getParty().getCommandChannel() != null)
				return getParty().getCommandChannel();
			else
				return getParty();
		}
		else
			return this;
	}

	public boolean isActionBlocked(String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions)
	{
		for(String action : actions)
			_blockedActions.remove(action);
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public void setOlympiadGame(OlympiadGame olympiadGame)
	{
		_olympiadGame = olympiadGame;
	}

	public OlympiadGame getOlympiadObserveGame()
	{
		return _olympiadObserveGame;
	}

	public void setOlympiadObserveGame(OlympiadGame olympiadObserveGame)
	{
		_olympiadObserveGame = olympiadObserveGame;
	}

	public void addRadar(int x, int y, int z)
	{
		sendPacket(new RadarControl(0, 1, x, y, z));
	}

	public void addRadarWithMap(int x, int y, int z)
	{
		sendPacket(new RadarControl(0, 2, x, y, z));
	}

	public void removeRadar()
	{
		sendPacket(new RadarControl(2, 2, 0, 0, 0));
	}

	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(int lectureMark, boolean broadcast)
	{
		if(lectureMark != 0)
		{
			TreeSet<Long> set = new TreeSet<Long>();
			set.add(getCreateTime());
			for(AccountPlayerInfo p : getAccountChars().values())
				set.add(p.getCreateTime());

			Calendar firstCreate = Calendar.getInstance();
			firstCreate.setTimeInMillis(set.pollFirst());
			firstCreate.add(Calendar.MONTH, 6);
			firstCreate.set(Calendar.DAY_OF_MONTH, 1);

			if(lectureMark == -1)
				lectureMark = INITIAL_MARK;

			switch(lectureMark)
			{
				case INITIAL_MARK:
					// если после создания первого чара, прошло 6 месяцев
					if(firstCreate.getTimeInMillis() < System.currentTimeMillis())
						lectureMark = OFF_MARK;
					break;
				case EVANGELIST_MARK:
					// если после создания первого чара, непрошло 6 месяцев
					if(firstCreate.getTimeInMillis() > System.currentTimeMillis())
						lectureMark = OFF_MARK;
					break;
			}

			if(_lectureMark == INITIAL_MARK)
				_lectureEndTask = ThreadPoolManager.getInstance().schedule(new LectureInitialPeriodEndTask(this), firstCreate.getTimeInMillis() - System.currentTimeMillis());
			else
				stopLectureTask();

			AccountLectureMarkDAO.getInstance().replace(getAccountName(), lectureMark);
		}

		_lectureMark = lectureMark;

		if(broadcast)
			broadcastUserInfo(true);
	}                         
        
	public void stopLectureTask()
	{
		if(_lectureEndTask != null)
		{
			_lectureEndTask.cancel(false);
			_lectureEndTask = null;
		}
	}

	public boolean isUserRelationActive()
	{
		return _enableRelationTask == null;
	}

	public void startEnableUserRelationTask(long time, SiegeEvent<?, ?> siegeEvent)
	{
		if(_enableRelationTask != null)
			return;

		_enableRelationTask = ThreadPoolManager.getInstance().schedule(new EnableUserRelationTask(this, siegeEvent), time);
	}

	public void stopEnableUserRelationTask()
	{
		if(_enableRelationTask != null)
		{
			_enableRelationTask.cancel(false);
			_enableRelationTask = null;
		}
	}

	public int getTpBookmarkSize()
	{
		return _tpBookmarkSize;
	}

	public void setTpBookmarkSize(int teleportBookmarkSize)
	{
		_tpBookmarkSize = teleportBookmarkSize;
	}

	public List<TpBookMark> getTpBookMarks()
	{
		return _tpBookMarks;
	}

	public boolean isTeleportBlocked()
	{
		return _isTeleportBlocked;
	}

	public void setIsTeleportBlocked(boolean val)
	{
		_isTeleportBlocked = val;
	}

	public Henna[] getHennas()
	{
		return _henna;
	}

	@Override
	public void setTeam(TeamType t)
	{
		super.setTeam(t);
		sendChanges();
		Servitor servitor = getServitor();
		if(servitor != null)
			servitor.sendChanges();
	}

	public IntObjectMap<SkillEntry> getTransformationSkills()
	{
		return _transformationSkills;
	}

	public List<int[]> getSavedServitors()
	{
		List<int[]> list = _savedServitors;
		_savedServitors = Collections.emptyList();
		return list;
	}

	public void setLastNpcInteractionTime()
	{
		_lastNpcInteractionTime = System.currentTimeMillis();
	}

	public boolean canMoveAfterInteraction()
	{
		return System.currentTimeMillis() - _lastNpcInteractionTime > 1500L;
	}

	public void broadcastRelation()
	{
		if(!isVisible() || isInvisible())
			return;

		for(Player target : World.getAroundObservers(this))
		{
			RelationChanged relationChanged = new RelationChanged();
			relationChanged.add(this, target);

			target.sendPacket(relationChanged);
		}
	}
}