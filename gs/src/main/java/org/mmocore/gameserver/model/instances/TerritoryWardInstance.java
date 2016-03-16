package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.events.objects.TerritoryWardObject;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 16:38/11.04.2011
 */
public class TerritoryWardInstance extends NpcInstance
{
	private final TerritoryWardObject _territoryWard;

	public TerritoryWardInstance(int objectId, NpcTemplate template, TerritoryWardObject territoryWardObject)
	{
		super(objectId, template);
		setHasChatWindow(false);
		_territoryWard = territoryWardObject;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, SkillEntry skill, int poleHitCount, boolean crit, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if (skill != null)
			return;

		super.reduceCurrentHp(damage, attacker, skill, poleHitCount, crit, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	public boolean isUndying(Creature attacker)
	{
		if (attacker.isDead() || !attacker.isPlayer() || ((Player)attacker).isTerritoryFlagEquipped() || attacker.isInZonePeace())
			return true;

		return !isAutoAttackable(attacker);
	}

	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(_territoryWard.canPickUp(player))
		{
			_territoryWard.pickUp(player);
			decayMe();
		}
	}

	@Override
	protected void onDecay()
	{
		decayMe();

		_spawnAnimation = 2;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		if (attacker.getPlayer() == null)
			return false;
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return false;
		DominionSiegeEvent siegeEvent2 = attacker.getPlayer().getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null)
			return false;
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker.getPlayer() == null)
			return false;
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return false;
		DominionSiegeEvent siegeEvent2 = attacker.getPlayer().getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null)
			return false;
		if(siegeEvent == siegeEvent2)
			return false;
		if(siegeEvent2.getResidence().getOwner() != attacker.getPlayer().getClan())
			return false;
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
