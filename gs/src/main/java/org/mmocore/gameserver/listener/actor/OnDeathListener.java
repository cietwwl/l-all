package org.mmocore.gameserver.listener.actor;

import org.mmocore.gameserver.listener.CharListener;
import org.mmocore.gameserver.model.Creature;

public interface OnDeathListener extends CharListener
{
	public void onDeath(Creature actor, Creature killer);
}
