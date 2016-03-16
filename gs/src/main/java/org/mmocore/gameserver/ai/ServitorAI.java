package org.mmocore.gameserver.ai;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.skills.SkillEntry;

public class ServitorAI extends PlayableAI
{
	private CtrlIntention _storedIntention = null;
	private Object _storedIntentionArg0 = null;
	private Object _storedIntentionArg1 = null;
	private boolean _storedForceUse = false;

	public ServitorAI(Servitor actor)
	{
		super(actor);
	}

	public void storeIntention()
	{
		if (_storedIntention == null)
		{
			_storedIntention = getIntention();
			_storedIntentionArg0 = _intention_arg0;
			_storedIntentionArg1 = _intention_arg1;
			_storedForceUse = _forceUse;
		}
	}

	public boolean restoreIntention()
	{
		final CtrlIntention intention = _storedIntention;
		final Object arg0 = _storedIntentionArg0;
		final Object arg1 = _storedIntentionArg1;
		if (intention != null)
		{
			_forceUse = _storedForceUse;
			setIntention(intention, arg0, arg1);
			clearStoredIntention();

			onEvtThink();
			return true;
		}
		return false;
	}

	public void clearStoredIntention()
	{
		_storedIntention = null;
		_storedIntentionArg0 = null;
		_storedIntentionArg1 = null;
	}

	@Override
	protected void onIntentionIdle()
	{
		clearStoredIntention();
		super.onIntentionIdle();
	}

	@Override
	protected void onEvtFinishCasting(SkillEntry skill)
	{
		if (!restoreIntention())
			super.onEvtFinishCasting(skill);
	}

	@Override
	protected void thinkActive()
	{
		Servitor actor = getActor();

		clearNextAction();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			thinkAttack(true);
		}
		else if(actor.isFollowMode() && !actor.isAfraid())
		{
			changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), Config.FOLLOW_RANGE);
			thinkFollow();
		}

		super.thinkActive();
	}

	@Override
	protected void thinkAttack(boolean checkRange)
	{
		Servitor actor = getActor();

		if(actor.isDepressed())
			setAttackTarget(actor.getPlayer());

		super.thinkAttack(checkRange);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, SkillEntry skill, int damage)
	{
		Servitor actor = getActor();
		if(attacker != null && actor.getPlayer().isDead() && !actor.isDepressed())
			Attack(attacker, false, false);
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	public void Cast(SkillEntry skillEntry, Creature target, boolean forceUse, boolean dontMove)
	{
		storeIntention();
		super.Cast(skillEntry, target, forceUse, dontMove);
	}

	@Override
	public Servitor getActor()
	{
		return (Servitor) super.getActor();
	}
}