package org.mmocore.gameserver.ai;

import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static org.mmocore.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;

import java.util.concurrent.ScheduledFuture;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.Skill.NextAction;
import org.mmocore.gameserver.model.Skill.SkillType;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.FlyToLocation.FlyType;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.utils.Location;

public class PlayableAI extends CharacterAI
{
	private volatile int thinking = 0; // to prevent recursive thinking

	protected Object _intention_arg0 = null, _intention_arg1 = null;
	protected SkillEntry _skillEntry;

	private nextAction _nextAction;
	private Object _nextAction_arg0;
	private Object _nextAction_arg1;
	private boolean _nextAction_arg2;
	private boolean _nextAction_arg3;

	protected boolean _forceUse;
	private boolean _dontMove;

	private ScheduledFuture<?> _followTask;

	public PlayableAI(Playable actor)
	{
		super(actor);
	}

	public enum nextAction
	{
		ATTACK,
		CAST,
		MOVE,
		REST,
		PICKUP,
		EQUIP,
		INTERACT,
		COUPLE_ACTION
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}

	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}

	@Override
	protected void onIntentionCast(SkillEntry skill, Creature target)
	{
		_skillEntry = skill;
		super.onIntentionCast(skill, target);
	}

	@Override
	public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{
		_nextAction = action;
		_nextAction_arg0 = arg0;
		_nextAction_arg1 = arg1;
		_nextAction_arg2 = arg2;
		_nextAction_arg3 = arg3;
	}

	public boolean setNextIntention()
	{
		nextAction nextAction = _nextAction;
		Object nextAction_arg0 = _nextAction_arg0;
		Object nextAction_arg1 = _nextAction_arg1;
		boolean nextAction_arg2 = _nextAction_arg2;
		boolean nextAction_arg3 = _nextAction_arg3;

		Playable actor = getActor();
		if(nextAction == null || actor.isActionsDisabled())
			return false;

		SkillEntry skill;
		Creature target;
		GameObject object;

		switch(nextAction)
		{
			case ATTACK:
				if(!(nextAction_arg0 instanceof Creature))
					return false;
				target = (Creature) nextAction_arg0;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				setIntention(AI_INTENTION_ATTACK, target);
				break;
			case CAST:
				if(!(nextAction_arg0 instanceof SkillEntry && nextAction_arg1 instanceof Creature))
					return false;
				skill = (SkillEntry) nextAction_arg0;
				target = (Creature) nextAction_arg1;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				if(!skill.checkCondition(actor, target, _forceUse, _dontMove, true))
				{
					if(!_forceUse && skill.getTemplate().getNextAction() == NextAction.ATTACK && target.isAutoAttackable(actor))
					{
						setNextAction(PlayableAI.nextAction.ATTACK, target, null, _forceUse, false);
						return setNextIntention();
					}
					return false;
				}
				setIntention(AI_INTENTION_CAST, skill, target);
				break;
			case MOVE:
				if(!(nextAction_arg0 instanceof Location && nextAction_arg1 instanceof Integer))
					return false;
				Location loc = (Location) nextAction_arg0;
				int offset = (Integer) nextAction_arg1;
				clearNextAction();
				actor.moveToLocation(loc, offset, nextAction_arg2);
				break;
			case REST:
				actor.sitDown(null);
				break;
			case INTERACT:
				if(!(nextAction_arg0 instanceof GameObject))
					return false;
				object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionInteract(object);
				break;
			case PICKUP:
				if(!(nextAction_arg0 instanceof GameObject))
					return false;
				object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionPickUp(object);
				break;
			case EQUIP:
				if(!(nextAction_arg0 instanceof ItemInstance))
					return false;
				ItemInstance item = (ItemInstance)nextAction_arg0;
				if (item.isEquipable())
					item.getTemplate().getHandler().useItem(getActor(), item, nextAction_arg2);
				clearNextAction();
				if (getIntention() == AI_INTENTION_ATTACK) // autoattack not aborted
					return false;
				break;
			case COUPLE_ACTION:
				if(!(nextAction_arg0 instanceof Creature && nextAction_arg1 instanceof Integer))
					return false;
				target = (Creature)nextAction_arg0;
				int socialId = (Integer)nextAction_arg1;
				_forceUse = nextAction_arg2;
				_nextAction = null;
				clearNextAction();
				onIntentionCoupleAction((Player)target, socialId);
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public void clearNextAction()
	{
		_nextAction = null;
		_nextAction_arg0 = null;
		_nextAction_arg1 = null;
		_nextAction_arg2 = false;
		_nextAction_arg3 = false;
	}

	@Override
	protected void onEvtFinishCasting(SkillEntry skill)
	{
		if(!setNextIntention())
			setIntention(AI_INTENTION_ACTIVE);
	}

	@Override
	protected void onEvtReadyToAct()
	{
		if(!setNextIntention())
			onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
			if(getIntention() == AI_INTENTION_INTERACT || getIntention() == AI_INTENTION_PICK_UP)
				onEvtThink();
			else
				changeIntention(AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		switch(getIntention())
		{
			case AI_INTENTION_ATTACK:
				thinkAttack(true);
				break;
			case AI_INTENTION_CAST:
				thinkCast(true);
				break;
			case AI_INTENTION_FOLLOW:
				thinkFollow();
				break;
			default:
				onEvtThink();
				break;
		}
	}

	@Override
	protected final void onEvtThink()
	{
		Playable actor = getActor();
		if(actor.isActionsDisabled())
			return;

		try
		{
			if(thinking++ > 1)
				return;

			switch(getIntention())
			{
				case AI_INTENTION_ACTIVE:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack(false);
					break;
				case AI_INTENTION_CAST:
					thinkCast(false);
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
				case AI_INTENTION_FOLLOW:
					thinkFollow();
					break;
				case AI_INTENTION_COUPLE_ACTION:
					thinkCoupleAction((Player)_intention_arg0, (Integer)_intention_arg1, false);
					break;
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			thinking--;
		}
	}

	protected void thinkActive()
	{

	}

	protected void thinkFollow()
	{
		Playable actor = getActor();

		Creature target = (Creature) _intention_arg0;
		Integer offset = (Integer) _intention_arg1;

		//Находимся слишком далеко цели, либо цель не пригодна для следования
		if(target == null || actor.getDistance(target) > 4000 || offset == null)
		{
			clientActionFailed();
			return;
		}

		//Уже следуем за этой целью
		if(actor.isFollow && actor.getFollowTarget() == target)
		{
			clientActionFailed();
			return;
		}

		//Находимся достаточно близко или не можем двигаться - побежим потом ?
		if(actor.isInRange(target, offset + 20) || actor.isMovementDisabled())
			clientActionFailed();

		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		_followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 250L);
	}

	protected class ThinkFollow extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Playable actor = getActor();

			if(getIntention() != AI_INTENTION_FOLLOW)
			{
				// Если пет прекратил преследование, меняем статус, чтобы не пришлось щелкать на кнопку следования 2 раза.
				if(actor.isServitor() && getIntention() == AI_INTENTION_ACTIVE)
					((Servitor) actor).setFollowMode(false);
				return;
			}

			if (!(_intention_arg0 instanceof Creature))
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}

			Creature target = (Creature) _intention_arg0;
			if(actor.getDistance(target) > 4000)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}

			Player player = actor.getPlayer();
			if(player == null || player.isLogoutStarted() || actor.isServitor() && player.getServitor() != actor)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}

			int offset = _intention_arg1 instanceof Integer ? (Integer) _intention_arg1 : 0;

			if(!actor.isAfraid() && !actor.isInRange(target, offset + 20) && (!actor.isFollow || actor.getFollowTarget() != target))
				actor.followToCharacter(target, offset, false);
			_followTask = ThreadPoolManager.getInstance().schedule(this, 250L);
		}
	}

	protected class ExecuteFollow extends RunnableImpl
	{
		private Creature _target;
		private int _range;

		public ExecuteFollow(Creature target, int range)
		{
			_target = target;
			_range = range;
		}

		@Override
		public void runImpl()
		{
			if(_target.isDoor())
				_actor.moveToLocation(_target.getLoc(), 40, true);
			else
				_actor.followToCharacter(_target, _range, false);
		}
	}

	@Override
	protected void onIntentionInteract(GameObject object)
	{
		Playable actor = getActor();

		if(actor.isActionsDisabled())
		{
			setNextAction(nextAction.INTERACT, object, null, false, false);
			clientActionFailed();
			return;
		}

		clearNextAction();
		changeIntention(AI_INTENTION_INTERACT, object, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCoupleAction(Player player, Integer socialId)
	{
		_nextAction = null;
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, player, socialId);
		onEvtThink();
	}

	protected void thinkInteract()
	{
		Playable actor = getActor();

		GameObject target = (GameObject) _intention_arg0;

		if(target == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		int range = (int) (Math.max(30, actor.getMinDistance(target)) + 20);

		if(actor.isInRangeZ(target, range))
		{
			if(actor.isPlayer())
				((Player) actor).doInteract(target);
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			actor.moveToLocation(target.getLoc(), 40, true);
			setNextAction(nextAction.INTERACT, target, null, false, false);
		}
	}

	@Override
	protected void onIntentionPickUp(GameObject object)
	{
		Playable actor = getActor();

		if(actor.isActionsDisabled())
		{
			setNextAction(nextAction.PICKUP, object, null, false, false);
			clientActionFailed();
			return;
		}

		clearNextAction();
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		onEvtThink();
	}

	protected void thinkPickUp()
	{
		final Playable actor = getActor();

		final GameObject target = (GameObject) _intention_arg0;

		if(target == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		if(actor.isInRange(target, 30) && Math.abs(actor.getZ() - target.getZ()) < 50)
		{
			if(actor.isPlayer() || actor.isPet())
				actor.doPickupItem(target);
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					actor.moveToLocation(target.getLoc(), 10, true);
					setNextAction(nextAction.PICKUP, target, null, false, false);
				}
			});
	}

	protected void thinkAttack(boolean arrived)
	{
		Playable actor = getActor();

		Player player = actor.getPlayer();
		if(player == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		if(actor.isActionsDisabled() || actor.isAttackingDisabled())
		{
			actor.sendActionFailed();
			return;
		}

		boolean isPosessed = actor instanceof Servitor && ((Servitor) actor).isDepressed();

		Creature attack_target = getAttackTarget();
		if(attack_target == null || attack_target.isDead() || !isPosessed && !(_forceUse ? attack_target.isAttackable(actor) : attack_target.isAutoAttackable(actor)))
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		int range = actor.getPhysicalAttackRange();
		if(range < 10)
			range = 10;

		boolean canSee = GeoEngine.canSeeTarget(actor, attack_target, false);

		if(!canSee && (range > 200 || Math.abs(actor.getZ() - attack_target.getZ()) > 200))
		{
			actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		range += actor.getMinDistance(attack_target);

		if(actor.isInRangeZ(attack_target, arrived ? range + 40 : range))
		{
			if(!canSee)
			{
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				setIntention(AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return;
			}

			clientStopMoving(false);
			actor.doAttack(attack_target);
		}
		else if(!_dontMove)
			ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, Math.max(range - 30, 10)));
		else
			actor.sendActionFailed();
	}

	protected void thinkCast(boolean arrived)
	{
		Playable actor = getActor();

		Creature target = getAttackTarget();

		Skill skill = _skillEntry.getTemplate();
		if(skill.getSkillType() == SkillType.CRAFT || skill.isToggle())
		{
			if(_skillEntry.checkCondition(actor, target, _forceUse, _dontMove, true))
				actor.doCast(_skillEntry, target, _forceUse);
			return;
		}

		if(target == null || target.isDead() != skill.getCorpse() && !skill.isNotTargetAoE())
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		int range = actor.getMagicalAttackRange(_skillEntry);
		if(range < 10)
			range = 10;

		boolean canSee = skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || GeoEngine.canSeeTarget(actor, target, actor.isFlying());
		canSee &= !(skill.getFlyType() == FlyType.DUMMY || skill.getFlyType() == FlyType.CHARGE) || actor.getFlyLocation(target, skill) != null;
		boolean noRangeSkill = skill.getCastRange() == 32767;

		if(!noRangeSkill && !canSee && (range > 200 || Math.abs(actor.getZ() - target.getZ()) > 200))
		{
			actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		range += actor.getMinDistance(target);

		if(actor.isInRangeZ(target, arrived ? range + 40 : range) || noRangeSkill)
		{
			if(!noRangeSkill && !canSee)
			{
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				setIntention(AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return;
			}

			// Если скилл имеет следующее действие, назначим это действие после окончания действия скилла
			if(!_forceUse && skill.getNextAction() == NextAction.ATTACK && target.isAutoAttackable(actor))
				setNextAction(nextAction.ATTACK, target, null, _forceUse, false);
			else
				clearNextAction();

			if(_skillEntry.checkCondition(actor, target, _forceUse, _dontMove, true))
			{
				if (skill.stopActor())
					clientStopMoving(false);
				actor.doCast(_skillEntry, target, _forceUse);
			}
			else
			{
				setNextIntention();
				if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					thinkAttack(false);
			}
		}
		else if(!_dontMove)
			ThreadPoolManager.getInstance().execute(new ExecuteFollow(target, Math.max(range - 30, 10)));
		else
		{
			actor.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
		}
	}

	protected void thinkCoupleAction(Player target, Integer socialId, boolean cancel)
	{
		//
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		clearNextAction();
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clearNextAction();
		super.onEvtFakeDeath();
	}

	public void lockTarget(Creature target)
	{
		Playable actor = getActor();

		if(target == null || target.isDead())
			actor.setAggressionTarget(null);
		else if(actor.getAggressionTarget() == null)
		{
			GameObject actorStoredTarget = actor.getTarget();
			actor.setAggressionTarget(target);
			actor.setTarget(target);

			clearNextAction();
			// DS: агрессия только перекидывает видимую цель, но не обрывает текущую атаку/каст
			/*if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				setAttackTarget(target);
			switch(getIntention())
			{
				case AI_INTENTION_ATTACK:
					setAttackTarget(target);
					break;
				case AI_INTENTION_CAST:
					L2Skill skill = actor.getCastingSkill();
					if(skill == null)
						skill = _skill;
					if(skill != null && !skill.isUsingWhileCasting())
						switch(skill.getTargetType())
						{
							case TARGET_ONE:
							case TARGET_AREA:
							case TARGET_MULTIFACE:
							case TARGET_TUNNEL:
								setAttackTarget(target);
								actor.setCastingTarget(target);
								break;
						}
					break;
			}*/

			if(actorStoredTarget != target)
				actor.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		}
	}

	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();

		if(target.isCreature())
		{
			if (actor.isActionsDisabled() || actor.isAttackingDisabled())
			{
				// Если не можем атаковать или лук еще не перезарядился, то атаковать позже
				if (actor.isMoving)
					clientStopMoving();
				setNextAction(nextAction.ATTACK, target, null, forceUse, false);
				actor.sendActionFailed();
				return;
			}
		}

		_dontMove = dontMove;
		_forceUse = forceUse;
		clearNextAction();
		setIntention(AI_INTENTION_ATTACK, target);
	}

	@Override
	public void Cast(SkillEntry skillEntry, Creature target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();

		Skill skill = skillEntry.getTemplate();
		// Если скилл альтернативного типа (например, бутылка на хп),
		// то он может использоваться во время каста других скиллов, или во время атаки, или на бегу.
		// Поэтому пропускаем дополнительные проверки.
		if(skill.altUse() || skill.isToggle())
		{
			if((skill.isToggle() || skill.isHandler()) && (actor.isOutOfControl() || actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isDead()))
				clientActionFailed();
			else
				actor.altUseSkill(skillEntry, target);
			return;
		}

		// Если не можем кастовать, то использовать скилл позже
		if(actor.isActionsDisabled())
		{
			setNextAction(nextAction.CAST, skillEntry, target, forceUse, dontMove);
			clientActionFailed();
			return;
		}

		_forceUse = forceUse;
		_dontMove = dontMove;
		clearNextAction();
		setIntention(CtrlIntention.AI_INTENTION_CAST, skillEntry, target);
	}

	@Override
	public Playable getActor()
	{
		return (Playable) super.getActor();
	}
}