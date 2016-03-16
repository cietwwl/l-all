package org.mmocore.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.network.l2.s2c.AutoAttackStart;
import org.mmocore.gameserver.network.l2.s2c.CharInfo;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

public class DecoyInstance extends NpcInstance
{
	private HardReference<Player> _playerRef;
	private int _timeRemaining;
	private ScheduledFuture<?> _decoyLifeTask, _hateSpam;

	public DecoyInstance(int objectId, NpcTemplate template, Player owner, int lifeTime)
	{
		super(objectId, template);

		setUndying(SpecialEffectState.FALSE);
		_playerRef = owner.getRef();
		_timeRemaining = lifeTime;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_decoyLifeTask = ThreadPoolManager.getInstance().schedule(new DecoyLifetime(), _timeRemaining);
		int skilllevel = getNpcId() < 13257 ? getNpcId() - 13070 : getNpcId() - 13250;
		_hateSpam = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HateSpam(SkillTable.getInstance().getSkillEntry(5272, skilllevel)), 1000, 3000);
	}
	
	@Override
	protected void onDespawn()
	{		
		if(_decoyLifeTask != null)
		{
			_decoyLifeTask.cancel(false);
			_decoyLifeTask = null;
		}
		if(_hateSpam != null)
		{
			_hateSpam.cancel(false);
			_hateSpam = null;
		}
		
		Player owner = getPlayer();
		if(owner != null)
			owner.setDecoy(null);
		
		super.onDespawn();
	}
	
	@Override
	protected void onDeath(Creature killer)
	{		
		if(_decoyLifeTask != null)
		{
			_decoyLifeTask.cancel(false);
			_decoyLifeTask = null;
		}
		if(_hateSpam != null)
		{
			_hateSpam.cancel(false);
			_hateSpam = null;
		}
		
		super.onDeath(killer);
	}

	private class DecoyLifetime extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
				deleteMe();
		}
	}

	private class HateSpam extends RunnableImpl
	{
		private SkillEntry _skill;

		HateSpam(SkillEntry skill)
		{
			_skill = skill;
		}

		@Override
		public void runImpl() throws Exception
		{
				setTarget(DecoyInstance.this);
				doCast(_skill, DecoyInstance.this, true);
		}
	}
	
	@Override
	public Player getPlayer()
	{
		return _playerRef.get();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player owner = getPlayer();
		return owner != null && owner.isAutoAttackable(attacker);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		Player owner = getPlayer();
		return owner != null && owner.isAttackable(attacker);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
	}

	@Override
	public double getColRadius()
	{
		Player player = getPlayer();
		if(player == null)
			return 0;
		if(player.getTransformation() != 0 && player.getTransformationTemplate() != 0)
			return NpcHolder.getInstance().getTemplate(player.getTransformationTemplate()).collisionRadius;
		return player.getBaseTemplate().collisionRadius;
	}

	@Override
	public double getColHeight()
	{
		Player player = getPlayer();
		if(player == null)
			return 0;
		if(player.getTransformation() != 0 && player.getTransformationTemplate() != 0)
			return NpcHolder.getInstance().getTemplate(player.getTransformationTemplate()).collisionHeight;
		return player.getBaseTemplate().collisionHeight;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(!isInCombat())
			return Collections.<L2GameServerPacket>singletonList(new CharInfo(this));
		else
		{
			List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>(2);
			list.add(new CharInfo(this));
			list.add(new AutoAttackStart(getObjectId()));
			return list;
		}
	}
}