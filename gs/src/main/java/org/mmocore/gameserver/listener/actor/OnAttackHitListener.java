package org.mmocore.gameserver.listener.actor;

import org.mmocore.gameserver.listener.CharListener;
import org.mmocore.gameserver.model.Creature;

public interface OnAttackHitListener extends CharListener
{
	public void onAttackHit(Creature actor, Creature attacker);
}
