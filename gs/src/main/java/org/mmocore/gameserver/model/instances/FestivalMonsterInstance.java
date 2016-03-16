package org.mmocore.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import org.mmocore.gameserver.model.reward.RewardList;
import org.mmocore.gameserver.model.reward.RewardType;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;


public class FestivalMonsterInstance extends MonsterInstance
{
	protected int _bonusMultiplier = 1;

	/**
	 * Constructor<?> of L2FestivalMonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2FestivalMonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public FestivalMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_hasRandomWalk = false;
	}

	public void setOfferingBonus(int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		List<Player> pl = World.getAroundPlayers(this);
		if(pl.isEmpty())
			return;
		List<Player> alive = new ArrayList<Player>(9);
		for(Player p : pl)
			if(!p.isDead())
				alive.add(p);
		if(alive.isEmpty())
			return;

		Player target = alive.get(Rnd.get(alive.size()));
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
	}

	/**
	 * Actions:
	 * <li>Check if the killing object is a player, and then find the party they belong to.</li>
	 * <li>Add a blood offering item to the leader of the party.</li>
	 * <li>Update the party leader's inventory to show the new item addition.</li>
	 */
	@Override
	public void rollRewards(RewardList entry, final Creature lastAttacker, Creature topDamager)
	{
		super.rollRewards(entry, lastAttacker, topDamager);

		if(entry.getType() != RewardType.RATED_GROUPED)
			return;
		if(!topDamager.isPlayable())
			return;

		Player topDamagerPlayer = topDamager.getPlayer();
		Party associatedParty = topDamagerPlayer.getParty();

		if(associatedParty == null)
			return;

		Player partyLeader = associatedParty.getPartyLeader();
		if(partyLeader == null)
			return;

		ItemFunctions.addItem(partyLeader, SevenSignsFestival.FESTIVAL_BLOOD_OFFERING, _bonusMultiplier);
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1000;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}