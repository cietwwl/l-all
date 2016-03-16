package org.mmocore.gameserver.model.entity.olympiad;

import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.listener.actor.OnCurrentHpDamageListener;
import org.mmocore.gameserver.listener.actor.OnDeathFromUndyingListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.skills.SkillEntry;

/**
 * @author VISTALL
 * @date 21:08/13.03.2012
 */
public class OlympiadPlayerListeners implements OnCurrentHpDamageListener, OnDeathFromUndyingListener
{
	@Override
	public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, SkillEntry skill, boolean crit)
	{
		if (!actor.isPlayer())
			return;

		// считаем дамаг от простых ударов и атакующих скиллов
		if (actor == attacker || (skill != null && !skill.getTemplate().isOffensive()))
			return;

		final Player player = (Player)actor;
		if (!player.isInOlympiadMode())
			return;

		final OlympiadGame game = player.getOlympiadGame();
		if (game == null)
			return;

		game.addDamage(player, Math.min(actor.getCurrentHp(), damage));
	}

	@Override
	public void onDeathFromUndying(Creature actor, Creature killer)
	{
		if (!actor.isPlayer())
			return;

		final Player player = (Player)actor;
		if (!player.isInOlympiadMode())
			return;

		final OlympiadGame game = player.getOlympiadGame();
		if (game == null)
			return;

		if(game.getType() != CompType.TEAM || game.doDie(player)) // Все умерли
		{
			game.setWinner(player.getOlympiadSide() == 1 ? 2 : 1);
			game.endGame(20, false);
		}
		else
		{
			player.startFrozen();
			player.stopAttackStanceTask();
			player.sendChanges();
			Servitor servitor = player.getServitor();
			if (servitor != null)
			{
				servitor.startFrozen();
				servitor.stopAttackStanceTask();
				servitor.sendChanges();
			}
		}

		killer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		killer.sendActionFailed();
	}
}
