package org.mmocore.gameserver.model.entity.events.impl;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.gameserver.listener.actor.OnDeathFromUndyingListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;

/**
 * @author VISTALL
 * @date 18:03/22.08.2011
 *
 * Базовый класс для евентов, которые могу быть на игроке ток в одном виде, толи это Твт, или Оллимпиада, или Ктф, или Дуель
 */
public abstract class SingleMatchEvent extends Event
{
	public class OnDeathFromUndyingListenerImpl implements OnDeathFromUndyingListener
	{
		@Override
		public void onDeathFromUndying(Creature actor, Creature killer)
		{
			onDie((Player)actor);
		}
	}

	protected SingleMatchEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected SingleMatchEvent(int id, String name)
	{
		super(id, name);
	}

	/**
	 * Проверяет может ли флагаться владелец эвента при атаке цели. Участники одной дуэли не флагаются.
	 */
	public boolean checkPvPFlag(Creature target)
	{
		final SingleMatchEvent targetEvent = target.getEvent(SingleMatchEvent.class);
		return targetEvent != this;
	}

	public void onStatusUpdate(Player player)
	{}

	public void onEffectIconsUpdate(Player player, Effect[] effects)
	{}

	public void onDie(Player player)
	{}

	public void sendPacket(IBroadcastPacket packet)
	{}

	public void sendPackets(IBroadcastPacket... packet)
	{}
}
