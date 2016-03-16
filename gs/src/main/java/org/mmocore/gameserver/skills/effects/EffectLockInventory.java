package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.LockType;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 14:13/16.05.2011
 */
public class EffectLockInventory extends Effect
{
	private LockType _lockType;
	private int[] _lockItems;

	public EffectLockInventory(Env env, EffectTemplate template)
	{
		super(env, template);
		_lockType = template.getParam().getEnum("lockType", LockType.class);
		_lockItems  = template.getParam().getIntegerArray("lockItems");
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Player player = _effector.getPlayer();

		player.getInventory().lockItems(_lockType, _lockItems);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		Player player = _effector.getPlayer();

		player.getInventory().unlock();
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}
