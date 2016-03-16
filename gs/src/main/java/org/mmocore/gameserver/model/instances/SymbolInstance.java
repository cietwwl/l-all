package org.mmocore.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObjectTasks;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.taskmanager.EffectTaskManager;
import org.mmocore.gameserver.templates.npc.NpcTemplate;


public class SymbolInstance extends NpcInstance
{
	private final Creature _owner;
	private final SkillEntry _skill;
	private ScheduledFuture<?> _targetTask;
	private ScheduledFuture<?> _destroyTask;

	public SymbolInstance(int objectId, NpcTemplate template, Creature owner, SkillEntry skill)
	{
		super(objectId, template);
		_owner = owner;
		_skill = skill;

		setReflection(owner.getReflection());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
	}

	@Override
	public Player getPlayer()
	{
		return _owner != null ? _owner.getPlayer() : null;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000L);

		_targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				for(Creature target : getAroundCharacters(200, 200))
					if(_skill.checkTarget(SymbolInstance.this, target, null, false, false) == null)
					{
						List<Creature> targets = new ArrayList<Creature>();

						if(!_skill.getTemplate().isAoE())
							targets.add(target);
						else
							for(Creature t : getAroundCharacters(_skill.getTemplate().getSkillRadius(), 128))
								if(_skill.checkTarget(SymbolInstance.this, t, null, false, false) == null)
									targets.add(target);

						_skill.useSkill(SymbolInstance.this, targets);
					}
			}
		}, 1000L, Rnd.get(4000L, 7000L));
	}

	@Override
	protected void onDelete()
	{
		if(_destroyTask != null)
			_destroyTask.cancel(false);
		_destroyTask = null;
		if(_targetTask != null)
			_targetTask.cancel(false);
		_targetTask = null;
		super.onDelete();
	}

	@Override
	public int getPAtk(Creature target)
	{
		return _owner != null ? _owner.getPAtk(target) : 0;
	}

	@Override
	public int getMAtk(Creature target, SkillEntry skill)
	{
		return _owner != null ? _owner.getMAtk(target, skill) : 0;
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
	public boolean isTargetable()
	{
		return false;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
