package org.mmocore.gameserver.skills.effects;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.ChangeWaitType;
import org.mmocore.gameserver.network.l2.s2c.Revive;
import org.mmocore.gameserver.stats.Env;

public final class EffectFakeDeath extends EffectManaDamOverTime
{
	public static final int FAKE_DEATH_OFF = 0;
	public static final int FAKE_DEATH_ON = 1;
	public static final int FAKE_DEATH_FAILED = 2;

	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!getEffected().isPlayer())
			return false;

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		final Player player = (Player) getEffected();
		player.abortAttack(true, false);
		if (player.isMoving)
			player.stopMove();
		if (Rnd.chance(getTemplate().chance()))
		{
			player.setFakeDeath(FAKE_DEATH_ON);
			player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		}
		else
			player.setFakeDeath(FAKE_DEATH_FAILED);
		player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
		player.broadcastCharInfo();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		// 5 секунд после FakeDeath на персонажа не агрятся мобы
		final Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		player.setFakeDeath(FAKE_DEATH_OFF);
		player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
		player.broadcastPacket(new Revive(player));
		player.broadcastCharInfo();
	}
}