package org.mmocore.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObjectTasks;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill.SkillTargetType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;
import org.mmocore.gameserver.network.l2.s2c.NpcInfo;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.taskmanager.EffectTaskManager;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;


public final class TrapInstance extends NpcInstance
{
	private static class CastTask extends RunnableImpl
	{
		private HardReference<NpcInstance> _trapRef;

		public CastTask(TrapInstance trap)
		{
			_trapRef = trap.getRef();
		}

		@Override
		public void runImpl() throws Exception
		{
			TrapInstance trap = (TrapInstance)_trapRef.get();

			if(trap == null)
				return;

			Creature owner =  trap.getOwner();
			if(owner == null)
				return;

			for(Creature target : trap.getAroundCharacters(200, 200))
				if(target != owner)
					if(trap._skill.checkTarget(owner, target, null, false, false) == null)
					{
						List<Creature> targets = new ArrayList<Creature>();
						if(trap._skill.getTemplate().getTargetType() != SkillTargetType.TARGET_AREA)
							targets.add(target);
						else
							for(Creature t : trap.getAroundCharacters(trap._skill.getTemplate().getSkillRadius(), 128))
								if(trap._skill.checkTarget(owner, t, null, false, false) == null)
									targets.add(target);

						trap._skill.useSkill(trap, targets);
						if(target.isPlayer())
							target.sendMessage(new CustomMessage("common.Trap"));
						trap.deleteMe();
						break;
					}
		}
	}
	private final HardReference<? extends Creature> _ownerRef;
	private final SkillEntry _skill;
	private ScheduledFuture<?> _targetTask;
	private ScheduledFuture<?> _destroyTask;
	private boolean _detected;

	public TrapInstance(int objectId, NpcTemplate template, Creature owner, SkillEntry skill)
	{
		this(objectId, template, owner, skill, owner.getLoc());
	}

	public TrapInstance(int objectId, NpcTemplate template, Creature owner, SkillEntry skill, Location loc)
	{
		super(objectId, template);
		_ownerRef = owner.getRef();
		_skill = skill;

		setReflection(owner.getReflection());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
		setLoc(loc);
	}

	@Override
	public boolean isTrap()
	{
		return true;
	}

	public Creature getOwner()
	{
		return _ownerRef.get();
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000L);

		_targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new CastTask(this), 250L, 250L);
	}

	@Override
	public void broadcastCharInfo()
	{
		if (!isDetected())
			return;
		super.broadcastCharInfo();
	}

	@Override
	protected void onDelete()
	{
		Creature owner = getOwner();
		if(owner != null && owner.isPlayer())
			((Player)owner).removeTrap(this);
		if(_destroyTask != null)
			_destroyTask.cancel(false);
		_destroyTask = null;
		if(_targetTask != null)
			_targetTask.cancel(false);
		_targetTask = null;
		super.onDelete();
	}

	public boolean isDetected()
	{
		return _detected;
	}

	public void setDetected(boolean detected)
	{
		_detected = detected;
	}

	@Override
	public int getPAtk(Creature target)
	{
		Creature owner = getOwner();
		return owner == null ? 0 : owner.getPAtk(target);
	}

	@Override
	public int getMAtk(Creature target, SkillEntry skill)
	{
		Creature owner = getOwner();
		return owner == null ? 0 : owner.getMAtk(target, skill);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{}

	@Override
	public void showChatWindow(Player player, String filename, Object... replace)
	{}

	@Override
	public void onBypassFeedback(Player player, String command)
	{}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
		}
		player.sendActionFailed();
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		// если не обезврежена и не овнер, ниче не показываем
		if(!isDetected() && getOwner() != forPlayer)
			return Collections.emptyList();

		return Collections.<L2GameServerPacket>singletonList(new NpcInfo(this, forPlayer));
	}

	@Override
	public Player getPlayer()
	{
		final Creature owner = getOwner();
		return (owner != null && owner.isPlayer()) ? (Player)owner : null;
	}
}