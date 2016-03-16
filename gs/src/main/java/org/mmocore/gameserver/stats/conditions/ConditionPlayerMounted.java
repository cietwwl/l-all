package org.mmocore.gameserver.stats.conditions;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerMounted extends Condition
{
	private int[] _mountIds;

	public ConditionPlayerMounted(String[] mountIds)
	{
		if (mountIds.length == 1 && mountIds[0].isEmpty())
			_mountIds = ArrayUtils.EMPTY_INT_ARRAY;
		else
		{
			_mountIds = new int[mountIds.length];
			for (int i = 0; i < mountIds.length; i++)
				_mountIds[i] = Integer.parseInt(mountIds[i]);
		}
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer() || !((Player)env.character).isMounted())
			return false;

		if (_mountIds.length == 0)
			return true;

		return ArrayUtils.contains(_mountIds, ((Player)env.character).getMountNpcId());
	}
}
