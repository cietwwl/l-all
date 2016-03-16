package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

public class ReflectionBossInstance extends RaidBossInstance
{
	private final static int COLLAPSE_AFTER_DEATH_TIME = 5; // 5 мин

	private boolean _isDropLocked = false;

	public ReflectionBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected int getMinChannelSizeForLock()
	{
		return 0;
	}

	@Override
	protected void onChannelLock(String leaderName)
	{
		super.onChannelLock(leaderName);
		_isDropLocked = true;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if (hasMinions())
			getMinionList().unspawnMinions();
		super.onDeath(killer);
		clearReflection();
	}

	/**
	 * Удаляет все спауны из рефлекшена и запускает 5ти минутный коллапс-таймер.
	 */
	protected void clearReflection()
	{
		getReflection().clearReflection(_isDropLocked ? COLLAPSE_AFTER_DEATH_TIME + 1 : COLLAPSE_AFTER_DEATH_TIME, true);
		_isDropLocked = false;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}
}