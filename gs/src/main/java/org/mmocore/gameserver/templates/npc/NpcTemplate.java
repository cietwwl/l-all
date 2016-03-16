package org.mmocore.gameserver.templates.npc;

import gnu.trove.TIntObjectHashMap;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.commons.util.TroveUtils;
import org.mmocore.gameserver.ai.CharacterAI;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.TeleportLocation;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestEventType;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.scripts.Scripts;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.effects.EffectTemplate;
import org.mmocore.gameserver.templates.CharTemplate;
import org.mmocore.gameserver.templates.StatsSet;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NpcTemplate extends CharTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(NpcTemplate.class);

	@SuppressWarnings("unchecked")
	public static final Constructor<NpcInstance> DEFAULT_TYPE_CONSTRUCTOR = (Constructor<NpcInstance>)NpcInstance.class.getConstructors()[0];
	@SuppressWarnings("unchecked")
	public static final Constructor<CharacterAI> DEFAULT_AI_CONSTRUCTOR = (Constructor<CharacterAI>)CharacterAI.class.getConstructors()[0];

	public final int npcId;
	public final String name;
	public final String title;
	public final int level;
	public final long rewardExp;
	public final int rewardSp;
	public final int rewardRp;
	public final int aggroRange;
	public final int rhand;
	public final int lhand;
	public final double rateHp;

	private final int _soulShotCount;
	private final int _spiritShotCount;

	private Faction faction = Faction.NONE;

	public final int displayId;

	public final boolean isRaid;
	private final StatsSet _AIParams;

	/** fixed skills*/
	private int race = 0;
	private final int _castleId;

	private List<RewardList> _rewardList = Collections.emptyList();

	private TIntObjectHashMap<TeleportLocation[]> _teleportList = TroveUtils.emptyIntObjectMap();
	private List<MinionData> _minions = Collections.emptyList();
	private List<AbsorbInfo> _absorbInfo = Collections.emptyList();

	private List<ClassId> _teachInfo = Collections.emptyList();
	private Map<QuestEventType, Quest[]> _questEvents = Collections.emptyMap();
	private IntObjectMap<SkillEntry> _skills = Containers.emptyIntObjectMap();

	private SkillEntry[] _damageSkills = SkillEntry.EMPTY_ARRAY;
	private SkillEntry[] _dotSkills = SkillEntry.EMPTY_ARRAY;
	private SkillEntry[] _debuffSkills = SkillEntry.EMPTY_ARRAY;
	private SkillEntry[] _buffSkills = SkillEntry.EMPTY_ARRAY;
	private SkillEntry[] _stunSkills = SkillEntry.EMPTY_ARRAY;
	private SkillEntry[] _healSkills = SkillEntry.EMPTY_ARRAY;

	private Class<NpcInstance> _classType = NpcInstance.class;
	private Constructor<NpcInstance> _constructorType = DEFAULT_TYPE_CONSTRUCTOR;

	private Class<CharacterAI> _classAI = CharacterAI.class;
	private Constructor<CharacterAI> _constructorAI = DEFAULT_AI_CONSTRUCTOR;

	private final String _htmRoot;

	public NpcTemplate(StatsSet set)
	{
		super(set);
		npcId = set.getInteger("npcId");
		displayId = set.getInteger("displayId");

		name = set.getString("name");
		title = set.getString("title");
		level = set.getInteger("level");
		rewardExp = set.getLong("rewardExp");
		rewardSp = set.getInteger("rewardSp");
		rewardRp = (int)set.getDouble("rewardRp");
		aggroRange = set.getInteger("aggroRange");
		rhand = set.getInteger("rhand", 0);
		lhand = set.getInteger("lhand", 0);
		rateHp = set.getDouble("baseHpRate");
		isRaid = set.getBool("isRaid", false);
		_htmRoot = set.getString("htm_root", null);
		_castleId = set.getInteger("castle_id", 0);
		_soulShotCount = set.getInteger("soulshot_count", 0);
		_spiritShotCount = set.getInteger("spiritshot_count", 0);
		_AIParams = (StatsSet) set.getObject("aiParams", StatsSet.EMPTY);

		setType(set.getString("type", null));
		setAI(set.getString("ai_type", null));
	}

	public Class<? extends NpcInstance> getInstanceClass()
	{
		return _classType;
	}

	public Constructor<? extends NpcInstance> getInstanceConstructor()
	{
		return _constructorType;
	}

	public boolean isInstanceOf(Class<?> _class)
	{
		return _class.isAssignableFrom(_classType);
	}

	/**
	 * Создает новый инстанс NPC. Для него следует вызывать (именно в этом порядке):
	 * <br> setSpawnedLoc (обязательно)
	 * <br> setReflection (если reflection не базовый)
	 * <br> setChampion (опционально)
	 * <br> setCurrentHpMp (если вызывался setChampion)
	 * <br> spawnMe (в качестве параметра брать getSpawnedLoc)
	 */
	public NpcInstance getNewInstance()
	{
		try
		{
			return _constructorType.newInstance(IdFactory.getInstance().getNextId(), this);
		}
		catch(Exception e)
		{
			_log.error("Unable to create instance of NPC " + npcId, e);
		}

		return null;
	}

	public CharacterAI getNewAI(NpcInstance npc)
	{
		try
		{
			return _constructorAI.newInstance(npc);
		}
		catch(Exception e)
		{
			_log.error("Unable to create ai of NPC " + npcId, e);
		}

		return new CharacterAI(npc);
	}

	@SuppressWarnings("unchecked")
	private void setType(String type)
	{
		Class<NpcInstance> classType = null;
		try
		{
			classType = (Class<NpcInstance>) Class.forName("org.mmocore.gameserver.model.instances." + type + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");
		}

		if(classType == null)
			_log.error("Not found type class for type: " + type + ". NpcId: " + npcId);
		else
		{
			_classType = classType;
			_constructorType = (Constructor<NpcInstance>)_classType.getConstructors()[0];
		}

		if(_classType.isAnnotationPresent(Deprecated.class))
			_log.error("Npc type: " + type + ", is deprecated. NpcId: " + npcId);
	}

	@SuppressWarnings("unchecked")
	private void setAI(String ai)
	{
		Class<CharacterAI> classAI = null;
		try
		{
			classAI = (Class<CharacterAI>) Class.forName("org.mmocore.gameserver.ai." + ai);
		}
		catch(ClassNotFoundException e)
		{
			classAI = (Class<CharacterAI>) Scripts.getInstance().getClasses().get("ai." + ai);
		}

		if(classAI == null)
			_log.error("Not found ai class for ai: " + ai + ". NpcId: " + npcId);
		else
		{
			_classAI = classAI;
			_constructorAI = (Constructor<CharacterAI>)_classAI.getConstructors()[0];
		}

		if(_classAI.isAnnotationPresent(Deprecated.class))
			_log.error("Ai type: " + ai + ", is deprecated. NpcId: " + npcId);
	}

	public void addTeachInfo(ClassId classId)
	{
		if(_teachInfo.isEmpty())
			_teachInfo = new ArrayList<ClassId>(1);
		_teachInfo.add(classId);
	}

	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}

	public boolean canTeach(ClassId classId)
	{
		return _teachInfo.contains(classId);
	}

	public void addTeleportList(int id, TeleportLocation[] list)
	{
		if(_teleportList.isEmpty())
			_teleportList = new TIntObjectHashMap<TeleportLocation[]>(1);

		_teleportList.put(id, list);
	}

	public TeleportLocation[] getTeleportList(int id)
	{
		return _teleportList.get(id);
	}

	public TIntObjectHashMap<TeleportLocation[]> getTeleportList()
	{
		return _teleportList;
	}

	public void addRewardList(RewardList rewardList)
	{
		if(_rewardList.isEmpty())
			_rewardList = new CopyOnWriteArrayList<RewardList>();

		_rewardList.add(rewardList);
	}

	public void removeRewardList(RewardList rewardList)
	{
		_rewardList.remove(rewardList);
	}

	public Collection<RewardList> getRewards()
	{
		return _rewardList;
	}

	public void addAbsorbInfo(AbsorbInfo absorbInfo)
	{
		if(_absorbInfo.isEmpty())
			_absorbInfo = new ArrayList<AbsorbInfo>(1);

		_absorbInfo.add(absorbInfo);
	}

	public void addMinion(MinionData minion)
	{
		if(_minions.isEmpty())
			_minions = new ArrayList<MinionData>(1);

		_minions.add(minion);
	}

	public void setFaction(Faction faction)
	{
		this.faction = faction;
	}

	public Faction getFaction()
	{
		return faction;
	}

	public void addSkill(SkillEntry skill)
	{
		if(_skills.isEmpty())
			_skills = new HashIntObjectMap<SkillEntry> ();

		_skills.put(skill.getId(), skill);

		//TODO [G1ta0] перенести в AI
		if(skill.getTemplate().isNotUsedByAI() || skill.getTemplate().getTargetType() == Skill.SkillTargetType.TARGET_NONE || skill.getSkillType() == Skill.SkillType.NOTDONE || !skill.getTemplate().isActive())
			return;

		switch(skill.getSkillType())
		{
			case PDAM:
			case MANADAM:
			case MDAM:
			case DRAIN:
			case DRAIN_SOUL:
			{
				boolean added = false;

				if(skill.getTemplate().hasEffects())
					for(EffectTemplate eff : skill.getTemplate().getEffectTemplates())
						switch(eff.getEffectType())
						{
							case Stun:
								_stunSkills = ArrayUtils.add(_stunSkills, skill);
								added = true;
								break;
							case DamOverTime:
							case DamOverTimeLethal:
							case ManaDamOverTime:
							case LDManaDamOverTime:
								_dotSkills = ArrayUtils.add(_dotSkills, skill);
								added = true;
								break;
						}

				if(!added)
					_damageSkills = ArrayUtils.add(_damageSkills, skill);

				break;
			}
			case DOT:
			case MDOT:
			case POISON:
			case BLEED:
				_dotSkills = ArrayUtils.add(_dotSkills, skill);
				break;
			case DEBUFF:
			case SLEEP:
			case ROOT:
			case PARALYZE:
			case MUTE:
			case TELEPORT_NPC:
			case AGGRESSION:
				_debuffSkills = ArrayUtils.add(_debuffSkills, skill);
				break;
			case BUFF:
				_buffSkills = ArrayUtils.add(_buffSkills, skill);
				break;
			case STUN:
				_stunSkills = ArrayUtils.add(_stunSkills, skill);
				break;
			case HEAL:
			case HEAL_PERCENT:
			case HOT:
			case MANAHEAL:
				_healSkills = ArrayUtils.add(_healSkills, skill);
				break;
			default:

				break;
		}
	}

	public SkillEntry[] getDamageSkills()
	{
		return _damageSkills;
	}

	public SkillEntry[] getDotSkills()
	{
		return _dotSkills;
	}

	public SkillEntry[] getDebuffSkills()
	{
		return _debuffSkills;
	}

	public SkillEntry[] getBuffSkills()
	{
		return _buffSkills;
	}

	public SkillEntry[] getStunSkills()
	{
		return _stunSkills;
	}

	public SkillEntry[] getHealSkills()
	{
		return _healSkills;
	}

	public List<MinionData> getMinionData()
	{
		return _minions;
	}

	public IntObjectMap<SkillEntry> getSkills()
	{
		return _skills;
	}

	public void addQuestEvent(QuestEventType EventType, Quest q)
	{
		if(_questEvents.isEmpty())
			_questEvents = new HashMap<QuestEventType, Quest[]>();

		if(_questEvents.get(EventType) == null)
			_questEvents.put(EventType, new Quest[] { q });
		else
		{
			Quest[] _quests = _questEvents.get(EventType);
			int len = _quests.length;

			Quest[] tmp = new Quest[len + 1];
			for(int i = 0; i < len; i++)
			{
				if(_quests[i].getName().equals(q.getName()))
				{
					_quests[i] = q;
					return;
				}
				tmp[i] = _quests[i];
			}
			tmp[len] = q;

			_questEvents.put(EventType, tmp);
		}
	}

	public Quest[] getEventQuests(QuestEventType EventType)
	{
		return _questEvents.get(EventType);
	}

	public int getRace()
	{
		return race;
	}

	public void setRace(int race)
	{
		this.race = race;
	}

	public boolean isUndead()
	{
		return race == 1;
	}

	@Override
	public String toString()
	{
		return "Npc template " + name + "[" + npcId + "]";
	}

	@Override
	public int getNpcId()
	{
		return npcId;
	}

	public String getName()
	{
		return name;
	}

	public final StatsSet getAIParams()
	{
		return _AIParams;
	}

	public List<AbsorbInfo> getAbsorbInfo()
	{
		return _absorbInfo;
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public Map<QuestEventType, Quest[]> getQuestEvents()
	{
		return _questEvents;
	}

	public String getHtmRoot()
	{
		return _htmRoot;
	}

	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
}