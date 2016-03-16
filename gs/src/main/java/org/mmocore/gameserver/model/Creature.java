package org.mmocore.gameserver.model;

import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.lang.reference.HardReferences;
import org.mmocore.commons.listener.Listener;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.util.Rnd;
import org.mmocore.commons.util.concurrent.atomic.AtomicState;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CharacterAI;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.ai.PlayableAI.nextAction;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.geodata.GeoMove;
import org.mmocore.gameserver.instancemanager.DimensionalRiftManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.GameObjectTasks.AltMagicUseTask;
import org.mmocore.gameserver.model.GameObjectTasks.CastEndTimeTask;
import org.mmocore.gameserver.model.GameObjectTasks.HitTask;
import org.mmocore.gameserver.model.GameObjectTasks.MagicGeoCheckTask;
import org.mmocore.gameserver.model.GameObjectTasks.MagicLaunchedTask;
import org.mmocore.gameserver.model.GameObjectTasks.MagicUseTask;
import org.mmocore.gameserver.model.GameObjectTasks.NotifyAITask;
import org.mmocore.gameserver.model.Skill.SkillTargetType;
import org.mmocore.gameserver.model.Skill.SkillType;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.actor.listener.CharListenerList;
import org.mmocore.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import org.mmocore.gameserver.model.base.SkillMastery;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.quest.QuestEventType;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.model.reference.L2Reference;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.Attack;
import org.mmocore.gameserver.network.l2.s2c.AutoAttackStart;
import org.mmocore.gameserver.network.l2.s2c.AutoAttackStop;
import org.mmocore.gameserver.network.l2.s2c.ChangeMoveType;
import org.mmocore.gameserver.network.l2.s2c.CharMoveToLocation;
import org.mmocore.gameserver.network.l2.s2c.FlyToLocation;
import org.mmocore.gameserver.network.l2.s2c.FlyToLocation.FlyType;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillCanceled;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillLaunched;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillUse;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;
import org.mmocore.gameserver.network.l2.s2c.SetupGauge;
import org.mmocore.gameserver.network.l2.s2c.StatusUpdate;
import org.mmocore.gameserver.network.l2.s2c.StopMove;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.network.l2.s2c.TeleportToLocation;
import org.mmocore.gameserver.network.l2.s2c.ValidateLocation;
import org.mmocore.gameserver.skills.AbnormalEffect;
import org.mmocore.gameserver.skills.AbnormalEffectType;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.SkillEntryType;
import org.mmocore.gameserver.skills.TimeStamp;
import org.mmocore.gameserver.stats.Calculator;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Formulas.AttackInfo;
import org.mmocore.gameserver.stats.StatFunctions;
import org.mmocore.gameserver.stats.StatTemplate;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.stats.funcs.Func;
import org.mmocore.gameserver.stats.triggers.TriggerInfo;
import org.mmocore.gameserver.stats.triggers.TriggerType;
import org.mmocore.gameserver.taskmanager.LazyPrecisionTaskManager;
import org.mmocore.gameserver.taskmanager.RegenTaskManager;
import org.mmocore.gameserver.templates.CharTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.Log;
import org.mmocore.gameserver.utils.PositionUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Creature extends GameObject
{
	public class MoveNextTask extends RunnableImpl
	{
		private double alldist, donedist;

		public MoveNextTask setDist(double dist)
		{
			alldist = dist;
			donedist = 0.;
			return this;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isMoving)
				return;

			moveLock.lock();
			try
			{
				if(!isMoving)
					return;

				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				Creature follow = null;
				int speed = getMoveSpeed();
				if(speed <= 0)
				{
					stopMove();
					return;
				}
				long now = System.currentTimeMillis();

				if(isFollow)
				{
					follow = getFollowTarget();
					if(follow == null)
					{
						stopMove();
						return;
					}
					if(isInRangeZ(follow, _offset) && GeoEngine.canSeeTarget(Creature.this, follow, false))
					{
						stopMove();
						ThreadPoolManager.getInstance().execute(new NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
						return;
					}
				}

				if(alldist <= 0)
				{
					moveNext(false);
					return;
				}

				donedist += (now - _startMoveTime) * _previousSpeed / 1000.;
				double done = donedist / alldist;

				if(done < 0)
					done = 0;
				if(done >= 1)
				{
					moveNext(false);
					return;
				}

				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				Location loc = null;

				int index = (int) (moveList.size() * done);
				if(index >= moveList.size())
					index = moveList.size() - 1;
				if(index < 0)
					index = 0;

				loc = moveList.get(index).clone().geo2world();

				if(!isFlying() && !isInBoat() && !isInWater() && !isBoat())
					if(loc.z - getZ() > 256)
					{
						String bug_text = "geo bug 1 at: " + getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + moveList.get(0) + " => " + moveList.get(moveList.size() - 1);
						Log.add(bug_text, "geo");
						stopMove();
						return;
					}

				// Проверяем, на всякий случай
				if(loc == null || isMovementDisabled())
				{
					stopMove();
					return;
				}

				setLoc(loc, true);

				// В процессе изменения координат, мы остановились
				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				if(isFollow && now - _followTimestamp > (_forestalling ? 500 : 1000) && follow != null && !follow.isInRange(movingDestTempPos, Math.max(50, _offset)))
				{
					if(Math.abs(getZ() - loc.z) > 1000 && !isFlying())
					{
						sendPacket(SystemMsg.CANNOT_SEE_TARGET);
						stopMove();
						return;
					}
					if(buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true))
						movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
					else
					{
						stopMove();
						return;
					}
					moveNext(true);
					return;
				}

				_previousSpeed = speed;
				_startMoveTime = now;
				_moveTask = ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval());
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				moveLock.unlock();
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Creature.class);

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;

	private SkillEntry _castingSkill;

	private long _castInterruptTime;
	private long _animationEndTime;

	public int _scheduledCastCount;
	public int _scheduledCastInterval;

	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	public Future<?> _skillGeoCheckTask; // используется и как флаг: null - проверка успешно пройдена или не нужна

	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;

	public final static int CLIENT_BAR_SIZE = 352; // 352 - размер полоски CP/HP/MP в клиенте, в пикселях

	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;

	protected double _currentCp = 0;
	protected double _currentHp = 1;
	protected double _currentMp = 1;

	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;

	private static final double[] POLE_VAMPIRIC_MOD = { 1, 0.5, 0,25, 0.125, 0.06, 0.03, 0.01 };

	protected final IntObjectMap<SkillEntry> _skills = new CTreeIntObjectMap<SkillEntry> ();
	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<TimeStamp>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();

	protected volatile EffectList _effectList;

	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;

	private List<Stats> _blockedStats;

	private int[] _abnormalEffects = new int[AbnormalEffectType.VALUES.length];

	protected AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();

	private volatile int _skillMasteryId = 0;

	private SpecialEffectState _invulState = SpecialEffectState.FALSE;

	private boolean _isBlessedByNoblesse; // Восстанавливает все бафы после смерти
	private boolean _isSalvation; // Восстанавливает все бафы после смерти и полностью CP, MP, HP

	private boolean _meditated;
	private boolean _lockedTarget;

	private boolean _blocked;

	private AtomicState _afraid = new AtomicState();
	private AtomicState _muted = new AtomicState();
	private AtomicState _pmuted = new AtomicState();
	private AtomicState _amuted = new AtomicState();
	private AtomicState _paralyzed = new AtomicState();
	private AtomicState _rooted = new AtomicState();
	private AtomicState _sleeping = new AtomicState();
	private AtomicState _stunned = new AtomicState();
	private AtomicState _immobilized = new AtomicState();
	private AtomicState _confused = new AtomicState();
	private boolean _frozen;

	private AtomicState _healBlocked = new AtomicState();
	private AtomicState _damageBlocked = new AtomicState();
	private AtomicState _buffImmunity = new AtomicState(); // Иммунитет к бафам
	private AtomicState _debuffImmunity = new AtomicState(); // Иммунитет к дебафам
	private AtomicState _effectImmunity = new AtomicState(); // Иммунитет ко всем эффектам

	private AtomicState _weaponEquipBlocked = new AtomicState();

	private boolean _flying;

	private boolean _running;

	public boolean isMoving;
	public boolean isFollow;
	private final Lock moveLock = new ReentrantLock();
	private Future<?> _moveTask;
	private MoveNextTask _moveTaskRunnable;
	private List<Location> moveList;
	private Location destination;
	/**
	 * при moveToLocation используется для хранения геокоординат в которые мы двигаемся для того что бы избежать повторного построения одного и того же пути
	 * при followToCharacter используется для хранения мировых координат в которых находилась последний раз преследуемая цель для отслеживания необходимости перестраивания пути
	 */
	private final Location movingDestTempPos = new Location();
	private int _offset;

	private boolean _forestalling;

	private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> followTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();

	private final List<List<Location>> _targetRecorder = new ArrayList<List<Location>>();
	private long _followTimestamp, _startMoveTime;
	private int _previousSpeed = 0;

	private int _heading;

	private final Calculator[] _calculators;

	protected CharTemplate _template;
	protected CharTemplate _baseTemplate;

	protected volatile CharacterAI _ai;

	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;

	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;

	protected SpecialEffectState _undyingState = SpecialEffectState.FALSE;
	private AtomicBoolean _undyingFlag = new AtomicBoolean(false);

	private List<Zone> _zones = new LazyArrayList<Zone>();
	/** Блокировка для чтения/записи объектов из региона */
	private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
	private final Lock zonesRead = zonesLock.readLock();
	private final Lock zonesWrite = zonesLock.writeLock();

	protected volatile CharListenerList listeners;

	/** Список игроков, которым необходимо отсылать информацию об изменении состояния персонажа */
	private List<Player> _statusListeners;
	private final Lock statusListenersLock = new ReentrantLock();

	protected HardReference<? extends Creature> reference;

	public Creature(int objectId, CharTemplate template)
	{
		super(objectId);

		_template = template;
		_baseTemplate = template;

		_calculators = new Calculator[Stats.NUM_STATS];

		StatFunctions.addPredefinedFuncs(this);

		reference = new L2Reference<Creature>(this);

		GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0;
			if(force)
				_isAttackAborted = true;

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage(SystemMsg.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}

	public final void abortCast(boolean force, boolean message)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			final SkillEntry castingSkill = _castingSkill;
			final Future<?> skillTask = _skillTask;
			final Future<?> skillLaunchedTask = _skillLaunchedTask;
			final Future<?> skillGeoCheckTask = _skillGeoCheckTask;

			finishFly(true); // Броадкаст пакета FlyToLoc уже выполнен, устанавливаем координаты чтобы не было визуальных глюков
			clearCastVars();

			if(skillTask != null)
				skillTask.cancel(false); // cancels the skill hit scheduled task

			if(skillLaunchedTask != null)
				skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task

			if(skillGeoCheckTask != null)
				skillGeoCheckTask.cancel(false); // cancels the skill hit scheduled task

			if(castingSkill != null && castingSkill.getTemplate().isUsingWhileCasting())
			{
				Creature target = getAI().getAttackTarget();
				if(target != null)
					target.getEffectList().stopEffect(castingSkill.getId());
			}

			broadcastPacket(new MagicSkillCanceled(this)); // broadcast packet to stop animations client-side

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
				sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}

	private double absorbAndReflect(Creature target, SkillEntry skillEntry, double damage, int poleAttackCount)
	{
		if(target.isDead())
			return 0;

		final WeaponType weapon = getActiveWeaponItem() != null ? getActiveWeaponItem().getItemType() : getTemplate().baseAtkType;
		final boolean bow = weapon == WeaponType.BOW || weapon == WeaponType.CROSSBOW;

		double value = 0;

		Skill skill = skillEntry == null ? null : skillEntry.getTemplate();
		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, this, skillEntry);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, this, skillEntry);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, this, null);

		//Цель отразила весь урон
		if(value > 0 && Rnd.chance(value))
		{
			reduceCurrentHp(damage, target, null, 0, false, true, true, false, false, false, false, true);
			return -1;
		}

		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, this, skillEntry);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, this, skillEntry);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, this, null);

		double reflectedDamage = 0;
		if(value > 0)
		{
			//Цель в состоянии отразить часть урона
			if(target.getCurrentHp() + target.getCurrentCp() > damage)
			{
				reflectedDamage = value / 100. * damage;
				reduceCurrentHp(reflectedDamage, target, null, 0, false, true, true, false, false, false, false, false);
			}
		}

		if(skill != null || bow)
			return reflectedDamage;

		// вампирик
		double damageToHp = damage - target.getCurrentCp();
		if(damageToHp <= 0)
			return reflectedDamage;

		damageToHp = Math.min(damageToHp, target.getCurrentHp());
		final double poleMod = poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[poleAttackCount] : 0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, target, null);
		double limit;
		if(absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if (getCurrentHp() < limit)
				setCurrentHp(Math.min(_currentHp + damageToHp * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit), false);
		}

		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if(absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if (getCurrentMp() < limit)
				setCurrentMp(Math.min(_currentMp + damageToHp * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit));
		}

		return reflectedDamage;
	}

	public double absorbToEffector(Creature attacker, double damage)
	{
		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.);
		if (transferToEffectorDam <= 0)
			return 0;

		final Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
		if (effect == null)
			return 0;

		final Creature effector = effect.getEffector();
		// на мертвого чара, не онлайн игрока - не даем абсорб, и не на самого себя
		if (effector == this || effector.isDead() || !isInRange(effector, 1000))
			return 0;

		double transferDamage = (damage * transferToEffectorDam) * .01;
		if (effector.getCurrentHp() > transferDamage + 1.)
			effector.reduceCurrentHp(transferDamage, attacker, null, 0, false, false, false, !attacker.isPlayable(), false, true, false, true);
		else
			return -transferDamage;

		return transferDamage;
	}

	public double absorbToMp(Creature attacker, double damage)
	{
		double transferToMpDamPercent = calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.);
		if(transferToMpDamPercent > 0)
		{
			double transferDamage = (damage * transferToMpDamPercent) * .01;

			double currentMp = getCurrentMp();
			if(currentMp > transferDamage)
			{
				sendPacket(new SystemMessage(SystemMsg.DUE_TO_THE_EFFECT_OF_THE_ARCANE_SHIELD_MP_RATHER_THAN_HP_RECEIVED_S1S_DAMAGE).addNumber((long)transferDamage));
				setCurrentMp(getCurrentMp() - transferDamage);
				return 0;
			}
			else
			{
				if(currentMp > 0)
				{
					damage -= currentMp;
					setCurrentMp(0);
					sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
				}
				getEffectList().stopEffects(EffectType.AbsorbDamageToMp);
			}

			return damage;
		}
		return damage;
	}

	public double absorbToSummon(Creature attacker, double damage)
	{
		final Servitor servitor = getServitor();
		if(servitor == null || servitor.isDead() || !servitor.isSummon() || !servitor.isInRangeZ(this, 1000))
			return 0;

		double transferDamage = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0);
		if (transferDamage <= 0)
			return 0;

		transferDamage = transferDamage * damage * 0.01;
		if(servitor.getCurrentHp() > transferDamage + 1.)
			servitor.reduceCurrentHp(transferDamage, attacker, null, 0, false, false, false, false, false, true, false, true);
		else
			return -transferDamage;

		return transferDamage;
	}

	public void addBlockStats(List<Stats> stats)
	{
		if(_blockedStats == null)
			_blockedStats = new ArrayList<Stats>();
		_blockedStats.addAll(stats);
	}

	public SkillEntry addSkill(SkillEntry newSkill)
	{
		if(newSkill == null)
			return null;

		SkillEntry oldSkill = _skills.get(newSkill.getId());

		if(oldSkill != null && oldSkill.getLevel() == newSkill.getLevel())
			return newSkill;

		_skills.put(newSkill.getId(), newSkill);

		if(oldSkill != null)
		{
			removeStatsByOwner(oldSkill);
			removeTriggers(oldSkill.getTemplate());
		}

		if(!newSkill.isDisabled())
		{
			addTriggers(newSkill.getTemplate());

			addStatFuncs(newSkill.getStatFuncs());
		}

		return oldSkill;
	}

	public Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final void addStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] == null)
				_calculators[stat] = new Calculator(f.stat, this);
			_calculators[stat].addFunc(f);
		}
	}

	public final void addStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			addStatFunc(f);
	}

	public final void removeStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] != null)
				_calculators[stat].removeFunc(f);
		}
	}

	public final void removeStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			removeStatFunc(f);
	}

	public final void removeStatsByOwner(Object owner)
	{
		synchronized (_calculators)
		{
			for(int i = 0; i < _calculators.length; i++)
				if(_calculators[i] != null)
					_calculators[i].removeOwner(owner);
		}
	}

	public void altOnMagicUseTimer(Creature aimingTarget, SkillEntry skillEntry)
	{
		if(isDead())
			return;
		Skill skill = skillEntry.getTemplate();
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(_currentMp < mpConsume2)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			if(skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skillEntry), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skillEntry), null);
		}

		callSkill(skillEntry, targets, false);
		removeSkillMastery(skillEntry);

		broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
	}

	public void altUseSkill(SkillEntry skillEntry, Creature target)
	{
		if(skillEntry == null || skillEntry.isDisabled())
			return;
		Skill skill = skillEntry.getTemplate();
		int magicId = skill.getId();

		if(isSkillDisabled(skillEntry))
		{
			sendReuseMessage(skillEntry);
			return;
		}
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		if (!skill.checkPreConditions(skillEntry, this, target))
			return;

		getListeners().onMagicUse(skillEntry, target, true);

		final double mpConsume1 = skill.getMpConsume1();
		if(mpConsume1 > 0)
		{
			if(_currentMp < mpConsume1)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}

		int itemConsume[] = skill.getItemConsume();
		if(itemConsume.length > 0)
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}

		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;

		if(skill.getSoulsConsume() > getConsumedSouls())
		{
			sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SOULS);
			return;
		}

		if(skill.getEnergyConsume() > getAgathionEnergy())
		{
			sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
			return;
		}

		if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		if(skill.getEnergyConsume() > 0)
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());

		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skillEntry, this);
		long reuseDelay = Formulas.calcSkillReuseDelay(this, skillEntry);
		if(!skill.isToggle())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
		// Не показывать сообщение для хербов и кубиков
		if(!skill.isHideUseMessage())
			if(skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(new SystemMessage(SystemMsg.SUMMONING_YOUR_PET));
			else if(skill.getItemConsumeId().length == 0 || !skill.isHandler())
				sendPacket(new SystemMessage(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));

		if(!skill.isHandler())
			disableSkill(skillEntry, reuseDelay);

		ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, target, skillEntry), skill.getHitTime());
	}

	public void sendReuseMessage(SkillEntry skill)
	{
	}

	public void broadcastPacket(IBroadcastPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(List<IBroadcastPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(IBroadcastPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		List<Player> players = World.getAroundObservers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}

	public void broadcastPacketToOthers(List<IBroadcastPacket> packets)
	{
		if(!isVisible() || packets.isEmpty())
			return;

		List<Player> players = World.getAroundObservers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}

	public void broadcastToStatusListeners(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null || _statusListeners.isEmpty())
				return;

			Player player;
			for(int i = 0; i < _statusListeners.size(); i++)
			{
				player = _statusListeners.get(i);
				player.sendPacket(packets);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void addStatusListener(Player cha)
	{
		if(cha == this)
			return;

		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				_statusListeners = new LazyArrayList<Player>();
			if(!_statusListeners.contains(cha))
				_statusListeners.add(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void removeStatusListener(Creature cha)
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.remove(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void clearStatusListeners()
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.clear();
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

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
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, getPvpFlag());
					break;
			}
		return su;
	}

	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP);
		broadcastToStatusListeners(su);
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}

	public final double calcStat(Stats stat, double init, Creature target, SkillEntry skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c == null)
			return init;
		Env env = new Env();
		env.character = this;
		env.target = target;
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}

	public final double calcStat(Stats stat, Creature target, SkillEntry skill)
	{
		Env env = new Env(this, target, skill);
		env.value = stat.getInit();
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c != null)
			c.calc(env);
		return env.value;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd(true));
	}

	public void callSkill(SkillEntry skillEntry, List<Creature> targets, boolean useActionSkills)
	{
		Skill skill = skillEntry.getTemplate();
		try
		{
			if(useActionSkills && !skill.isUsingWhileCasting())
				if(skill.isOffensive())
				{
					if(skill.isMagic())
						useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skillEntry, 0);
					else
						useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skillEntry, 0);
				}
				else if(skill.isMagic()) // для АоЕ, пати/клан бафов и селфов триггер накладывается на кастера
				{
					final boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF;
					useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skillEntry, 0);
				}

			Player pl = getPlayer();
			Creature target;
			Iterator<Creature> itr = targets.iterator();
			while(itr.hasNext())
			{
				target = itr.next();

				//Фильтруем неуязвимые цели
				if(skill.isOffensive() && target.isInvul())
				{
					Player pcTarget = target.getPlayer();
					if((!skill.isIgnoreInvul() || pcTarget != null && pcTarget.isGM()) && !target.isArtefact())
					{
						itr.remove();
						continue;
					}
				}
				//Рассчитываем игрорируемые скилы из спец.эффекта
				Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
				if(ie != null)
					if(ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()))
					{
						itr.remove();
						continue;
					}

				target.getListeners().onMagicHit(skillEntry, this);

				if(pl != null)
					if(target != null && target.isNpc())
					{
						NpcInstance npc = (NpcInstance) target;
						List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
						if(ql != null)
							for(QuestState qs : ql)
								qs.getQuest().notifySkillUse(npc, skill, qs);
					}

				if(skill.getNegateSkill() > 0)
					for(Effect e : target.getEffectList().getAllEffects())
					{
						SkillEntry efs = e.getSkill();
						if(efs.getId() == skill.getNegateSkill() && e.isCancelable() && (skill.getNegatePower() <= 0 || efs.getTemplate().getPower() <= skill.getNegatePower()))
							e.exit();
					}

				if(skill.getCancelTarget() > 0)
					if(Rnd.chance(skill.getCancelTarget()))
						if((target.getCastingSkill() == null || !(target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE || target.getCastingSkill().getSkillType() == SkillType.TAKEFORTRESS || target.getCastingSkill().getSkillType() == SkillType.TAKEFLAG)) && !target.isRaid())
						{
							target.abortAttack(true, true);
							target.abortCast(true, true);
							target.setTarget(null);
						}
			}

			if(skill.isOffensive())
				startAttackStanceTask();

			// Применяем селфэффекты на кастера
			// Особое условие для атакующих аура-скиллов (Vengeance 368):
			// если ни одна цель не задета то селфэффекты не накладываются
			if (!(skill.isNotTargetAoE() && skill.isOffensive()&& targets.size() == 0))
				skillEntry.getEffects(this, this, false, true);

			skill.useSkill(skillEntry, this, targets);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public void useTriggers(GameObject target, TriggerType type, SkillEntry ex, SkillEntry owner, double damage)
	{
		Set<TriggerInfo> SkillsOnSkillAttack = _triggers.get(type);
		if(SkillsOnSkillAttack != null)
			for(TriggerInfo t : SkillsOnSkillAttack)
				if(t.getSkill() != ex)
					useTriggerSkill(target == null ? getTarget() : target, t, owner, damage);
	}

	private void useTriggerSkill(GameObject target, TriggerInfo trigger, SkillEntry owner, double damage)
	{
		SkillEntry skillEntry = trigger.getSkill();
		Skill skill = skillEntry.getTemplate();
		if(skill.getReuseDelay() > 0 && isSkillDisabled(skillEntry))
			return;

		Creature aimTarget = skill.getAimingTarget(this, target);
		final int castRange = skill.getCastRange();
		if (aimTarget != this && castRange > 0 && castRange != 32767 && getRealDistance3D(aimTarget) > castRange)
			return;
		// DS: Для шансовых скиллов с TARGET_SELF и условием "пвп" сам кастер будет являться aimTarget,
		// поэтому в условиях для триггера проверяем реальную цель.
		Creature realTarget = target != null && target.isCreature() ? (Creature)target : null;
		if(Rnd.chance(trigger.getChance()) && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skillEntry.checkCondition(this, aimTarget, false, true, true))
		{
			int displayId = 0, displayLevel = 0;

			if(skill.hasEffects())
			{
				displayId = skill.getEffectTemplates()[0]._displayId;
				displayLevel = skill.getEffectTemplates()[0]._displayLevel;
			}

			if(displayId == 0)
				displayId = skill.getDisplayId();
			if(displayLevel == 0)
				displayLevel = skill.getDisplayLevel();

			disableSkill(skillEntry, skill.getReuseDelay());

			if(trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE)
			{
				List<Creature> targets = skill.getTargets(this, aimTarget, false);
				for(Creature cha : targets)
					broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0));

				ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, aimTarget, skillEntry), skill.getHitTime());
			}
			else
				ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, aimTarget, skillEntry), 25L);
		}
	}

	public boolean checkBlockedStat(Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}

	public void doCounterAttack(SkillEntry skillEntry, Creature attacker, boolean blow)
	{
		if (skillEntry == null)
			return;
		if(isDead()) // если персонаж уже мертв, контратаки быть не должно
			return;
		if(isDamageBlocked() || attacker.isDamageBlocked()) // Не контратакуем, если есть неуязвимость, иначе она может отмениться
			return;
		final Skill skill = skillEntry.getTemplate();
		final int numberOfAttacks = skill.getNumberOfCounterAttacks();
		if(numberOfAttacks <= 0)
			return;
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0, attacker, skillEntry)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessage(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessage(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			if (numberOfAttacks > 1)
				for (int i = 0 ; i < skill.getNumberOfCounterAttacks(); i++)
					attacker.reduceCurrentHp(damage, this, skillEntry, 0, false, true, true, false, false, false, false, true);
			else
				attacker.reduceCurrentHp(damage, this, skillEntry, 0, false, true, true, false, false, false, false, true);
		}
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(SkillEntry skill, long delay)
	{
		if (delay > 0)
			_skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
	}

	public abstract boolean isAutoAttackable(Creature attacker);

	public void doAttack(Creature target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isDead() || !isInRange(target, 2000))
			return;

		getListeners().onAttack(target);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = Math.max(calculateAttackDelay(), 333);
		int ssGrade = 0;

		WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem != null)
		{
			if(isPlayer() && weaponItem.getAttackReuseDelay() > 0)
			{
				int reuse = (int) (weaponItem.getAttackReuseDelay() * getReuseModifier(target) * 666 * calcStat(Stats.ATK_BASE, 0, target, null) / 293. / getPAtkSpd(false));
				if(reuse > 0)
				{
					sendPacket(new SetupGauge(this, SetupGauge.RED, reuse));
					_attackReuseEndTime = reuse + System.currentTimeMillis() - 75;
					if(reuse > sAtk)
						ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse);
				}
			}

			ssGrade = weaponItem.getCrystalType().externalOrdinal;
		}

		// DS: скорректировано на 1/100 секунды поскольку AI task вызывается с небольшой погрешностью
		// особенно на слабых машинах и происходит обрыв автоатаки по isAttackingNow() == true
		_attackEndTime = sAtk + System.currentTimeMillis() - Config.ATTACK_END_DELAY;
		_isAttackAborted = false;

		Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);

		setHeading(PositionUtils.calculateHeadingFrom(this, target));

		// Select the type of attack to
		if(weaponItem == null)
			doAttackHitSimple(attack, target, 1., 0, !isPlayer(), sAtk, true);
		else
			switch(weaponItem.getItemType())
			{
				case BOW:
				case CROSSBOW:
					doAttackHitByBow(attack, target, sAtk);
					break;
				case POLE:
					doAttackHitByPole(attack, target, sAtk);
					break;
				case DUAL:
				case DUALFIST:
				case DUALDAGGER:
					doAttackHitByDual(attack, target, sAtk);
					break;
				default:
					doAttackHitSimple(attack, target, 1., 0, true, sAtk, true);
			}

		if(attack.hasHits())
			broadcastPacket(attack);
	}

	private boolean doAttackHitSimple(Attack attack, Creature target, double multiplier, int poleHitCount, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, poleHitCount, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify), sAtk);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		return !miss1;
	}

	private void doAttackHitByBow(Attack attack, Creature target, int sAtk)
	{
		WeaponTemplate activeWeapon = getActiveWeaponItem();
		if(activeWeapon == null)
			return;

		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);


                if(!Config.NOT_DESTROY_ARROWS)
                    reduceArrowCount();
                
		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;

			int range = activeWeapon.getAttackRange();
			damage1 *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // разброс 20% в обе стороны
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, 0, crit1, miss1, attack._soulshot, shld1, true, true), sAtk);

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}

		if(!miss2)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, 0, crit1, miss1, attack._soulshot, shld1, true, false), sAtk / 2);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, 0, crit2, miss2, attack._soulshot, shld2, false, true), sAtk);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(Attack attack, Creature target, int sAtk)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 90, target, null);
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);

		// Используем Math.round т.к. обычный кастинг обрезает к меньшему
		// double d = 2.95. int i = (int)d, выйдет что i = 2
		// если 1% угла или 1 дистанции не играет огромной роли, то для
		// количества целей это критично
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0, target, null));

		if(isBoss())
			attackcountmax += 27;
		else if(isRaid())
			attackcountmax += 12;
		else if(isMonster() && getLevel() > 0)
			attackcountmax += getLevel() / 7.5;

		double mult = 1.;
		int poleHitCount = 0;
		int poleAttackCount = 0;

		if (doAttackHitSimple(attack, target, 1., poleHitCount, true, sAtk, true))
			poleHitCount++;

		if(!isInZonePeace())// Гварды с пикой, будут атаковать только одиночные цели в городе
			for(Creature t : getAroundCharacters(range, 200))
				if(poleAttackCount < attackcountmax)
				{
					if(t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle))
						continue;

					// Не флагаемся если рядом стоит флагнутый и его может задеть
					if(t.isPlayable() ? ((Playable)t).isAttackable(this, false, true) : t.isAutoAttackable(this))
					{
						if (doAttackHitSimple(attack, t, mult, poleHitCount, false, sAtk, false))
							poleHitCount++;
						mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
						poleAttackCount++;
					}
				}
				else
					break;
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public void doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		int itemConsume[] = skill.getItemConsume();

		if(itemConsume.length > 0)
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}

		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;

		int magicId = skill.getId();

		if(target == null)
			target = skill.getAimingTarget(this, getTarget());
		if(target == null)
			return;

		getListeners().onMagicUse(skillEntry, target, false);

		if(this != target)
			setHeading(PositionUtils.calculateHeadingFrom(this, target));

		int level = Math.max(1, getSkillDisplayLevel(magicId));

		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;

		if(skillTime < Config.SKILLS_CAST_TIME_MIN && !skill.isFishingSkill())
		{
			skillTime = Config.SKILLS_CAST_TIME_MIN;
			skillInterruptTime = 0;
		}

		_animationEndTime = System.currentTimeMillis() + skillTime;

		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot(true) > 0)
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}

		Formulas.calcSkillMastery(skillEntry, this); // Calculate skill mastery for current cast
		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(this, skillEntry));

		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));

		if(!skill.isHandler())
			disableSkill(skillEntry, reuseDelay);

		if(isPlayer())
			if(skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			else if(skill.getItemConsumeId().length == 0 || !skill.isHandler())
				sendPacket(new SystemMessage(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));

		if(skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);

		double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
		if(mpConsume1 > 0)
		{
			if(_currentMp < mpConsume1)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				onCastEndTime(skillEntry);
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}

		if(skill.getNumCharges() > 0)
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());

		_flyLoc = null;
		switch (skill.getFlyType())
		{
			case DUMMY:
				if(getFlyLocation(target, skill) == null)
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
				break;
			case CHARGE:
				_flyLoc = getFlyLocation(target, skill);
				if(_flyLoc != null)
					broadcastPacket(new FlyToLocation(this, _flyLoc, skill.getFlyType()));
				else
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
		}

		_castingSkill = skillEntry;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		setCastingTarget(target);

		if(skill.isUsingWhileCasting())
			callSkill(skillEntry, skill.getTargets(this, target, forceUse), true);

		if(isPlayer())
			sendPacket(new SetupGauge(this, SetupGauge.BLUE, skillTime));

		_scheduledCastCount = skill.getCastCount();
		_scheduledCastInterval = _scheduledCastCount > 0 ? skillTime / _scheduledCastCount : skillTime;

		// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), skillInterruptTime);
		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), _scheduledCastInterval);

		_skillGeoCheckTask = null;
		if(skill.getCastRange() < 32767 && skill.getSkillType() != SkillType.TAKECASTLE && skill.getSkillType() != SkillType.TAKEFORTRESS && _scheduledCastInterval > 600)
			_skillGeoCheckTask = ThreadPoolManager.getInstance().schedule(new MagicGeoCheckTask(this), (long)(_scheduledCastInterval * 0.5));
	}

	private Location _flyLoc;

	public Location getFlyLocation(GameObject target, Skill skill)
	{
		if(target != null && target != this)
		{
			Location loc;

			double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
			if(skill.isFlyToBack())
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40), target.getY() - (int) (Math.cos(radian) * 40), target.getZ());
			else
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40), target.getY() + (int) (Math.cos(radian) * 40), target.getZ());

			if(isFlying())
			{
				if(isPlayer() && ((Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
					return null;
				if(GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ();

				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}

			return loc;
		}

		double radian = PositionUtils.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());

		if(isFlying())
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}

	public final void doDie(Creature killer)
	{
		// killing is only possible one time
		if(!isDead.compareAndSet(false, true))
			return;

		onDeath(killer);
	}

	protected void onDeath(Creature killer)
	{
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null)
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);

			killer.getListeners().onKill(this);

			if(isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}

		setTarget(null);
		stopMove();
		stopRegeneration();

		_currentHp = 0;

		// Stop all active skills effects in progress on the L2Character
		if(isBlessedByNoblesse() || isSalvation())
		{
			if(isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode())
				getPlayer().reviveRequest(getPlayer(), 100, false);
			for(Effect e : getEffectList().getAllEffects())
				// Noblesse Blessing Buff/debuff effects are retained after
				// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
				if(e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == Skill.SKILL_FORTUNE_OF_NOBLESSE || e.getSkill().getId() == Skill.SKILL_RAID_BLESSING)
					e.exit();
				else if(e.getEffectType() == EffectType.AgathionResurrect)
				{
					if(isPlayer())
						getPlayer().setAgathionRes(true);
					e.exit();
				}
		}
		else
			for(Effect e : getEffectList().getAllEffects())
				// Некоторые эффекты сохраняются при смерти
				if(!e.getSkill().getTemplate().isPreservedOnDeath())
					e.exit();

		getAI().notifyEvent(CtrlEvent.EVT_DEAD, killer);

		getListeners().onDeath(killer);

		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}

	protected void onRevive()
	{

	}

	public void enableSkill(SkillEntry skill)
	{
		_skillReuses.remove(skill.hashCode());
	}

	public int getAbnormalEffect(AbnormalEffectType t)
	{
		return _abnormalEffects[t.ordinal()];
	}

	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
	}

	/**
	 * Возвращает коллекцию скиллов для быстрого перебора
	 */
	public Collection<SkillEntry> getAllSkills()
	{
		return _skills.values();
	}

	/**
	 * Возвращает массив скиллов для безопасного перебора
	 */
	public final SkillEntry[] getAllSkillsArray()
	{
		Collection<SkillEntry> vals = _skills.values();
		return vals.toArray(new SkillEntry[vals.size()]);
	}

	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd(true) / getTemplate().basePAtkSpd;
	}

	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}

	public SkillEntry getCastingSkill()
	{
		return _castingSkill;
	}

	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, _template.baseCON, null, null);
	}

	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 */
	public int getCriticalHit(Creature target, SkillEntry skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _template.baseCritRate, target, skill);
	}

	/**
	 * Возвращает шанс магического крита в процентах
	 */
	public double getMagicCriticalRate(Creature target, SkillEntry skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}

	/**
	 * Return the current CP of the L2Character.
	 *
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}

	public Location getDestination()
	{
		return destination;
	}

	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, _template.baseDEX, null, null);
	}

	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0, target, null);
	}

	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, _template.baseINT, null, null);
	}

	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final SkillEntry getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}

	public final int getMagicalAttackRange(SkillEntry skill)
	{
		if(skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getTemplate().getCastRange(), null, skill);
		return getTemplate().baseAtkRange;
	}

	public int getMAtk(Creature target, SkillEntry skill)
	{
		if(skill != null && skill.getTemplate().getMatak() > 0)
			return skill.getTemplate().getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
	}

	public int getMAtkSpd()
	{
		return (int) (calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null));
	}

	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
	}

	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
	}

	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
	}

	public int getMDef(Creature target, SkillEntry skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, target, skill), 1);
	}

	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, _template.baseMEN, null, null);
	}

	public double getMinDistance(GameObject obj)
	{
		double distance = getTemplate().collisionRadius;

		if(obj != null && obj.isCreature())
			distance += ((Creature) obj).getTemplate().collisionRadius;

		return distance;
	}

	public double getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1. / _template.baseRunSpd;
	}

	@Override
	public int getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();

		return getWalkSpeed();
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}

	public int getPAtkSpd(boolean applyLimit)
	{
		final int result = (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null);
		return applyLimit && result > Config.LIM_PATK_SPD ? Config.LIM_PATK_SPD : result;
	}

	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}

	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}

	public final int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}

	public int getRunSpeed()
	{
		return getSpeed(_template.baseRunSpd);
	}

	public final int getShldDef()
	{
		if(isPlayer())
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.baseShldDef, null, null);
	}

	public final int getSkillDisplayLevel(int skillId)
	{
		SkillEntry skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getDisplayLevel();
	}

	public final int getSkillLevel(int skillId)
	{
		return getSkillLevel(skillId, -1);
	}

	public final int getSkillLevel(int skillId, int def)
	{
		SkillEntry skill = _skills.get(skillId);
		if(skill == null)
			return def;
		return skill.getLevel();
	}

	public SkillMastery getSkillMastery(Skill skill)
	{
		return skill.getId() == _skillMasteryId ? skill.getDefaultSkillMastery() : SkillMastery.NONE;
	}

	public void removeSkillMastery(SkillEntry skill)
	{
		if(skill.getId() == _skillMasteryId)
			_skillMasteryId = 0;
	}

	public int getSpeed(int baseSpeed)
	{
		if(isInWater())
			return getSwimSpeed();
		return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
	}

	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, _template.baseSTR, null, null);
	}

	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}

	public GameObject getTarget()
	{
		return target.get();
	}

	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public CharTemplate getTemplate()
	{
		return _template;
	}

	public CharTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}

	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}

	public final int getWalkSpeed()
	{
		if(isInWater())
			return getSwimSpeed();
		return getSpeed(_template.baseWalkSpd);
	}

	public int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, _template.baseWIT, null, null);
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}

	public boolean isAlikeDead()
	{
		return isDead();
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}

	public final boolean isSalvation()
	{
		return _isSalvation;
	}

	public boolean isEffectImmune()
	{
		return _effectImmunity.get();
	}

	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}

	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get();
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	@Override
	public final boolean isFlying()
	{
		return _flying;
	}

	/**
	 * Находится ли персонаж в боевой позе
	 * @return true, если персонаж в боевой позе, атакован или атакует
	 */
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}

	public SpecialEffectState getInvulnerable()
	{
		return _invulState;
	}

	public boolean isInvul()
	{
		return getInvulnerable() != SpecialEffectState.FALSE;
	}

	public SpecialEffectState getInvisible()
	{
		return SpecialEffectState.FALSE;
	}

	public final boolean isInvisible()
	{
		return getInvisible() != SpecialEffectState.FALSE;
	}

	public boolean isMageClass()
	{
		return getTemplate().baseMAtk > 3;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	public boolean isSkillDisabled(SkillEntry skill)
	{
		TimeStamp sts = _skillReuses.get(skill.hashCode());
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.hashCode());
		return false;
	}

	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}

	/**
	 * Возвращает позицию цели, в которой она будет через пол секунды.
	 */
	public Location getIntersectionPoint(Creature target)
	{
		if(!PositionUtils.isFacing(this, target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		double angle = PositionUtils.convertHeadingToDegree(target.getHeading()); // угол в градусах
		double radian = Math.toRadians(angle - 90); // угол в радианах
		double range = target.getMoveSpeed() / 2; // расстояние, пройденное за 1 секунду, равно скорости. Берем половину.
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}

	public Location applyOffset(Location point, int offset)
	{
		if(offset <= 0)
			return point;

		long dx = point.x - getX();
		long dy = point.y - getY();
		long dz = point.z - getZ();

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if(distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			point.x -= (int) (dx * cut + 0.5);
			point.y -= (int) (dy * cut + 0.5);
			point.z -= (int) (dz * cut + 0.5);

			if(!isFlying() && !isInBoat() && !isInWater() && !isBoat())
				point.correctGeoZ();
		}

		return point;
	}

	public List<Location> applyOffset(List<Location> points, int offset)
	{
		offset = offset >> 4;
		if(offset <= 0)
			return points;

		long dx = points.get(points.size() - 1).x - points.get(0).x;
		long dy = points.get(points.size() - 1).y - points.get(0).y;
		long dz = points.get(points.size() - 1).z - points.get(0).z;

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(distance <= offset)
		{
			Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			int num = (int) (points.size() * cut + 0.5);
			for(int i = 1; i <= num && points.size() > 0; i++)
				points.remove(points.size() - 1);
		}

		return points;
	}

	private boolean setSimplePath(Location dest)
	{
		List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if(moveList.isEmpty())
			return false;
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}

	private boolean buildPathTo(int x, int y, int z, int offset, boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}

	private boolean buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
	{
		int geoIndex = getGeoIndex();

		Location dest;

		if(forestalling && follow != null && follow.isMoving)
			dest = getIntersectionPoint(follow);
		else
			dest = new Location(x, y, z);

		if(isInBoat() || isBoat() || !Config.ALLOW_GEODATA)
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}

		if(isFlying() || isInWater())
		{
			applyOffset(dest, offset);

			Location nextloc;

			if(isFlying())
			{
				if(GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true))
					return setSimplePath(dest);

				// DS: При передвижении обсервера клавишами клиент шлет очень далекие (дистанция больше 2000) координаты,
				// поэтому обычная процедура проверки не работает. Используем имитацию плавания в воде.
				if (isObservePoint())
					nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, 15000, geoIndex);
				else
					nextloc = GeoEngine.moveCheckInAir(getX(), getY(), getZ(), dest.x, dest.y, dest.z, getColRadius(), geoIndex);
				if(nextloc != null && !nextloc.equals(getX(), getY(), getZ()))
					return setSimplePath(nextloc);
			}
			else
			{
				int waterZ = getWaterZ();
				nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, waterZ, geoIndex);
				if(nextloc == null)
					return false;

				List<Location> moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if(!moveList.isEmpty())
					_targetRecorder.add(moveList);

				int dz = dest.z - nextloc.z;
				// если пытаемся выбратся на берег, считаем путь с точки выхода до точки назначения
				if(dz > 0 && dz < 128)
				{
					moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					if(moveList != null) // null - до конца пути дойти нельзя
					{
						if(!moveList.isEmpty()) // уже стоим на нужной клетке
							_targetRecorder.add(moveList);
					}
				}

				return !_targetRecorder.isEmpty();
			}
			return false;
		}

		List<Location> moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, true); // onlyFullPath = true - проверяем весь путь до конца
		if(moveList != null) // null - до конца пути дойти нельзя
		{
			if(moveList.isEmpty()) // уже стоим на нужной клетке
				return false;
			applyOffset(moveList, offset);
			if(moveList.isEmpty()) // уже стоим на нужной клетке
				return false;
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}

		if(pathFind)
		{
			List<List<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.getX(), dest.getY(), dest.getZ(), this, geoIndex);
			if(!targets.isEmpty())
			{
				moveList = targets.remove(targets.size() - 1);
				applyOffset(moveList, offset);
				if(!moveList.isEmpty())
					targets.add(moveList);
				if(!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					return true;
				}
			}
		}

		if(isPlayable()) // расчитываем путь куда сможем дойти, только для игровых персонажей
		{
			applyOffset(dest, offset);

			moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, false); // onlyFullPath = false - идем до куда можем
			if(moveList != null && !moveList.isEmpty()) // null - нет геодаты, empty - уже стоим на нужной клетке
			{
				_targetRecorder.clear();
				_targetRecorder.add(moveList);
				return true;
			}
		}

		return false;
	}

	public Creature getFollowTarget()
	{
		return followTarget.get();
	}

	public void setFollowTarget(Creature target)
	{
		followTarget = target == null ? HardReferences.<Creature> emptyRef() : target.getRef();
	}

	public boolean followToCharacter(Creature target, int offset, boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}

	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		moveLock.lock();
		try
		{
			if(isMovementDisabled() || target == null || isInBoat())
				return false;

			offset = Math.max(offset, 10);
			if(isFollow && target == getFollowTarget() && offset == _offset)
				return true;

			if(Math.abs(getZ() - target.getZ()) > 1000 && !isFlying())
			{
				sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				return false;
			}

			getAI().clearNextAction();

			stopMove(false, false);

			if(buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor()))
				movingDestTempPos.set(loc.x, loc.y, loc.z);
			else
				return false;

			isMoving = true;
			isFollow = true;
			_forestalling = forestalling;
			_offset = offset;
			setFollowTarget(target);

			moveNext(true);

			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if(isMoving && !isFollow && movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}

			if(isMovementDisabled())
			{
				getAI().setNextAction(nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding, false);
				sendActionFailed();
				return false;
			}

			getAI().clearNextAction();

			if(isPlayer())
				getAI().changeIntention(AI_INTENTION_ACTIVE, null, null);

			stopMove(false, false);

			if(buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding))
				movingDestTempPos.set(dst_geoloc);
			else
			{
				sendActionFailed();
				return false;
			}

			isMoving = true;

			moveNext(true);

			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	private void moveNext(boolean firstMove)
	{
		if(!isMoving || isMovementDisabled())
		{
			stopMove();
			return;
		}

		_previousSpeed = getMoveSpeed();
		if(_previousSpeed <= 0)
		{
			stopMove();
			return;
		}

		if(!firstMove)
		{
			Location dest = destination;
			if(dest != null)
				setLoc(dest, true);
		}

		if(_targetRecorder.isEmpty())
		{
			CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			stopMove(false, true);
			ThreadPoolManager.getInstance().execute(new NotifyAITask(this, ctrlEvent));
			return;
		}

		moveList = _targetRecorder.remove(0);
		Location begin = moveList.get(0).clone().geo2world();
		Location end = moveList.get(moveList.size() - 1).clone().geo2world();
		destination = end;
		double distance = (isFlying() || isInWater()) ? begin.distance3D(end) : begin.distance(end); //клиент при передвижении не учитывает поверхность

		if(distance != 0)
			setHeading(PositionUtils.calculateHeadingFrom(getX(), getY(), destination.x , destination.y));

		broadcastMove();

		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		if(_moveTaskRunnable == null)
			_moveTaskRunnable = new MoveNextTask();
		_moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
	}

	public int getMoveTickInterval()
	{
		return (isPlayer() ? 16000 : 32000) / Math.max(getMoveSpeed(), 1);
	}

	protected void broadcastMove()
	{
		validateLocation(isPlayer() ? 2 : 1);
		broadcastPacket(movePacket());
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	/**
	 * Останавливает движение и рассылает StopMove, ValidateLocation
	 */
	public void stopMove()
	{
		stopMove(true, true);
	}

	/**
	 * Останавливает движение и рассылает StopMove
	 * @param validate - рассылать ли ValidateLocation
	 */
	public void stopMove(boolean validate)
	{
		stopMove(true, validate);
	}

	/**
	 * Останавливает движение
	 *
	 * @param stop - рассылать ли StopMove
	 * @param validate - рассылать ли ValidateLocation
	 */
	public void stopMove(boolean stop, boolean validate)
	{
		if(!isMoving)
			return;

		moveLock.lock();
		try
		{
			if(!isMoving)
				return;

			isMoving = false;
			isFollow = false;

			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			destination = null;
			moveList = null;

			_targetRecorder.clear();

			if(validate)
				validateLocation(isPlayer() ? 2 : 1);
			if(stop)
				broadcastStopMove();
		}
		finally
		{
			moveLock.unlock();
		}
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int getWaterZ()
	{
		if(!isInWater())
			return Integer.MIN_VALUE;

		int waterZ = Integer.MIN_VALUE;
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == ZoneType.water)
					if(waterZ == Integer.MIN_VALUE || waterZ < zone.getTerritory().getZmax())
						waterZ = zone.getTerritory().getZmax();
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return waterZ;
	}

	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}

	public L2GameServerPacket movePacket()
	{
		return new CharMoveToLocation(this);
	}

	public void updateZones()
	{
		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

		LazyArrayList<Zone> entering = null;
		LazyArrayList<Zone> leaving = null;

		Zone zone;

		zonesWrite.lock();
		try
		{
			if(!_zones.isEmpty())
			{
				leaving = LazyArrayList.newInstance();
				for(int i = 0; i < _zones.size(); i++)
				{
					zone = _zones.get(i);
					// зоны больше нет в регионе, либо вышли за территорию зоны
					if(!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}

				//Покинули зоны, убираем из списка зон персонажа
				if(!leaving.isEmpty())
				{
					for(int i = 0; i < leaving.size(); i++)
					{
						zone = leaving.get(i);
						_zones.remove(zone);
					}
				}
			}

			if(zones.length > 0)
			{
				entering = LazyArrayList.newInstance();
				for(int i = 0; i < zones.length; i++)
				{
					zone = zones[i];
					// в зону еще не заходили и зашли на территорию зоны
					if(!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}

				//Вошли в зоны, добавим в список зон персонажа
				if(!entering.isEmpty())
				{
					for(int i = 0; i < entering.size(); i++)
					{
						zone = entering.get(i);
						_zones.add(zone);
					}
				}
			}
		}
		finally
		{
			zonesWrite.unlock();
		}

		onUpdateZones(leaving, entering);

		if(leaving != null)
			LazyArrayList.recycle(leaving);

		if(entering != null)
			LazyArrayList.recycle(entering);

	}

	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		Zone zone;

		if(leaving != null && !leaving.isEmpty())
		{
			for(int i = 0; i < leaving.size(); i++)
			{
				zone = leaving.get(i);
				zone.doLeave(this);
			}
		}

		if(entering != null && !entering.isEmpty())
		{
			for(int i = 0; i < entering.size(); i++)
			{
				zone = entering.get(i);
				zone.doEnter(this);
			}
		}
	}

	public boolean isInZonePeace()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isInZoneBattle()
	{
		return isInZone(ZoneType.battle_zone);
	}

	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}

	public boolean isInZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public List<Event> getZoneEvents()
	{
		List<Event> e = Collections.emptyList();
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(!zone.getEvents().isEmpty())
				{
					if(e.isEmpty())
						e = new ArrayList<Event>(2);

					e.addAll(zone.getEvents());
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return e;
	}

	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getName().equals(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public boolean isInZone(Zone zone)
	{
		zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			zonesRead.unlock();
		}
	}

	public Zone getZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return zone;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	public Location getPKRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getPKSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return loc.z;

		return super.getGeoZ(loc);
	}

	protected boolean needStatusUpdate()
	{
		if(!isVisible())
			return false;

		boolean result = false;

		int bar;
		bar = (int) (getCurrentHp() * CLIENT_BAR_SIZE / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}

		bar = (int) (getCurrentMp() * CLIENT_BAR_SIZE / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}

		if(isPlayer())
		{
			bar = (int) (getCurrentCp() * CLIENT_BAR_SIZE / getMaxCp());
			if(bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}

		return result;
	}

	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

		if(!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}

		player.getAI().Attack(this, true, shift);
	}

	public void onHitTimer(Creature target, int damage, int poleHitCount, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}

		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}

		target.getListeners().onAttackHit(this);

		// if hitted by a cursed weapon, Cp is reduced to 0, if a cursed weapon is hitted by a Hero, Cp is reduced to 0
		if(!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
			target.setCurrentCp(0);

		Formulas.calcStunBreak(target, crit);

		displayGiveDamageMessage(target, damage, crit, miss, shld, false);

		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));

		boolean checkPvP = checkPvP(target, null);
		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		if(!miss && damage > 0)
		{
			target.reduceCurrentHp(damage, this, null, poleHitCount, crit, true, true, false, true, false, false, true);

			// Скиллы, кастуемые при физ атаке
			if(!target.isDead())
			{
				if(crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);

				useTriggers(target, TriggerType.ATTACK, null, null, damage);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}

			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);

		startAttackStanceTask();

		if(checkPvP)
			startPvPFlag(target);
	}

	public void onMagicUseTimer(Creature aimingTarget, SkillEntry skillEntry, boolean forceUse)
	{
		_castInterruptTime = 0;

		Skill skill = skillEntry.getTemplate();
		if(skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime(skillEntry);
			return;
		}

		if(!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;

		if(!skillEntry.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if(skill.getSkillType() == SkillType.PET_SUMMON && isPlayer())
				getPlayer().setPetControlItem(null);
			onCastEndTime(skillEntry);
			return;
		}

		// TODO: DS: прикинуть точное время проверки видимости цели.
		//if(skill.getCastRange() < 32767 && skill.getSkillType() != SkillType.TAKECASTLE && skill.getSkillType() != SkillType.TAKEFORTRESS && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		if (_skillGeoCheckTask != null && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		{
			sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			broadcastPacket(new MagicSkillCanceled(this));
			onCastEndTime(skillEntry);
			return;
		}

		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);

		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			setCurrentHp(Math.max(0, _currentHp - hpConsume), false);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(skill.isMusic())
			{
				mpConsume2 += getEffectList().getActiveMusicCount(skill.getId()) * mpConsume2 / 2;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skillEntry);
			}
			else if(skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skillEntry);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skillEntry);

			if(_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				onCastEndTime(skillEntry);
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}

		callSkill(skillEntry, targets, true);

		if(skill.isSoulBoost())
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		else if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);

		Location flyLoc;
		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
				for(Creature target : targets)
				{
					//target.setHeading(this, false); //TODO [VISTALL] set heading of target ? Oo
					flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
				}
				break;
			case DUMMY:
				if (aimingTarget != null)
				{
					_flyLoc = getFlyLocation(aimingTarget, skill);
					if (_flyLoc != null)
						broadcastPacket(new FlyToLocation(this, _flyLoc, skill.getFlyType()));
				}
				break;
		}

		if(_scheduledCastCount > 1)
		{
			_scheduledCastCount--;
			_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), _scheduledCastInterval);
			_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), _scheduledCastInterval);
			return;
		}

		final SkillMastery sm = getSkillMastery(skill);
		if (sm.hasDoubleTime())
			sendPacket(SystemMsg.A_SKILL_IS_READY_TO_BE_USED_AGAIN_BUT_ITS_REUSE_COUNTER_TIME_HAS_INCREASED);
		else if (sm.hasZeroReuse())
			sendPacket(SystemMsg.A_SKILL_IS_READY_TO_BE_USED_AGAIN);

		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this, skillEntry), skillCoolTime);
		else if (skill.hasEffects())
			ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this, skillEntry), 20);
		else
			onCastEndTime(skillEntry);
	}

	public void onCastEndTime(SkillEntry se)
	{
		finishFly(false);
		removeSkillMastery(se);
		clearCastVars();
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, se, null);
	}

	public void clearCastVars()
	{
		_animationEndTime = 0;
		_castInterruptTime = 0;
		_scheduledCastCount = 0;
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_skillGeoCheckTask = null;
		_flyLoc = null;
	}

	private void finishFly(boolean aborted)
	{
		SkillEntry skill = _castingSkill;
		Location flyLoc = _flyLoc;
		_flyLoc = null;
		if(flyLoc != null)
		{
			// TODO: DS: уточнить пакет при обрыве каста
			if (aborted && skill != null && skill.getTemplate().getFlyType() != FlyType.CHARGE)
				broadcastPacket(new FlyToLocation(this, getLoc(), FlyType.NONE));
			else
				setLoc(flyLoc);
			validateLocation(1);
		}
	}

	public void reduceCurrentHp(double damage, Creature attacker, SkillEntry skill, int poleHitCount, boolean crit, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		if(isDamageBlocked() && transferDamage)
			return;

		if(isDamageBlocked() && attacker != this)
		{
			if (sendMessage)
				attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		double transferedToEffectorDamage = 0;
		double transferedDamage = 0;
		double reflectedDamage = 0;
		if(canReflect)
		{
			reflectedDamage = attacker.absorbAndReflect(this, skill, damage, poleHitCount);
			if (reflectedDamage < 0) // all damage was reflected
				return;

			transferedToEffectorDamage = absorbToEffector(attacker, damage);
			if (transferedToEffectorDamage > 0)
				damage -= transferedToEffectorDamage;
			else
				transferedToEffectorDamage = -transferedToEffectorDamage;

			transferedDamage = absorbToSummon(attacker, damage); // Minus if not enough HP
			if (transferedDamage > 0)
				damage -= transferedDamage;
			else
				transferedDamage = -transferedDamage;

			transferedDamage += transferedToEffectorDamage;
		}

		if (attacker != this || transferDamage)
		{
			if (sendMessage && (damage > 0 || transferedDamage > 0))
				displayReceiveDamageMessage(attacker, (int) damage, (int)transferedDamage, (int)reflectedDamage, transferDamage);

			if(!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}

		if (canReflect)
			damage = absorbToMp(attacker, damage);

		getListeners().onCurrentHpDamage(damage, attacker, skill, crit);

		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	protected void onReduceCurrentHp(final double damage, Creature attacker, SkillEntry skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(awake && isSleeping())
			getEffectList().stopEffects(EffectType.Sleep);

		boolean isUndying = isUndying(attacker);

		if(attacker != this || (skill != null && skill.getTemplate().isOffensive()))
		{
			if(isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}

			startAttackStanceTask();
			checkAndRemoveInvisible();

			if(getCurrentHp() - damage < 0.5 && !isUndying)
				useTriggers(attacker, TriggerType.DIE, null, null, damage);
		}

		// undying mode
		setCurrentHp(Math.max(getCurrentHp() - damage, isUndying ? 0.5 : 0), false);

		if(isUndying)
		{
			if (getCurrentHp() == 0.5 && _undyingState != SpecialEffectState.GM)
				if (_undyingFlag.compareAndSet(false, true))
					getListeners().onDeathFromUndying(attacker);
		}
		else if(getCurrentHp() < 0.5)
			doDie(attacker);
	}

	public void reduceCurrentMp(double i, Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getEffectList().stopEffects(EffectType.Sleep);

			if(isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}
		}

		if(isDamageBlocked() && attacker != null && attacker != this)
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}

		i = _currentMp - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);

		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public double relativeSpeed(GameObject target)
	{
		return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}

	public void removeAllSkills()
	{
		for(SkillEntry s : getAllSkillsArray())
			removeSkill(s);
	}

	public void removeBlockStats(List<Stats> stats)
	{
		if(_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if(_blockedStats.isEmpty())
				_blockedStats = null;
		}
	}

	public SkillEntry removeSkill(SkillEntry skill)
	{
		if(skill == null)
			return null;
		return removeSkillById(skill.getId());
	}

	public SkillEntry removeSkillById(int id)
	{
		SkillEntry oldSkill = _skills.remove(id);

		if(oldSkill != null)
		{
			removeTriggers(oldSkill.getTemplate());
			removeStatsByOwner(oldSkill);
		}

		return oldSkill;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}

	public void addTrigger(TriggerInfo t)
	{
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo> ();
			_triggers.put(t.getType(), hs);
		}

		hs.add(t);

		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, t, null, 0);
	}

	public void removeTriggers(StatTemplate f)
	{
		if( f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}

	public void removeTrigger(TriggerInfo t)
	{
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
			return;
		hs.remove(t);
	}

	public void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC);
	}

	public boolean hasAI()
	{
		return _ai != null;
	}

	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new CharacterAI(this);
			}

		return _ai;
	}

	public void setAI(CharacterAI newAI)
	{
		if(newAI == null)
			return;

		CharacterAI oldAI = _ai;

		synchronized (this)
		{
			_ai = newAI;
		}

		if(oldAI != null)
		{
			if(oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}

	public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
	{
		int maxHp = getMaxHp();

		newHp = Math.min(maxHp, Math.max(0, newHp));

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;

		_currentHp = newHp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentHp < maxHp)
			startRegeneration();
	}

	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		setCurrentHp(newHp, canRessurect, true);
	}

	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();

		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(_currentMp == newMp)
			return;

		if(newMp >= 0.5 && isDead())
			return;

		_currentMp = newMp;

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentMp < maxMp)
			startRegeneration();
	}

	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if(!isPlayer())
			return;

		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0, newCp));

		if(_currentCp == newCp)
			return;

		if(newCp >= 0.5 && isDead())
			return;

		_currentCp = newCp;

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentCp < maxCp)
			startRegeneration();
	}

	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}

	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();

		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(_currentHp == newHp && _currentMp == newMp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;

		_currentHp = newHp;
		_currentMp = newMp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		broadcastStatusUpdate();
		sendChanges();

		if(_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	@Override
	public final int getHeading()
	{
		return _heading;
	}

	public void setHeading(int heading)
	{
		_heading = heading;
	}

	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}

	public final void setName(String name)
	{
		_name = name;
	}

	public Creature getCastingTarget()
	{
		return castingTarget.get();
	}

	public void setCastingTarget(Creature target)
	{
		if(target == null)
			castingTarget = HardReferences.emptyRef();
		else
			castingTarget = target.getRef();
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void setSkillMastery(Skill skill)
	{
		_skillMasteryId = skill.getId();
	}

	public void setAggressionTarget(Creature target)
	{
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
			object = null;

		/* DS: на оффе сброс текущей цели не отменяет атаку или каст.
		if(object == null)
		{
			if(isAttackingNow() && getAI().getAttackTarget() == getTarget())
				abortAttack(false, true);
			if(isCastingNow() && getAI().getAttackTarget() == getTarget())
				abortCast(false, true);
		}
		*/

		if(object == null)
			target = HardReferences.emptyRef();
		else
			target = object.getRef();
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NULL)
			for(int i = 0; i < _abnormalEffects.length; i++)
				_abnormalEffects[i] = ae.getMask();
		else
			_abnormalEffects[ae.getType().ordinal()] |= ae.getMask();

		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	/**
	 * Запускаем задачу анимации боевой позы. Если задача уже запущена, увеличиваем время, которое персонаж будет в боевой позе на 15с
	 */
	protected void startAttackStanceTask0()
	{
		// предыдущая задача еще не закончена, увеличиваем время
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}

		_stanceEndTime = System.currentTimeMillis() + 15000L;

		broadcastPacket(new AutoAttackStart(getObjectId()));

		// отменяем предыдущую
		final Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);

		// Добавляем задачу, которая будет проверять, если истекло время нахождения персонажа в боевой позе,
		// отменяет задачу и останаливает анимацию.
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}

	/**
	 * Останавливаем задачу анимации боевой позы.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;

		final Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;

			broadcastPacket(new AutoAttackStop(getObjectId()));
		}
	}

	private class AttackStanceTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	/**
	 * Остановить регенерацию
	 */
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;

				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	/**
	 * Запустить регенерацию
	 */
	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0L)
			return;

		if(_isRegenerating)
			return;

		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, 0, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	public long getRegenTick()
	{
		return 3333L;
	}

	private class RegenTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(isDead() || getRegenTick() == 0L)
				return;

			double hpStart = _currentHp;

			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;

			double addHp = 0.;
			double addMp = 0.;

			regenLock.lock();
			try
			{
				if(_currentHp < maxHp)
					addHp += Formulas.calcHpRegen(Creature.this);

				if(_currentMp < maxMp)
					addMp += Formulas.calcMpRegen(Creature.this);

				// Added regen bonus when character is sitting
				if(isPlayer() && Config.REGEN_SIT_WAIT)
				{
					Player pl = (Player) Creature.this;
					if(pl.isSitting())
					{
						pl.updateWaitSitTime();
						if(pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
						}
					}
				}
				else if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}

				_currentHp += Math.max(0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100. - _currentHp));
				_currentMp += Math.max(0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100. - _currentMp));

				_currentHp = Math.min(maxHp, _currentHp);
				_currentMp = Math.min(maxMp, _currentMp);

				if(isPlayer())
				{
					_currentCp += Math.max(0, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100. - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
				}

				//отрегенились, останавливаем задачу
				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}

			broadcastStatusUpdate();
			sendChanges();

			checkHpMessages(hpStart, _currentHp);
		}
	}

	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		_abnormalEffects[ae.getType().ordinal()] &= ~ ae.getMask();
		sendChanges();
	}

	public void setUndying(SpecialEffectState val)
	{
		_undyingState = val;
		_undyingFlag.set(false);
	}

	public boolean isUndying(Creature attacker)
	{
		return _undyingState != SpecialEffectState.FALSE;
	}
	/**
	 * Блокируем персонажа
	 */
	public void block()
	{
		_blocked = true;
	}

	/**
	 * Разблокируем персонажа
	 */
	public void unblock()
	{
		_blocked = false;
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startRooted()
	{
		return _rooted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopRooted()
	{
		return _rooted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}

	public void startFrozen()
	{
		_frozen = true;
	}

	public void stopFrozen()
	{
		_frozen = false;
	}

	public void setMeditated(boolean value)
	{
		_meditated = value;
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}

	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}

	public void setInvul(SpecialEffectState value)
	{
		_invulState = value;
	}

	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}

	public boolean isConfused()
	{
		return _confused.get();
	}

	public boolean isAfraid()
	{
		return _afraid.get();
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(SkillEntry skill)
	{
		if(skill == null || skill.getTemplate().isNotAffectedByMute())
			return false;
		return isMMuted() && skill.getTemplate().isMagic() || isPMuted() && !skill.getTemplate().isMagic();
	}

	public boolean isPMuted()
	{
		return _pmuted.get();
	}

	public boolean isMMuted()
	{
		return _muted.get();
	}

	public boolean isAMuted()
	{
		return _amuted.get();
	}

	public boolean isRooted()
	{
		return _rooted.get();
	}

	public boolean isSleeping()
	{
		return _sleeping.get();
	}

	public boolean isStunned()
	{
		return _stunned.get();
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}

	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}

	public boolean isFrozen()
	{
		return _frozen;
	}

	public boolean isImmobilized()
	{
		return isImmobilized0() || getRunSpeed() < 1;
	}

	public final boolean isImmobilized0()
	{
		return _immobilized.get();
	}

	public boolean isHealBlocked()
	{
		return isDead() || _healBlocked.get();
	}

	public boolean isDamageBlocked()
	{
		return isInvul() || _damageBlocked.get();
	}

	public boolean isCastingNow()
	{
		return _skillTask != null;
	}

	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}

	public boolean isMovementDisabled()
	{
		if (isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isFrozen())
			return true;

		if (isCastingNow())
		{
			final SkillEntry skill = getCastingSkill();
			if (skill != null && skill.getTemplate().stopActor())
				return true;
		}
		return false;
	}

	public boolean isActionsDisabled()
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid() || isFrozen();
	}

	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}

	public void teleToLocation(Location loc, int refId)
	{
		teleToLocation(loc.x, loc.y, loc.z, refId);
	}

	public void teleToLocation(Location loc, Reflection r)
	{
		teleToLocation(loc.x, loc.y, loc.z, r);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}

	public void checkAndRemoveInvisible()
	{
		SpecialEffectState invisibleType = getInvisible();
		if(invisibleType == SpecialEffectState.TRUE)
			getEffectList().stopEffects(EffectType.Invisible);
	}

	public void teleToLocation(int x, int y, int z, int refId)
	{
		Reflection r = ReflectionManager.getInstance().get(refId);
		if(r == null)
			return;
		teleToLocation(x, y, z, r);
	}

	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return;

		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);
		stopMove();

		if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());

		//TODO [G1ta0] убрать DimensionalRiftManager.teleToLocation
		if(isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true))
		{
			Player player = (Player) this;
			if(player.isInParty() && player.getParty().isInDimensionalRift())
			{
				Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
				x = newCoords.x;
				y = newCoords.y;
				z = newCoords.z;
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
		}

		if(isPlayer())
		{
			Player player = (Player) this;

			player.getListeners().onTeleport(x, y, z, r);

			// DS: перенесено до decayMe() потому что иначе Delete() выдергивает воздушный корабль из-под задницы и сбивает местонахождение в корабле
			player.sendPacket(new TeleportToLocation(player, x, y, z));

			decayMe();

			setXYZ(x, y, z);

			setReflection(r);

			// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);

			//player.sendPacket(new TeleportToLocation(player, x, y, z));
		}
		else
		{
			setXYZ(x, y, z);

			setReflection(r);

			broadcastPacket(new TeleportToLocation(this, x, y, z));
			onTeleported();
		}
	}

	public boolean onTeleported()
	{
		return isTeleporting.compareAndSet(true, false);
	}

	public void sendMessage(CustomMessage message)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}

	@Override
	public double getColRadius()
	{
		return getTemplate().collisionRadius;
	}

	@Override
	public double getColHeight()
	{
		return getTemplate().collisionHeight;
	}

	public EffectList getEffectList()
	{
		if(_effectList == null)
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new EffectList(this);
			}

		return _effectList;
	}

	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;

		NpcInstance leader;
		if(isRaid() || isMinion() && (leader = ((NpcInstance) this).getLeader()) != null && leader.isRaid())
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		else if(isNpc())
		{
			int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if(max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
		}

		if(attacker.getLevel() > max_attacker_level)
			return true;

		return false;
	}

	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(this);

		getEffectList().stopAllEffects();

		super.onDelete();
	}

	// ---------------------------- Not Implemented -------------------------------

	public void addExpAndSp(long exp, long sp)
	{}

	public void broadcastCharInfo()
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}

	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return true;
	}

	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}

	public boolean isFearImmune()
	{
		return false;
	}

	public boolean isLethalImmune()
	{
		return false; // return getMaxHp() >= 50000;
	}

	public boolean getChargedSoulShot()
	{
		return false;
	}

	public int getChargedSpiritShot(boolean first)
	{
		return 0;
	}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getConsumedSouls()
	{
		return 0;
	}

	public int getAgathionEnergy()
	{
		return 0;
	}

	public void setAgathionEnergy(int val)
	{
		//
	}

	public int getKarma()
	{
		return 0;
	}

	public double getLevelMod()
	{
		return (89. + getLevel()) / 100.0;
	}

	public int getNpcId()
	{
		return 0;
	}

	public Servitor getServitor()
	{
		return null;
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public void setTeam(TeamType t)
	{
		_team = t;
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}

	public void sendMessage(String message)
	{}

	public void sendPacket(IBroadcastPacket mov)
	{}

	public void sendPacket(IBroadcastPacket... mov)
	{}

	public void sendPacket(List<? extends IBroadcastPacket> mov)
	{}

	public void setIncreasedForce(int i)
	{}

	public void setConsumedSouls(int i, NpcInstance monster)
	{}

	public void startPvPFlag(Creature target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	public void updateEffectIcons()
	{}

	/**
	 * Выставить предельные значения HP/MP/CP и запустить регенерацию, если в этом есть необходимость
	 */
	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxMp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;

		if(_currentHp > maxHp)
			setCurrentHp(maxHp, false);
		if(_currentMp > maxMp)
			setCurrentMp(maxMp, false);
		if(_currentCp > maxCp)
			setCurrentCp(maxCp, false);

		if(_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}

	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}

	public void setOverhitAttacker(Creature attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public boolean isCursedWeaponEquipped()
	{
		return false;
	}

	public boolean isHero()
	{
		return false;
	}

	public int getAccessLevel()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public double getRateAdena()
	{
		return 1.;
	}

	public double getRateItems()
	{
		return 1.;
	}

	public double getRateExp()
	{
		return 1.;
	}

	public double getRateSp()
	{
		return 1.;
	}

	public double getRateSpoil()
	{
		return 1.;
	}

	public int getFormId()
	{
		return 0;
	}

	public boolean isNameAbove()
	{
		return true;
	}

	public boolean isTargetable()
	{
		return true;
	}

	@Override
	public void setLoc(Location loc)
	{
		setXYZ(loc.x, loc.y, loc.z);
	}

	public void setLoc(Location loc, boolean MoveTask)
	{
		setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}

	@Override
	public void setXYZ(int x, int y, int z)
	{
		setXYZ(x, y, z, false);
	}

	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!MoveTask)
			stopMove();

		moveLock.lock();
		try
		{
			super.setXYZ(x, y, z);
		}
		finally
		{
			moveLock.unlock();
		}

		updateZones();
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		updateStats();
		updateZones();
	}

	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h > 0)
			setHeading(loc.h);
		super.spawnMe(loc);
	}

	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
			setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		updateZones();
		clearStatusListeners();

		super.onDespawn();
	}

	public final void doDecay()
	{
		if(!isDead())
			return;

		onDecay();
	}

	protected void onDecay()
	{
		decayMe();
	}

	public void validateLocation(int broadcast)
	{
		L2GameServerPacket sp = new ValidateLocation(this);
		if(broadcast == 0)
			sendPacket(sp);
		else if(broadcast == 1)
			broadcastPacket(sp);
		else
			broadcastPacketToOthers(sp);
	}

	public abstract int getLevel();

	public abstract ItemInstance getActiveWeaponInstance();

	public abstract WeaponTemplate getActiveWeaponItem();

	public abstract ItemInstance getSecondaryWeaponInstance();

	public abstract WeaponTemplate getSecondaryWeaponItem();

	public CharListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}

	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}

		return _statsRecorder;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	/**
	 * Внимание: цифры дамага отсылаются в displayReceiveDamageMessage() цели,
	 * здесь только общие сообщения.
	 */
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if (target.isPlayer())
		{
			if (miss && !target.isDamageBlocked())
				target.sendPacket(new SystemMessage(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));
			else if (shld)
			{
				if(damage > 1)
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				else if(damage == 1)
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			}
		}
	}

	/**
	 * Здесь отсылка цифр дамага атакующему от цели.
	 * Самой цели сообщения отсылаются в Playables (Player, Summon/Pet).
	 * Вызов super обязателен.
	 */
	public void displayReceiveDamageMessage(Creature attacker, int damage, int transfered, int reflected, boolean toTargetOnly)
	{
		if (!isDead() && !isDamageBlocked() && attacker.isPlayable() && attacker.getPlayer() != null)
		{
			if (!toTargetOnly)
			{
				if (transfered > 0)
					attacker.getPlayer().sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_DEALT_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR).addNumber(damage).addNumber(transfered));
				else if (attacker.isPet())
					attacker.getPlayer().sendPacket(new SystemMessage(SystemMsg.YOUR_PET_HIT_FOR_S1_DAMAGE).addNumber(damage));
				else
					attacker.getPlayer().sendPacket(new SystemMessage(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(attacker).addName(this).addNumber(damage));

				if (reflected > 0)
					attacker.getPlayer().sendPacket(new SystemMessage(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addNumber(reflected));
			}
		}
	}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.values();
	}

	public TimeStamp getSkillReuse(SkillEntry skill)
	{
		return _skillReuses.get(skill.hashCode());
	}

	public void enableSkillsByEntryType(SkillEntryType entryType)
	{
		if(entryType == SkillEntryType.NONE)
			throw new IllegalArgumentException();

		for(SkillEntry entry : _skills.values())
		{
			if(entry.getEntryType() != entryType || !entry.isDisabled())
				continue;

			entry.setDisabled(false);

			addStatFuncs(entry.getStatFuncs());
			addTriggers(entry.getTemplate());
		}
	}

	public void disableSkillsByEntryType(SkillEntryType entryType)
	{
		if(entryType == SkillEntryType.NONE)
			throw new IllegalArgumentException();

		for(SkillEntry entry : _skills.values())
		{
			if(entry.getEntryType() != entryType || entry.isDisabled())
				continue;

			entry.setDisabled(true);

			removeTriggers(entry.getTemplate());
			removeStatsByOwner(entry);
		}
	}

	public void onSeeSpell(SkillEntry skill, Creature caster)
	{}
}