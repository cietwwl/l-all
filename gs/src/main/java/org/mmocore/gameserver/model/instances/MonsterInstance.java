package org.mmocore.gameserver.model.instances;

import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.model.AggroList.HateInfo;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Manor;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestEventType;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.model.reward.RewardItem;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.model.reward.RewardType;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SocialAction;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.npc.Faction;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;

/**
 * This class manages all Monsters.
 *
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 */
public class MonsterInstance extends NpcInstance
{
	protected static final class RewardInfo
	{
		protected Creature _attacker;
		protected int _dmg = 0;

		public RewardInfo(final Creature attacker, final int dmg)
		{
			_attacker = attacker;
			_dmg = dmg;
		}

		public void addDamage(int dmg)
		{
			if(dmg < 0)
				dmg = 0;

			_dmg += dmg;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	/** crops */
	private boolean _isSeeded;
	private int _seederId;
	private boolean _altSeed;
	private RewardItem _harvestItem;

	private final Lock harvestLock = new ReentrantLock();

	private int overhitAttackerId;
	/** Stores the extra (over-hit) damage done to the L2NpcInstance when the attacker uses an over-hit enabled skill */
	private double _overhitDamage;

	/** The table containing all players objectID that successfully absorbed the soul of this L2NpcInstance */
	private TIntHashSet _absorbersIds;
	private final Lock absorbLock = new ReentrantLock();

	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _isSpoiled;
	private int spoilerId;
	/** Table containing all Items that a Dwarf can Sweep on this L2NpcInstance */
	private List<RewardItem> _sweepItems;
	private final Lock sweepLock = new ReentrantLock();

	private int _isChampion;

	private final boolean _canMove;

	public MonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		setUndying(SpecialEffectState.FALSE);
		_canMove = getParameter("canMove", true);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return !_canMove || super.isMovementDisabled();
	}

	@Override
	public boolean isLethalImmune()
	{
		return _isChampion > 0 || super.isLethalImmune();
	}

	@Override
	public boolean isFearImmune()
	{
		return _isChampion > 0 || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return _isChampion > 0 || super.isParalyzeImmune();
	}

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !attacker.isMonster();
	}

	public int getChampion()
	{
		return _isChampion;
	}

