package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.stats.Env;

public final class EffectCharge extends Effect
{
	public static final int MAX_CHARGE = 8;

	private final int _charges;
	private final boolean _fullCharge;

	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
		_charges = template.getParam().getInteger("charges", MAX_CHARGE);
		_fullCharge = template.getParam().getBool("fullCharge", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (getEffected().isPlayer())
		{
			final Player player = (Player)getEffected();

			if (player.getIncreasedForce() >= _charges)
				player.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
			else if (_fullCharge)
				player.setIncreasedForce(_charges);
			else
				player.setIncreasedForce(player.getIncreasedForce() + 1);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
