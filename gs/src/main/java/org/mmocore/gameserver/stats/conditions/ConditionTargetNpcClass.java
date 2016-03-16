package org.mmocore.gameserver.stats.conditions;

import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.scripts.Scripts;
import org.mmocore.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 3:38/17.05.2011
 */
@SuppressWarnings("unchecked")
public class ConditionTargetNpcClass extends Condition
{
	private final Class<NpcInstance> _npcClass;

	public ConditionTargetNpcClass(String name)
	{
		Class<NpcInstance> classType = null;
		try
		{
			classType = (Class<NpcInstance>) Class.forName("org.mmocore.gameserver.model.instances." + name + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + name + "Instance");
		}

		if(classType == null)
			throw new IllegalArgumentException("Not found type class for type: " + name + ".");
		else
			_npcClass = classType;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.target != null && env.target.getClass() == _npcClass;
	}
}