	public void setChampion()
	{
		if(getReflection().canChampions() && canChampion())
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100. >= random)
				setChampion(2);
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100. >= random)
				setChampion(1);
			else
				setChampion(0);
		}
		else
			setChampion(0);
	}

	public void setChampion(int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillTable.getInstance().getSkillEntry(4407, level));
			_isChampion = level;
		}
	}

	public boolean canChampion()
	{
		return !isMinion() && getTemplate().rewardExp > 0 && getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL;
	}

	@Override
	public TeamType getTeam()
	{
		return getChampion() == 2 ? TeamType.RED : getChampion() == 1 ? TeamType.BLUE : TeamType.NONE;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
	}

	@Override
	protected void onDespawn()
	{
		setOverhitDamage(0);
		setOverhitAttacker(null);
		clearSweep();
		clearHarvest();
		clearAbsorbers();

		super.onDespawn();
	}

	@Override
	public Location getRndMinionPosition()
	{
		return Location.findPointToStay(this, 100, 150);
	}

	@Override
	public void spawnMinion(NpcInstance minion)
	{
		if (minion.isMonster())
		{
			if(getChampion() == 2)
				((MonsterInstance)minion).setChampion(1);
			else
				((MonsterInstance)minion).setChampion(0);
		}
		super.spawnMinion(minion);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		calculateRewards(killer);

		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, SkillEntry skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(skill != null && skill.getTemplate().isOverhit())
		{
			// Calculate the over-hit damage
			// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
			double overhitDmg = (getCurrentHp() - damage) * -1;
			if(overhitDmg <= 0)
			{
				setOverhitDamage(0);
				setOverhitAttacker(null);
			}
			else
			{
				setOverhitDamage(overhitDmg);
				setOverhitAttacker(attacker);
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	public void calculateRewards(Creature lastAttacker)
	{
		Creature topDamager = getAggroList().getTopDamager();
		if(lastAttacker == null || !lastAttacker.isPlayable())
			lastAttacker = topDamager;

		if(lastAttacker == null || !lastAttacker.isPlayable())
			return;

		Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;

		Map<Playable, HateInfo> aggroMap = getAggroList().getPlayableMap();

		Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(quests != null && quests.length > 0)
		{
			List<Player> players = null; // массив с игроками, которые могут быть заинтересованы в квестах
			if(isRaid() && Config.ALT_NO_LASTHIT) // Для альта на ластхит берем всех игроков вокруг
			{
				players = new ArrayList<Player>(20);
				for(Playable pl : aggroMap.keySet())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						if (!players.contains(pl.getPlayer())) // не добавляем дважды если есть пет
							players.add(pl.getPlayer());
			}
			if(killer.getParty() != null) // если пати то собираем всех кто подходит
			{
				if (players == null)
					players = new ArrayList<Player>(killer.getParty().getMemberCount());
				for(Player pl : killer.getParty().getPartyMembers())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						if (!players.contains(pl.getPlayer())) // не добавляем дважды
							players.add(pl);
			}

			for(Quest quest : quests)
			{
				Player toReward = killer;
				if(!(quest.getParty() == Quest.PARTY_NONE || quest.getParty() == Quest.PARTY_ANY) && players != null)
					if(isRaid() || quest.getParty() == Quest.PARTY_ALL) // если цель рейд или квест для всей пати награждаем всех участников
					{
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted())
								quest.notifyKill(this, qs);
						}
						toReward = null;
					}
					else
					{ // иначе выбираем одного
						List<Player> interested = new ArrayList<Player>(players.size());
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted()) // из тех, у кого взят квест
								interested.add(pl);
						}

						if(interested.isEmpty())
							continue;

						toReward = interested.get(Rnd.get(interested.size()));
						if(toReward == null)
							toReward = killer;
					}

				if(toReward != null)
				{
					QuestState qs = toReward.getQuestState(quest);
					if(qs != null && !qs.isCompleted())
						quest.notifyKill(this, qs);
					else if (quest.getParty() == Quest.PARTY_ANY && killer.getParty() != null) // у избранного нет квеста
						for(Player pl : killer.getParty().getPartyMembers()) // этот тип квеста вызывается если он есть хотя бы у кого-то в пати
							if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
							{
								qs = pl.getQuestState(quest);
								if(qs != null && !qs.isCompleted())
								{
									quest.notifyKill(this, qs);
									break;
								}
							}
				}
			}
		}

		Map<Player, RewardInfo> rewards = new HashMap<Player, RewardInfo>();
		for(HateInfo info : aggroMap.values())
		{
			if(info.damage <= 1)
				continue;
			Playable attacker = (Playable) info.attacker;
			Player player = attacker.getPlayer();
			RewardInfo reward = rewards.get(player);
			if(reward == null)
				rewards.put(player, new RewardInfo(player, info.damage));
			else
				reward.addDamage(info.damage);
		}

		Player[] attackers = rewards.keySet().toArray(new Player[rewards.size()]);
		double[] xpsp = new double[2];

		for(Player attacker : attackers)
		{
			if(attacker.isDead())
				continue;

			RewardInfo reward = rewards.get(attacker);

			if(reward == null)
				continue;

			Party party = attacker.getParty();
			int maxHp = getMaxHp();

			xpsp[0] = 0.;
			xpsp[1] = 0.;

			if(party == null)
			{
				int damage = Math.min(reward._dmg, maxHp);
				if(damage > 0)
				{
					if(isInRangeZ(attacker, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						xpsp = calculateExpAndSp(attacker.getLevel(), damage);

					xpsp[0] = applyOverhit(killer, xpsp[0]);

					attacker.addExpAndCheckBonus(this, (long) xpsp[0], (long) xpsp[1], 1.);
				}
				rewards.remove(attacker);
			}
			else
			{
				int partyDmg = 0;
				int partylevel = 1;
				List<Player> rewardedMembers = new ArrayList<Player>();
				for(Player partyMember : party.getPartyMembers())
				{
					RewardInfo ai = rewards.remove(partyMember);
					if(partyMember.isDead() || !isInRangeZ(partyMember, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						continue;
					if(ai != null)
						partyDmg += ai._dmg;

					rewardedMembers.add(partyMember);
					if(partyMember.getLevel() > partylevel)
						partylevel = partyMember.getLevel();
				}
				partyDmg = Math.min(partyDmg, maxHp);
				if(partyDmg > 0)
				{
					xpsp = calculateExpAndSp(partylevel, partyDmg);
					double partyMul = (double) partyDmg / maxHp;
					xpsp[0] *= partyMul;
					xpsp[1] *= partyMul;
					xpsp[0] = applyOverhit(killer, xpsp[0]);
					party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
				}
			}
		}

		// Check the drop of a cursed weapon
		CursedWeaponsManager.getInstance().dropAttackable(this, killer);

		if(topDamager == null || !topDamager.isPlayable())
			return;

		for(RewardList rewardList : getTemplate().getRewards())
			rollRewards(rewardList, lastAttacker, topDamager);
	}

	@Override
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			broadcastPacket(new SocialAction(getObjectId(), 1));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	@Override
	public void startRandomAnimation()
	{
		//У мобов анимация обрабатывается в AI
	}

	@Override
	public int getKarma()
	{
		return 0;
	}

	public void addAbsorber(final Player attacker)
	{
		// The attacker must not be null
		if(attacker == null)
			return;

		if(getCurrentHpPercents() > 50)
			return;

		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
				_absorbersIds = new TIntHashSet();

			_absorbersIds.add(attacker.getObjectId());
		}
		finally
		{
			absorbLock.unlock();
		}
	}

	public boolean isAbsorbed(Player player)
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
				return false;
			if(!_absorbersIds.contains(player.getObjectId()))
				return false;
		}
		finally
		{
			absorbLock.unlock();
		}
		return true;
	}

	public void clearAbsorbers()
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds != null)
				_absorbersIds.clear();
		}
		finally
		{
			absorbLock.unlock();
		}
	}

	public RewardItem takeHarvest()
	{
		harvestLock.lock();
		try
		{
			RewardItem harvest;
			harvest = _harvestItem;
			clearHarvest();
			return harvest;
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public void clearHarvest()
	{
		harvestLock.lock();
		try
		{
			_harvestItem = null;
			_altSeed = false;
			_seederId = 0;
			_isSeeded = false;
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public boolean setSeeded(Player player, int seedId, boolean altSeed)
	{
		harvestLock.lock();
		try
		{
			if(isSeeded())
				return false;
			_isSeeded = true;
			_altSeed = altSeed;
			_seederId = player.getObjectId();
			_harvestItem = new RewardItem(Manor.getInstance().getCropType(seedId));
			// Количество всходов от xHP до (xHP + xHP/2)
			if(getTemplate().rateHp > 1)
				_harvestItem.count = Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5 * getTemplate().rateHp));
		}
		finally
		{
			harvestLock.unlock();
		}

		return true;
	}

	public boolean isSeeded(Player player)
	{
		//засиден этим игроком, и смерть наступила не более 20 секунд назад
		return isSeeded() && _seederId == player.getObjectId() && getDeadTime() < 20000L;
	}

	public boolean isSeeded()
	{
		return _isSeeded;
	}

	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoiled()
	{
		return _isSpoiled;
	}

	public boolean isSpoiled(Player player)
	{
		if(!isSpoiled()) // если не заспойлен то false
			return false;

		//заспойлен этим игроком, и смерть наступила не более 20 секунд назад
		if(player.getObjectId() == spoilerId && getDeadTime() < 20000L)
			return true;

		if(player.isInParty())
			for(Player pm : player.getParty().getPartyMembers())
				if(pm.getObjectId() == spoilerId && getDistance(pm) < Config.ALT_PARTY_DISTRIBUTION_RANGE)
					return true;

		return false;
	}

	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 * @param player
	 */
	public boolean setSpoiled(Player player)
	{
		sweepLock.lock();
		try
		{
			if(isSpoiled())
				return false;
			_isSpoiled = true;
			spoilerId = player.getObjectId();
		}
		finally
		{
			sweepLock.unlock();
		}
		return true;
	}

	/**
	 * Return True if a Dwarf use Sweep on the L2NpcInstance and if item can be spoiled.<BR><BR>
	 */
	public boolean isSweepActive()
	{
		sweepLock.lock();
		try
		{
			return _sweepItems != null && _sweepItems.size() > 0;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public List<RewardItem> takeSweep()
	{
		sweepLock.lock();
		try
		{
			List<RewardItem> sweep = _sweepItems;
			clearSweep();
			return sweep;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public void clearSweep()
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = false;
			spoilerId = 0;
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	protected Player lockDropTo(Player topDamager)
	{
		return topDamager;
	}

	protected void rollRewards(RewardList list, final Creature lastAttacker, Creature topDamager)
	{
		final Creature activeChar;
		final Player activePlayer;

		RewardType type = list.getType();
		if(type == RewardType.SWEEP)
		{
			if (!isSpoiled())
				return;
			activeChar = lastAttacker;
			activePlayer = lastAttacker.getPlayer();
		}
		else
		{
			activeChar = topDamager;
			activePlayer = lockDropTo(topDamager.getPlayer());
		}

		if(activePlayer == null)
			return;

		final int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		double mod = calcStat(Stats.REWARD_MULTIPLIER, 1., activeChar, null);
		mod *= Experience.penaltyModifier(diff, 9);


		List<RewardItem> rewardItems = list.roll(activePlayer, getRewardRate(activePlayer), mod);
		switch(type)
		{
			case SWEEP:
				_sweepItems = rewardItems;
				break;
			default:
				for(RewardItem drop : rewardItems)
				{
					// Если в моба посеяно семя, причем не альтернативное - не давать никакого дропа, кроме адены.
					if(isSeeded() && !_altSeed && !drop.isAdena)
						continue;
					dropItem(activePlayer, drop.itemId, drop.count, list.isAutoLoot());
				}
				break;
		}
	}

	private double[] calculateExpAndSp(int level, long damage)
	{
		int diff = level - getLevel();
		if(level > 77 && diff > 3 && diff <= 5) // kamael exp penalty
			diff += 3;

		double xp = getExpReward() * damage / getMaxHp();
		double sp = getSpReward() * damage / getMaxHp();

		if(diff > 5)
		{
			double mod = Math.pow(.83, diff - 5);
			xp *= mod;
			sp *= mod;
		}

		xp = Math.max(0., xp);
		sp = Math.max(0., sp);

		return new double[] { xp, sp };
	}

	private double applyOverhit(Player killer, double xp)
	{
		if(xp > 0 && killer.getObjectId() == overhitAttackerId)
		{
			int overHitExp = calculateOverhitExp(xp);
			killer.sendPacket(SystemMsg.OVERHIT, new SystemMessage(SystemMsg.YOU_HAVE_ACQUIRED_S1_BONUS_EXPERIENCE_FROM_A_SUCCESSFUL_OVERHIT).addNumber(overHitExp));
			xp += overHitExp;
		}
		return xp;
	}

	@Override
	public void setOverhitAttacker(Creature attacker)
	{
		overhitAttackerId = attacker == null ? 0 : attacker.getObjectId();
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	@Override
	public void setOverhitDamage(double damage)
	{
		_overhitDamage = damage;
	}

	public int calculateOverhitExp(final double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();
		if(overhitPercentage > 25)
			overhitPercentage = 25;
		double overhitExp = overhitPercentage / 100 * normalExp;
		setOverhitAttacker(null);
		setOverhitDamage(0);
		return (int) Math.round(overhitExp);
	}

	@Override
	public boolean isAggressive()
	{
		return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}

	@Override
	public Faction getFaction()
	{
		return Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFaction() : Faction.NONE;
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}