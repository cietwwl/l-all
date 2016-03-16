package org.mmocore.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 2:17/26.06.2011
 */
public class DuelSnapshotObject
{
	private final TeamType _team;
	private Player _player;
	//
	private List<Effect> _effects = Collections.emptyList();
	private Location _returnLoc;
	private double _currentHp;
	private double _currentMp;
	private double _currentCp;
	private int _classId;

	private boolean _isDead;

	public DuelSnapshotObject(Player player, TeamType team, boolean store)
	{
		_player = player;
		_team = team;
		if(store)
			store();
	}

	public void store()
	{
		_returnLoc = _player._stablePoint == null ? _player.getReflection().getReturnLoc() == null ? _player.getLoc() : _player.getReflection().getReturnLoc() : _player._stablePoint;
		_currentCp = _player.getCurrentCp();
		_currentHp = _player.getCurrentHp();
		_currentMp = _player.getCurrentMp();
		_classId = _player.getActiveClassId();

		List<Effect> effectList = _player.getEffectList().getAllEffects();
		if (!effectList.isEmpty())
		{
			_effects = new ArrayList<Effect>(effectList.size());
			for(Effect e : effectList)
			{
				if (e.getSkill().getTemplate().isToggle())
					continue;
				Effect effect = e.getTemplate().getEffect(new Env(e.getEffector(), e.getEffected(), e.getSkill()));
				effect.setCount(e.getCount());
				effect.setPeriod(e.getCount() == 1 ? e.getPeriod() - e.getTime() : e.getPeriod());

				_effects.add(effect);
			}
		}
	}

	public void restore()
	{
		if(_player == null)
			return;

		for (Effect e : _player.getEffectList().getAllEffects())
			if (e != null && !e.getSkill().getTemplate().isToggle())
				e.exit();
		if (!_effects.isEmpty() && _player.getActiveClassId() == _classId) // если саб был сменен во время дуэли бафы не восстанавливаем
		{
			int size = _effects.size();
			for (Effect e : _effects)
			{
				_player.getEffectList().addEffect(e);
				e.fixStartTime(size--);
			}
		}

		_player.setCurrentCp(_currentCp);
		_player.setCurrentHpMp(_currentHp, _currentMp);
	}

	public void teleportBack()
	{
		if(_player == null)
			return;

		_player._stablePoint = null;

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				_player.stopFrozen();
				_player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
			}
		}, 5000L);
	}

	public void blockUnblock()
	{
		if(_player == null)
			return;

		_player.block();
		final Servitor servitor = _player.getServitor();
		if(servitor != null)
			servitor.block();

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				_player.unblock();
				if(servitor != null)
					servitor.unblock();
			}
		}, 3000L);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isDead()
	{
		return _isDead;
	}

	public void setDead()
	{
		_isDead = true;
	}

	public Location getLoc()
	{
		return _returnLoc;
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public Location getReturnLoc()
	{
		return _returnLoc;
	}

	public void clear()
	{
		_player = null;
	}
}
