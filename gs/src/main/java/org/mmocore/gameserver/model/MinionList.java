package org.mmocore.gameserver.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.templates.npc.MinionData;


/**
 * Список минионов моба. 
 * @author G1ta0
 */
public class MinionList extends RunnableImpl
{
	private final Set<MinionData> _minionData;
	private final Set<NpcInstance> _minions;
	private final Lock lock;
	private final NpcInstance _master;

	public MinionList(NpcInstance master)
	{
		_master = master;
		_minions = new HashSet<NpcInstance>();
		_minionData = new HashSet<MinionData>();
		_minionData.addAll(_master.getTemplate().getMinionData());
		lock = new ReentrantLock();
	}

	@Override
	public void runImpl() throws Exception
	{
		if (_master.isVisible() && !_master.isDead())
			spawnMinions();
	}

	/**
	 * Добавить шаблон для миниона
	 * @param m
	 */
	public boolean addMinion(MinionData m)
	{
		lock.lock();
		try
		{
			return _minionData.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 * Добавить миниона
	 * @param m
	 * @return true, если успешно добавлен
	 */
	public boolean addMinion(NpcInstance m)
	{
		lock.lock();
		try
		{
			return _minions.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return имеются ли живые минионы
	 */
	public boolean hasAliveMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					return true;
		}
		finally
		{
			lock.unlock();
		}
		return false;
	}

	public boolean hasMinions()
	{
		return _minionData.size() > 0 || _minions.size() > 0;
	}

	/**
	 * Возвращает список живых минионов
	 * @return список живых минионов
	 */
	public List<NpcInstance> getAliveMinions()
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>(_minions.size());
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					result.add(m);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	/**
	 *  Спавнит всех недостающих миньонов
	 */
	public void spawnMinions()
	{
		for(MinionData minion : _minionData)
			spawnMinion(minion.getMinionId(), minion.getAmount(), true);
	}

	/**
	 * Спавнит миньонов одного типа, по возможности используя старые инстансы повторно.
	 * @param minionId - npcId миньонов
	 * @param minionCount - нужное количество
	 * @param onSpawn - если true то старые живые миньоны удаляются и спавнятся заново, false - повторно используются только мертвые
	 */
	public void spawnMinion(int minionId, int minionCount, boolean onSpawn)
	{
		if (_master.isMinion() && _master.getNpcId() == minionId)
			return; // prevent eternal loop

		lock.lock();
		try
		{
			int count = minionCount;
			for(NpcInstance m : _minions)
			{
				if(m.getNpcId() == minionId && (onSpawn || m.isDead()))
				{
					count--;
					m.stopDecay();
					m.decayMe(); // doDecay() не выполнится !
					m.refreshID();
					_master.spawnMinion(m);
				}
			}

			if (count > 0)
			{
				for(int i = 0; i < count; i++)
				{
					NpcInstance m = NpcHolder.getInstance().getTemplate(minionId).getNewInstance();
					m.setLeader(_master);
					_master.spawnMinion(m);
					_minions.add(m);
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *  Деспавнит всех минионов
	 */
	public void unspawnMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				m.decayMe();
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 *	Удаляет минионов и чистит список 
	 */
	public void deleteMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				m.deleteMe();
			_minions.clear();
		}
		finally
		{
			lock.unlock();
		}
	}
}