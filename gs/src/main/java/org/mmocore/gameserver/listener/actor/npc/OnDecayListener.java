package org.mmocore.gameserver.listener.actor.npc;

import org.mmocore.gameserver.listener.NpcListener;
import org.mmocore.gameserver.model.instances.NpcInstance;

public interface OnDecayListener extends NpcListener
{
	public void onDecay(NpcInstance actor);
}
