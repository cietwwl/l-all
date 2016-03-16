package org.mmocore.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.instances.SummonInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.stats.Env;


public class EffectDiscord extends Effect
{
	public EffectDiscord(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		int skilldiff = _effected.getLevel() - _skill.getTemplate().getMagicLevel();
		int lvldiff = _effected.getLevel() - _effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
			return false;

		boolean multitargets = _skill.getTemplate().isAoE();

		if(!_effected.isMonster())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		if(_effected.isFearImmune() || _effected.isRaid())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		// Discord нельзя наложить на осадных саммонов
		Player player = _effected.getPlayer();
		if(player != null)
		{
			SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
			if(_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance)_effected))
			{
				if(!multitargets)
					getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}

		if(_effected.isInZonePeace())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startConfused();
		
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		
		if(!_effected.stopConfused())
		{
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			_effected.stopMove();
			_effected.getAI().setAttackTarget(null);
			_effected.setWalking();
			_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		List<Creature> targetList = new ArrayList<Creature>();

		for(Creature character : _effected.getAroundCharacters(900, 200))
			if(character.isNpc() && character != getEffected())
				targetList.add(character);

		// if there is no target, exit function
		if(targetList.isEmpty())
			return true;

		// Choosing randomly a new target
		Creature target = targetList.get(Rnd.get(targetList.size()));

		// Attacking the target
		_effected.setRunning();
		_effected.getAI().Attack(target, true, false);

		return false;
	}
}