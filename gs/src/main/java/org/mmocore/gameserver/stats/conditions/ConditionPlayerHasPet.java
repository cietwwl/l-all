package org.mmocore.gameserver.stats.conditions;

import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.stats.Env;

public class ConditionPlayerHasPet extends Condition
{
	private final int[] _petIds;

	public ConditionPlayerHasPet(String[] petIds)
	{
		if (petIds.length == 1 && petIds[0].isEmpty())
			_petIds = ArrayUtils.EMPTY_INT_ARRAY;
		else
		{
			_petIds = new int[petIds.length];
			for (int i = 0; i < petIds.length; i++)
				_petIds[i] = Integer.parseInt(petIds[i]);
		}
	}

	@Override
	protected boolean testImpl(Env env)
	{
		final Servitor pet = env.character.isServitor() ? (Servitor)env.character : env.character.isPlayer() ? ((Player)env.character).getServitor() : null;
		if (pet == null || !pet.isPet())
			return false;

		if (_petIds.length == 0)
			return true;

		return ArrayUtils.contains(_petIds, pet.getNpcId());
	}
}