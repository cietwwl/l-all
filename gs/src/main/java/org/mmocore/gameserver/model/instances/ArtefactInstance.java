package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

public final class ArtefactInstance extends NpcInstance
{
	public ArtefactInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isArtefact()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	/**
	 * Артефакт нельзя убить
	 */
	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}