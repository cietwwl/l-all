package org.mmocore.gameserver.model.instances;

import java.util.concurrent.Future;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.dao.SummonDAO;
import org.mmocore.gameserver.dao.SummonEffectDAO;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SetSummonRemainTime;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.mmocore.gameserver.templates.npc.NpcTemplate;


public class SummonInstance extends Servitor
{
	public final static int CYCLE = 5000; // in millis
	private double _expPenalty = 0;
	private int _itemConsumeIdInTime;
	private int _itemConsumeCountInTime;
	private int _itemConsumeDelay;

	private Future<?> _disappearTask;

	private int _consumeCountdown;
	private int _lifetimeCountdown;
	private int _maxLifetime;

	private final int _skillId;

	private boolean _isSiegeSummon;

	public SummonInstance(int objectId, NpcTemplate template, Player owner, int currentLifeTime, int maxLifeTime, int consumeid, int consumecount, int consumedelay, int skillId)
	{
		super(objectId, template, owner);
		setName(template.name);
		_lifetimeCountdown = currentLifeTime;
		_maxLifetime = maxLifeTime;
		_itemConsumeIdInTime = consumeid;
		_itemConsumeCountInTime = consumecount;
		_consumeCountdown = _itemConsumeDelay = consumedelay;
		_skillId = skillId;
		_disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(), CYCLE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<SummonInstance> getRef()
	{
		return (HardReference<SummonInstance>) super.getRef();
	}

	@Override
	public final int getLevel()
	{
		return getTemplate() != null ? getTemplate().level : 0;
	}

	@Override
	public int getServitorType()
	{
		return SUMMON_TYPE;
	}

	@Override
	public int getCurrentFed()
	{
		return _lifetimeCountdown;
	}

	@Override
	public int getMaxFed()
	{
		return _maxLifetime;
	}

	public void setExpPenalty(double expPenalty)
	{
		_expPenalty = expPenalty;
	}

	@Override
	public double getExpPenalty()
	{
		return _expPenalty;
	}

	class Lifetime extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Player owner = getPlayer();
			if(owner == null)
			{
				_disappearTask = null;
				unSummon(false, false);
				return;
			}

			int usedtime = isInCombat() ? CYCLE : CYCLE / 4;
			_lifetimeCountdown -= usedtime;

			if(_lifetimeCountdown <= 0)
			{
				owner.sendPacket(SystemMsg.YOUR_SERVITOR_HAS_VANISHED_YOULL_NEED_TO_SUMMON_A_NEW_ONE);
				_disappearTask = null;
				unSummon(false, false);
				return;
			}

			_consumeCountdown -= usedtime;
			if(_itemConsumeIdInTime > 0 && _itemConsumeCountInTime > 0 && _consumeCountdown <= 0)
				if(owner.getInventory().destroyItemByItemId(getItemConsumeIdInTime(), getItemConsumeCountInTime()))
				{
					_consumeCountdown = _itemConsumeDelay;
					owner.sendPacket(new SystemMessage(SystemMsg.A_SUMMONED_MONSTER_USES_S1).addItemName(getItemConsumeIdInTime()));
				}
				else
				{
					owner.sendPacket(SystemMsg.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_HAS_DISAPPEARED);
					unSummon(false, false);
				}

			owner.sendPacket(new SetSummonRemainTime(SummonInstance.this));

			_disappearTask = ThreadPoolManager.getInstance().schedule(this, CYCLE);
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		saveEffects();

		if(_disappearTask != null)
		{
			_disappearTask.cancel(false);
			_disappearTask = null;
		}
	}

	public int getItemConsumeIdInTime()
	{
		return _itemConsumeIdInTime;
	}

	public int getItemConsumeCountInTime()
	{
		return _itemConsumeCountInTime;
	}

	public int getItemConsumeDelay()
	{
		return _itemConsumeDelay;
	}

	protected synchronized void stopDisappear()
	{
		if(_disappearTask != null)
		{
			_disappearTask.cancel(false);
			_disappearTask = null;
		}
	}

	@Override
	public void unSummon(boolean saveEffects, boolean store)
	{
		stopDisappear();
		if(store)
			SummonDAO.getInstance().insert(getPlayer(), this);
		else if(isSiegeSummon())
		{
			SiegeEvent<?,?> siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null) //FIXME [VISTALL] тут не должно быть null? Узнать когда нулл и поправить
				siegeEvent.removeSiegeSummon(getPlayer(), this);
		}

		super.unSummon(saveEffects, store);
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
			getPlayer().sendPacket(new SystemMessage(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addNumber(damage));
			if (reflected > 0)
				getPlayer().sendPacket(new SystemMessage(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(attacker).addName(this).addNumber(reflected));
		}
	}

	public int getCallSkillId()
	{
		return _skillId;
	}

	@Override
	public int getId()
	{
		return _skillId;
	}

	@Override
	public boolean isSummon()
	{
		return true;
	}

	// TODO: DS: не забыть удалить после корректного вычисления статов самонов
	@Override
	public double getLevelMod()
	{
		return 1;
	}

	@Override
	public long getWearedMask()
	{
		return WeaponType.SWORD.mask(); // TODO: читать пассивки и смотреть тип оружия и брони там
	}

	@Override
	public int getSoulshotConsumeCount()
	{
		return getTemplate().getSoulShotCount();
	}

	@Override
	public int getSpiritshotConsumeCount()
	{
		return getTemplate().getSpiritShotCount();
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
			SummonEffectDAO.getInstance().insert(this);
	}

	public boolean isSiegeSummon()
	{
		return _isSiegeSummon;
	}

	public void setSiegeSummon(boolean siegeSummon)
	{
		_isSiegeSummon = siegeSummon;
	}
}