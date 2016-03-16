package org.mmocore.gameserver.model;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.util.Rnd;
import org.mmocore.commons.util.concurrent.atomic.AtomicState;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.model.AggroList.AggroInfo;
import org.mmocore.gameserver.model.Skill.SkillTargetType;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.entity.boat.Boat;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.impl.DuelEvent;
import org.mmocore.gameserver.model.entity.events.impl.SingleMatchEvent;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.StaticObjectInstance;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.Revive;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.CharTemplate;
import org.mmocore.gameserver.templates.item.EtcItemTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;


public abstract class Playable extends Creature
{
	private AtomicState _isSilentMoving = new AtomicState();
	
	private boolean _isPendingRevive;

	/** Блокировка для чтения/записи состояний квестов */
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();

	protected int _zoneMask;

	public volatile boolean buff = false;

	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}

	public abstract Inventory getInventory();

	public abstract long getWearedMask();

	/**
	 * Проверяет, выставлять ли PvP флаг для игрока.<BR><BR>
	 */
	@Override
	public boolean checkPvP(final Creature target, Skill skill)
	{
		Player player = getPlayer();

		if(isDead() || target == null || player == null || target == this || target == player || target == player.getServitor() || player.getKarma() > 0)
			return false;

		if(skill != null)
		{
			if(skill.altUse())
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_CHEST)
				return false;
		}

		// Проверка на дуэли... Мэмбэры одной дуэли не флагаются
		SingleMatchEvent duelEvent = getEvent(SingleMatchEvent.class);
		if(duelEvent != null && !duelEvent.checkPvPFlag(target))
			return false;

		if(isInZonePeace() && target.isInZonePeace())
			return false;
		if(isInZoneBattle() && target.isInZoneBattle())
			return false;
		if(isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE))
			return false;

		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() > 0)
				return false;
			else if(target.isPlayable())
				return true;
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster())
			return true;

		return false;
	}

	/**
	 * Проверяет, можно ли атаковать цель (для физ атак)
	 */
	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return false;

		if(target == null || target.isDead())
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		if(!isInRange(target, 2000))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(target.isDoor() && !target.isAttackable(this))
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		if(target.paralizeOnAttack(this))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
				paralizeMe(target, Skill.SKILL_RAID_CURSE);
			return false;
		}

		if(target.isInvisible() || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target, false))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}

