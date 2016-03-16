package org.mmocore.gameserver.listener.actor.npc;

import org.mmocore.gameserver.listener.NpcListener;
import org.mmocore.gameserver.model.instances.NpcInstance;

public interface OnSpawnListener extends NpcListener
{
	public void onSpawn(NpcInstance actor);
}
