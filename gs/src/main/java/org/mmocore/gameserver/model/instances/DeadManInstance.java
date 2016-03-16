package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.ai.CharacterAI;
import org.mmocore.gameserver.network.l2.s2c.Die;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

public class DeadManInstance extends NpcInstance
{
	public DeadManInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setAI(new CharacterAI(this));
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setCurrentHp(0, false);
		broadcastPacket(new Die(this));
		setWalking();
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}
	
	@Override
	public boolean isBlocked()
	{
		return true;
	}
}