/* DS: это не нужно
		if(player.isInZone(ZoneType.epic) != target.isInZone(ZoneType.epic))
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
*/
		if(target.isPlayable())
		{
			// Нельзя атаковать того, кто находится на арене, если ты сам не на арене
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}

			// Если цель либо атакующий находится в мирной зоне - атаковать нельзя
			if(isInZonePeace() || target.isInZonePeace())
			{
				player.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
		}

		if(player.isInOlympiadMode() && !player.isOlympiadCompStarted())
			return false;

		return true;
	}

	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return;

		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("l2p.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack"));
			return;
		}

		if(!checkTarget(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}

		// Прерывать дуэли если цель не дуэлянт
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		WeaponTemplate weaponItem = getActiveWeaponItem();

		if(weaponItem != null && (weaponItem.getItemType() == WeaponType.BOW || weaponItem.getItemType() == WeaponType.CROSSBOW))
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0)
			{
				// cheap shot SA
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0., target, null);
				if(chance > 0 && Rnd.chance(chance))
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);

				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(SystemMsg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}

				reduceCurrentMp(bowMpConsume, null);
			}

			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponType.BOW ? SystemMsg.YOU_HAVE_RUN_OUT_OF_ARROWS : SystemMsg.NOT_ENOUGH_BOLTS);
				player.sendActionFailed();
				return;
			}
		}

		super.doAttack(target);
	}

	@Override
	public void doCast(final SkillEntry skill, final Creature target, boolean forceUse)
	{
		Player player = getPlayer();
		if(player == null || skill == null)
			return;

		// Прерывать дуэли если цель не дуэлянт
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		//нельзя использовать масс скиллы в мирной зоне
		if(skill.getTemplate().isNotTargetAoE() && skill.getTemplate().isOffensive() && isInPeaceZone())
		{
			player.sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return;
		}

		if(skill.getTemplate().isPvpSkill() && target.isNpc() && !target.isMonster() && !target.isAutoAttackable(this))
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		// до начала боя на олимпе атаковать никого нельзя
		if (skill.getTemplate().isOffensive() && player.isInOlympiadMode() && !player.isOlympiadCompStarted())
		{
			if (!skill.getTemplate().isNotTargetAoE() && !forceUse)
				player.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		super.doCast(skill, target, forceUse);
	}

	@Override
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

		if(attacker != this && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();
			if(pcAttacker != player)
				if(player.isInOlympiadMode() && !player.isOlympiadCompStarted())
				{
					if (sendMessage)
						pcAttacker.sendPacket(SystemMsg.INVALID_TARGET);
					return;
				}

			if(isInZoneBattle() != attacker.isInZoneBattle())
			{
				if (sendMessage)
					attacker.getPlayer().sendPacket(SystemMsg.INVALID_TARGET);
				return;
			}

			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent)
				duelEvent.abortDuel(player);
		}

		super.reduceCurrentHp(damage, attacker, skill, poleHitCount, crit, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	public int getPAtkSpd(boolean applyLimit)
	{
		final int result = Math.max((int) (calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.basePAtkSpd, null, null), null, null)), 1);
		return applyLimit && result > Config.LIM_PATK_SPD ? Config.LIM_PATK_SPD : result;
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? _template.basePAtk : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(final Creature target, final SkillEntry skill)
	{
		if(skill != null && skill.getTemplate().getMatak() > 0)
			return skill.getTemplate().getMatak();
		final double init = getActiveWeaponInstance() == null ? _template.baseMAtk : 0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAttackable(attacker, true, false);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isAttackable(attacker, false, false);
	}

	/**
	 * force - Ctrl нажат или нет.
	 * nextAttackCheck - для флагнутых не нужно нажимать Ctrl, но нет и автоатаки.
	 */
	public boolean isAttackable(Creature attacker, boolean force, boolean nextAttackCheck)
	{
		Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isAlikeDead() || attacker.isAlikeDead())
			return false;

		if(isInvisible() || getReflection() != attacker.getReflection())
			return false;

		Boat boat = player.getBoat();
		if(boat != null && !boat.isAirShip())
			return false;

		Player pcAttacker = attacker.getPlayer();
		if(isPlayer() && pcAttacker == this)
			return false;

		if(pcAttacker != null && pcAttacker != player)
		{
			boat = pcAttacker.getBoat();
			if(boat != null && !boat.isAirShip())
				return false;

			if(pcAttacker.getBlockCheckerArena() > -1 || player.getBlockCheckerArena() > -1)
				return false;
/* DS: это не нужно
			if(player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
				return false;
*/
			if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGame() != pcAttacker.getOlympiadGame()) // На всякий случай
				return false;
			if(player.isInOlympiadMode() && !player.isOlympiadCompStarted()) // Бой еще не начался
				return false;
			if(player.isInOlympiadMode() && player.isOlympiadCompStarted() && player.getOlympiadSide() == pcAttacker.getOlympiadSide() && !force) // Свою команду атаковать нельзя
				return false;

			if(!force && player.getParty() != null && player.getParty() == pcAttacker.getParty())
				return false;

			for(Event e : attacker.getEvents())
				if(e.checkForAttack(this, attacker, null, force) != null)
					return false;

			if(isInZoneBattle())
				return true;

			if(isInZonePeace())
				return false;

			for(Event e : attacker.getEvents())
				if(e.canAttack(this, attacker, null, force, nextAttackCheck))
					return true;

			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21
			if(pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)
				return false;

			if(!force && player.getClan() != null && player.getClan() == pcAttacker.getClan())
				return false;

			if(isInZone(ZoneType.SIEGE))
				return true;

			if(player.getKarma() > 0)
				return true;

			/* DS: для атаки нефлагнутых варов нужен Ctrl
			if(pcAttacker.atMutualWarWith(player))
				return !nextAttackCheck; */

			if(player.getPvpFlag() != 0)
				return !nextAttackCheck;

			return force;
		}

		return true;
	}

	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(SkillEntry skillEntry, List<Creature> targets, boolean useActionSkills)
	{
		Player player = getPlayer();
		if(player == null)
			return;

		Skill skill = skillEntry.getTemplate();
		if(useActionSkills && !skill.altUse())
			for(Creature target : targets)
			{
				if(target.isNpc())
				{
					if(skill.isOffensive())
					{
						// mobs will hate on debuff
						if(target.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
								paralizeMe(target, Skill.SKILL_RAID_CURSE_MUTE);
							return;
						}
						if(!skill.isAI())
						{
							int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, skillEntry, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skillEntry, this);
				}
				else
				if(target.isPlayable())
				{
					int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());

					List<NpcInstance> npcs = World.getAroundNpc(target);
					for(NpcInstance npc : npcs)
					{
						if(npc.isDead() || !npc.isInRangeZ(this, 2000)) //FIXME [G1ta0] параметр достойный конфига
							continue;

						npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skillEntry, this);

						AggroInfo ai = npc.getAggroList().get(target);
						//Пропускаем, если цель отсутсвует в хейтлисте
						if(ai == null)
							continue;

						if(!skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
								paralizeMe(npc, Skill.SKILL_RAID_CURSE_MUTE);
							return;
						}

						//Если хейт меньше 100, пропускаем
						if(ai.hate < 100)
							continue;

						if(GeoEngine.canSeeTarget(npc, target, false)) // Моб агрится только если видит цель, которую лечишь/бафаешь.
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
					}
				}

				// Check for PvP Flagging / Drawing Aggro
				if(checkPvP(target, skill))
					startPvPFlag(target);
			}

		super.callSkill(skillEntry, targets, useActionSkills);
	}

	/**
	 * Оповещает других игроков о поднятии вещи
	 * @param item предмет который был поднят
	 */
	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();

		if(item == null || player == null || player.isInvisible())
			return;

		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessage msg;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				msg = new SystemMessage(isPlayer() ? SystemMsg.ATTENTION_C1_HAS_PICKED_UP_S2S3 : SystemMsg.ATTENTION_C1S_PET_HAS_PICKED_UP_S2S3);
				msg.addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				msg = new SystemMessage(isPlayer() ? SystemMsg.ATTENTION_C1_HAS_PICKED_UP_S2 : SystemMsg.ATTENTION_C1S_PET_HAS_PICKED_UP_S2);
				msg.addString(player_name).addItemName(item.getItemId());
			}
			player.broadcastPacket(msg);
		}
	}

	public void paralizeMe(Creature effector, int skillId)
	{
		SkillEntry revengeSkill = SkillTable.getInstance().getSkillEntry(skillId, 1);
		revengeSkill.getEffects(effector, this, false, false);
	}

	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}

	/** Sets HP, MP and CP and revives the L2Playable. */
	public void doRevive()
	{
		getListeners().onRevive();

		if(!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

			if(isSalvation())
			{
				for(Effect e : getEffectList().getAllEffects())
					if(e.getEffectType() == EffectType.Salvation)
					{
						e.exit();
						break;
					}
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				double value;
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0)
				{
					value = getMaxCp() * Config.RESPAWN_RESTORE_CP;
					if (getCurrentCp() < value)
						setCurrentCp(value);
				}

				value = Math.max(1, getMaxHp() * Config.RESPAWN_RESTORE_HP);
				if (getCurrentHp() < value)
					setCurrentHp(value, true);

				if(Config.RESPAWN_RESTORE_MP >= 0)
				{
					value = getMaxMp() * Config.RESPAWN_RESTORE_MP;
					if (getCurrentMp() < value)
						setCurrentMp(value);
				}
			}

			broadcastPacket(new Revive(this));
		}
		else
			setPendingRevive(true);
	}

	public abstract void doPickupItem(GameObject object);

	public void sitDown(StaticObjectInstance throne)
	{}

	public void standUp()
	{}

	private long _nonAggroTime;

	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}

	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}

	/**
	 * 
	 * @return предыдущее состояние
	 */
	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}

	/**
	 * 
	 * @return текущее состояние
	 */
	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}
	
	/**
	 * @return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}

	public boolean isInCombatZone()
	{
		return isInZoneBattle();
	}

	public boolean isInPeaceZone()
	{
		return isInZonePeace();
	}

	@Override
	public boolean isInZoneBattle()
	{
		return super.isInZoneBattle();
	}

	public boolean isOnSiegeField()
	{
		return isInZone(ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		return isInZone(ZoneType.damage) || isInZone(ZoneType.swamp) || isInZone(ZoneType.poison) || isInZone(ZoneType.instant_skill);
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	public int getMaxLoad()
	{
		return 0;
	}

	public int getInventoryLimit()
	{
		return 0;
	}

	public int getRelation(Player target)
	{
		return 0;
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}
}