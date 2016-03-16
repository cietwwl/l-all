package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 8:13/31.01.2011
 */
public class ConditionPlayerResidence extends Condition
{
	private final int _id;
	private final Class<? extends Residence> _type;

	@SuppressWarnings("unchecked")
	public ConditionPlayerResidence(int id, String type)
	{
		_id = id;
		try
		{
			_type = (Class<? extends Residence>) Class.forName("org.mmocore.gameserver.model.entity.residence." + type);
		}
		catch(ClassNotFoundException e)
		{
			throw new Error(e);
		}
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		Player player = (Player)env.character;
		Clan clan = player.getClan();
		if(clan == null)
			return false;

		int residenceId = clan.getResidenceId(_type);

		return _id > 0 ? residenceId == _id : residenceId > 0;
	}
}
