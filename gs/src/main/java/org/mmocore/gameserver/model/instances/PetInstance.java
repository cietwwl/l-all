package org.mmocore.gameserver.model.instances;

import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.dao.PetDAO;
import org.mmocore.gameserver.dao.PetEffectDAO;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.PetData;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.base.BaseStats;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.PetInventory;
import org.mmocore.gameserver.model.items.attachment.FlagItemAttachment;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.InventoryUpdate;
import org.mmocore.gameserver.network.l2.s2c.SocialAction;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Log;
import org.mmocore.gameserver.utils.Log.ItemLog;

public class PetInstance extends Servitor
{
	class FeedTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Player owner = getPlayer();

			if (getCurrentFed() * 100 <= getMaxFed() * 55) // меньше или равно 55%
				for (ItemInstance food : getInventory().getItems())
				{
					if (!food.getTemplate().isPetFood())
						continue;

					if (!food.getTemplate().testCondition(PetInstance.this, food, false))
						continue;
					
					if (food.getTemplate().getHandler().useItem(PetInstance.this, food, false))
					{
						getPlayer().sendPacket(new SystemMessage(SystemMsg.YOUR_PET_WAS_HUNGRY_SO_IT_ATE_S1).addItemName(food.getItemId()));
						break;
					}
				}

			if(PetDataTable.isVitaminPet(getNpcId()) && getCurrentFed() <= 0)
				deleteMe();
			else if(getCurrentFed() <= 0.10 * getMaxFed())
			{
				// Если пища закончилась, отозвать пета
				owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.UnSummonHungryPet"));
				unSummon(false, false);
				return;
			}

			setCurrentFed(getCurrentFed() - 5);

			sendStatusUpdate();
			startFeed(isInCombat());
		}
	}

	private int _controlItemObjId;
	private int _curFed;
	protected PetData _data;
	private Future<?> _feedTask;
	protected PetInventory _inventory;
	private int _level;
	private int lostExp;

	private boolean _existsInDatabase;


	public static PetInstance restore(ItemInstance control, NpcTemplate template, Player owner)
	{
		return PetDAO.getInstance().select(owner, control, template);
	}

	public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		this(objectId, template, owner, control, 0);
	}

	public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, long exp)
	{
		super(objectId, template, owner);

		_controlItemObjId = control.getObjectId();
		_exp = exp;
		_level = control.getEnchantLevel();

		if(_level <= 0)
		{
			if(template.npcId == PetDataTable.SIN_EATER_ID)
				_level = owner.getLevel();
			else
				_level = template.level;
			_exp = getExpForThisLevel();
		}

		int minLevel = PetDataTable.getMinLevel(template.npcId);
		if(_level < minLevel)
			_level = minLevel;

		if(_exp < getExpForThisLevel())
			_exp = getExpForThisLevel();

		while(_exp >= getExpForNextLevel() && _level < Experience.getPetMaxLevel())
			_level++;

		while(_exp < getExpForThisLevel() && _level > minLevel)
			_level--;

		if(PetDataTable.isVitaminPet(template.npcId))
		{
			_level = owner.getLevel();
			_exp = getExpForNextLevel();
		}

		_data = PetDataTable.getInstance().getInfo(template.npcId, _level);
		_inventory = new PetInventory(this);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		startFeed(false);
	}

	@Override
	protected void onDespawn()
	{
		super.onSpawn();

		stopFeed();
	}

	@Override
	public void addExpAndSp(long addToExp, long addToSp)
	{
		Player owner = getPlayer();

		if(PetDataTable.isVitaminPet(getNpcId()))
			return;

		_exp += addToExp;
		_sp += addToSp;

		if(_exp > getMaxExp())
			_exp = getMaxExp();

		if(addToExp > 0 || addToSp > 0)
			owner.sendPacket(new SystemMessage(SystemMsg.YOUR_PET_GAINED_S1_EXPERIENCE_POINTS).addNumber(addToExp));

		int old_level = _level;

		while(_level < Experience.getPetMaxLevel() && _exp >= getExpForNextLevel())
			_level++;

		while(_level > getMinLevel() && _exp < getExpForThisLevel())
			_level--;

		if(old_level < _level)
		{
			owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.PetLevelUp").addNumber(_level));
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
			setCurrentHpMp(getMaxHp(), getMaxMp());
		}

		if(old_level != _level)
		{
			updateControlItem();
			updateData();
		}

		if(addToExp > 0 || addToSp > 0)
			sendStatusUpdate();
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return getInventory().destroyItemByItemId(itemConsumeId, itemCount);
	}

	private void deathPenalty()
	{
		if(isInZoneBattle())
			return;
		int lvl = getLevel();
		double percentLost = -0.07 * lvl + 6.5;
		// Calculate the Experience loss
		lostExp = (int) Math.round((getExpForNextLevel() - getExpForThisLevel()) * percentLost / 100);
		addExpAndSp(-lostExp, 0);
	}

	/**
	 * Remove the Pet from DB and its associated item from the player inventory
	 */
	private void destroyControlItem()
	{
		Player owner = getPlayer();
		if(getControlItemObjId() == 0)
			return;
		if(!owner.getInventory().destroyItemByObjectId(getControlItemObjId(), 1L))
			return;

		PetDAO.getInstance().delete(getControlItemObjId());
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		Player owner = getPlayer();

		owner.sendPacket(SystemMsg.THE_PET_HAS_BEEN_KILLED);
		startDecay(86400000L);

		if(PetDataTable.isVitaminPet(getNpcId()))
			return;

		stopFeed();
		deathPenalty();
	}

	@Override
	public void doPickupItem(GameObject object)
	{
		Player owner = getPlayer();

		stopMove();

		if(!object.isItem())
			return;

		ItemInstance item = (ItemInstance) object;

		if(item.isCursed())
		{
			owner.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_FAILED_TO_PICK_UP_S1).addItemName(item.getItemId()));
			return;
		}

		synchronized (item)
		{
			if(!item.isVisible())
				return;

			if(item.isHerb())
			{
				SkillEntry[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
					for(SkillEntry skill : skills)
						altUseSkill(skill, this);
				item.deleteMe();
				return;
			}

			if(!getInventory().validateWeight(item))
			{
				sendPacket(SystemMsg.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS_);
				return;
			}

			if(!getInventory().validateCapacity(item))
			{
				sendPacket(SystemMsg.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}

			if(!item.getTemplate().getHandler().pickupItem(this, item))
				return;

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;
			if(attachment != null)
				return;

			if(owner.getParty() == null || owner.getParty().getLootDistribution() == Party.ITEM_LOOTER)
			{
				Log.LogItem(owner, ItemLog.PetPickup, item);
				getInventory().addItem(item);
				sendChanges();
				broadcastPickUpMsg(item);
				item.pickupMe();
			}
			else
				owner.getParty().distributeItem(owner, item, null);
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
		stopDecay();
		super.doRevive();
		startFeed(false);
		setRunning();
	}

	@Override
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, _data.getAccuracy(), null, null);
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	public ItemInstance getControlItem()
	{
		Player owner = getPlayer();
		if(owner == null)
			return null;
		int item_obj_id = getControlItemObjId();
		if(item_obj_id == 0)
			return null;
		return owner.getInventory().getItemByObjectId(item_obj_id);
	}

	@Override
	public int getControlItemObjId()
	{
		return _controlItemObjId;
	}

	@Override
	public int getId()
	{
		return _controlItemObjId;
	}

	@Override
	public int getCriticalHit(Creature target, SkillEntry skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _data.getCritical(), target, skill);
	}

	@Override
	public int getCurrentFed()
	{
		return _curFed;
	}

	@Override
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, _data.getEvasion(), target, null);
	}

	@Override
	public long getExpForNextLevel()
	{
		return PetDataTable.getInstance().getInfo(getNpcId(), _level + 1).getExp();
	}

	@Override
	public long getExpForThisLevel()
	{
		return PetDataTable.getInstance().getInfo(getNpcId(), _level).getExp();
	}

	public int getFoodId()
	{
		return _data.getFoodId();
	}

	public int getAddFed()
	{
		return _data.getAddFed();
	}

	public boolean isStarving()
	{
		return getCurrentFed() * 100 <= getMaxFed(); // меньше 1%
	}

	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	@Override
	public final int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public int getMinLevel()
	{
		return _data.getMinLevel();
	}

	public long getMaxExp()
	{
		return PetDataTable.getInstance().getInfo(getNpcId(), Experience.getPetMaxLevel() + 1).getExp();
	}

	@Override
	public int getMaxFed()
	{
		return _data.getFeedMax();
	}

	@Override
	public int getMaxLoad()
	{
		return (int) calcStat(Stats.MAX_LOAD, _data.getMaxLoad(), null, null);
	}

	@Override
	public int getInventoryLimit()
	{
		return Config.ALT_PET_INVENTORY_LIMIT;
	}

	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _data.getHP(), null, null);
	}

	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _data.getMP(), null, null);
	}

	@Override
	public int getPAtk(Creature target)
	{
		// В базе указаны параметры, уже домноженные на этот модификатор, для удобства. Поэтому вычисляем и убираем его.
		double mod = BaseStats.STR.calcBonus(this) * getLevelMod();
		return (int) calcStat(Stats.POWER_ATTACK, _data.getPAtk() / mod, target, null);
	}

	@Override
	public int getPDef(Creature target)
	{
		// В базе указаны параметры, уже домноженные на этот модификатор, для удобства. Поэтому вычисляем и убираем его.
		double mod = getLevelMod();
		return (int) calcStat(Stats.POWER_DEFENCE, _data.getPDef() / mod, target, null);
	}

	@Override
	public int getMAtk(Creature target, SkillEntry skill)
	{
		// В базе указаны параметры, уже домноженные на этот модификатор, для удобства. Поэтому вычисляем и убираем его.
		double ib = BaseStats.INT.calcBonus(this);
		double lvlb = getLevelMod();
		double mod = lvlb * lvlb * ib * ib;
		return (int) calcStat(Stats.MAGIC_ATTACK, _data.getMAtk() / mod, target, skill);
	}

	@Override
	public int getMDef(Creature target, SkillEntry skill)
	{
		// В базе указаны параметры, уже домноженные на этот модификатор, для удобства. Поэтому вычисляем и убираем его.
		double mod = BaseStats.MEN.calcBonus(this) * getLevelMod();
		return (int) calcStat(Stats.MAGIC_DEFENCE, _data.getMDef() / mod, target, skill);
	}

	@Override
	public int getPAtkSpd(boolean applyLimit)
	{
		final int result = (int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _data.getAtkSpeed(), null, null), null, null);
		return applyLimit && result > Config.LIM_PATK_SPD ? Config.LIM_PATK_SPD : result;
	}

	@Override
	public int getMAtkSpd()
	{
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _data.getCastSpeed(), null, null);
	}

	@Override
	public int getRunSpeed()
	{
		return getSpeed(_data.getSpeed());
	}

	@Override
	public int getSoulshotConsumeCount()
	{
		return PetDataTable.getSoulshots(getNpcId());
	}

	@Override
	public int getSpiritshotConsumeCount()
	{
		return PetDataTable.getSpiritshots(getNpcId());
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public int getServitorType()
	{
		return PET_TYPE;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public boolean isMountable()
	{
		return _data.isMountable();
	}

	public void restoreExp(double percent)
	{
		if(lostExp != 0)
		{
			addExpAndSp((long) (lostExp * percent / 100.), 0);
			lostExp = 0;
		}
	}

	public void setCurrentFed(int num)
	{
		_curFed = Math.min(getMaxFed(), Math.max(0, num));
	}

	@Override
	public void setSp(int sp)
	{
		_sp = sp;
	}

	public void startFeed(boolean battleFeed)
	{
		boolean first = _feedTask == null;
		stopFeed();
		if(!isDead())
		{
			int feedTime;
			if(PetDataTable.isVitaminPet(getNpcId()))
				feedTime = 10000;
			else
				feedTime = Math.max(first ? 15000 : 1000, 60000 / (battleFeed ? _data.getFeedBattle() : _data.getFeedNormal()));
			_feedTask = ThreadPoolManager.getInstance().schedule(new FeedTask(), feedTime);
		}
	}

	private void stopFeed()
	{
		if(_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}

	@Override
	protected void onDecay()
	{
		getInventory().store();
		destroyControlItem(); // this should also delete the pet from the db

		super.onDecay();
	}


	@Override
	public void unSummon(boolean saveEffects, boolean store)
	{
		stopFeed();

		getInventory().store();

 		if(getControlItemObjId() == 0)
			return;

		if(saveEffects)
			saveEffects();

		if(isExistsInDatabase())
			PetDAO.getInstance().update(this);
		else
			PetDAO.getInstance().insert(this);

		deleteMe();
	}

	public void updateControlItem()
	{
		ItemInstance controlItem = getControlItem();
		if(controlItem == null)
			return;
		controlItem.setEnchantLevel(_level);
		controlItem.setCustomType2(isDefaultName() ? 0 : 1);
		controlItem.setJdbcState(JdbcEntityState.UPDATED);
		controlItem.update();
		Player owner = getPlayer();
		owner.sendPacket(new InventoryUpdate().addModifiedItem(controlItem));
	}

	private void updateData()
	{
		_data = PetDataTable.getInstance().getInfo(getTemplate().npcId, _level);
	}

	@Override
	public double getExpPenalty()
	{
		return PetDataTable.getExpPenalty(getTemplate().npcId);
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if (getPlayer() != null)
		{
			if(crit)
				getPlayer().sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
			if(miss)
				getPlayer().sendPacket(new SystemMessage(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(this));
		}
	}

	@Override
	public final void displayReceiveDamageMessage(Creature attacker, int damage, int transfered, int reflected, boolean toTargetOnly)
	{
		super.displayReceiveDamageMessage(attacker, damage, transfered, reflected, toTargetOnly);

		if(attacker != this && !isDead() && getPlayer() != null)
		{
			getPlayer().sendPacket(new SystemMessage(SystemMsg.YOUR_PET_RECEIVED_S2_DAMAGE_BY_C1).addName(attacker).addNumber(damage));
			if (reflected > 0)
				getPlayer().sendPacket(new SystemMessage(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(attacker).addName(this).addNumber(reflected));
		}
	}

	@Override
	public int getFormId()
	{
		switch(getNpcId())
		{
			case PetDataTable.GREAT_WOLF_ID:
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				if(getLevel() >= 70)
					return 3;
				else if(getLevel() >= 65)
					return 2;
				else if(getLevel() >= 60)
					return 1;
				break;
		}
		return 0;
	}

	@Override
	public boolean isPet()
	{
		return true;
	}

	public boolean isDefaultName()
	{
		return StringUtils.isEmpty(_name) || getName().equalsIgnoreCase(getTemplate().name);
	}

	@Override
	public void saveEffects()
	{
		Player owner = getPlayer();
		if(owner == null)
			return;

		if(owner.isInOlympiadMode())
			getEffectList().stopAllEffects();  //FIXME [VISTALL] нужно ли
		else
			PetEffectDAO.getInstance().insert(this);
	}

	public boolean isExistsInDatabase()
	{
		return _existsInDatabase;
	}

	public void setExistsInDatabase(boolean existsInDatabase)
	{
		_existsInDatabase = existsInDatabase;
	}
}