package org.mmocore.gameserver.model.pledge;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.utils.Log;

public enum Rank
{
	VAGABOND,
	VASSAL,
	HEIR,
	KNIGHT,
	ELDER,
	BARON,
	VISCOUNT,
	COUNT,
	MARQUIS,
	DUKE,
	GRAND_DUKE,
	KING,
	EMPEROR;

	public static final Rank[] VALUES = values();

	public static final Rank getPledgeClass(Player player)
	{
		final Clan clan = player.getClan();
		final int clanLevel = clan == null ? -1 : clan.getLevel();
		final boolean inAcademy = clan != null && Clan.isAcademy(player.getPledgeType());
		final boolean isGuard = clan != null && Clan.isRoyalGuard(player.getPledgeType());
		final boolean isKnight = clan != null && Clan.isOrderOfKnights(player.getPledgeType());

		boolean isGuardCaptain = false, isKnightCommander = false, isLeader = false;

		final SubUnit unit = player.getSubUnit();
		if(unit != null)
		{
			UnitMember unitMember = unit.getUnitMember(player.getObjectId());
			if(unitMember == null)
			{
				Log.debug("Player: " + player + "unitMember null, clan: " + clan.getClanId() + "; pledgeType: " + unit.getType());
				return VAGABOND;
			}
			isGuardCaptain = Clan.isRoyalGuard(unitMember.isLeaderOf());
			isKnightCommander = Clan.isOrderOfKnights(unitMember.isLeaderOf());
			isLeader = unitMember.isLeaderOf() == Clan.SUBUNIT_MAIN_CLAN;
		}

		Rank pledgeClass = VAGABOND;
		switch(clanLevel)
		{
			case -1:
				pledgeClass = VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				if(isLeader)
					pledgeClass = HEIR;
				else
					pledgeClass = VASSAL;
				break;
			case 4:
				if(isLeader)
					pledgeClass = KNIGHT;
				else
					pledgeClass = HEIR;
				break;
			case 5:
				if(isLeader)
					pledgeClass = ELDER;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else
					pledgeClass = HEIR;
				break;
			case 6:
				if(isLeader)
					pledgeClass = BARON;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = ELDER;
				else if(isGuard)
					pledgeClass = HEIR;
				else
					pledgeClass = KNIGHT;
				break;
			case 7:
				if(isLeader)
					pledgeClass = COUNT;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = VISCOUNT;
				else if(isGuard)
					pledgeClass = KNIGHT;
				else if(isKnightCommander)
					pledgeClass = BARON;
				else if(isKnight)
					pledgeClass = HEIR;
				else
					pledgeClass = ELDER;
				break;
			case 8:
				if(isLeader)
					pledgeClass = MARQUIS;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = COUNT;
				else if(isGuard)
					pledgeClass = ELDER;
				else if(isKnightCommander)
					pledgeClass = VISCOUNT;
				else if(isKnight)
					pledgeClass = KNIGHT;
				else
					pledgeClass = BARON;
				break;
			case 9:
				if(isLeader)
					pledgeClass = DUKE;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = MARQUIS;
				else if(isGuard)
					pledgeClass = BARON;
				else if(isKnightCommander)
					pledgeClass = COUNT;
				else if(isKnight)
					pledgeClass = ELDER;
				else
					pledgeClass = VISCOUNT;
				break;
			case 10:
				if(isLeader)
					pledgeClass = GRAND_DUKE;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = DUKE;
				else if(isGuard)
					pledgeClass = VISCOUNT;
				else if(isKnightCommander)
					pledgeClass = MARQUIS;
				else if(isKnight)
					pledgeClass = BARON;
				else
					pledgeClass = COUNT;
				break;
			case 11:
				if(isLeader)
					pledgeClass = KING;
				else if(inAcademy)
					pledgeClass = VASSAL;
				else if(isGuardCaptain)
					pledgeClass = GRAND_DUKE;
				else if(isGuard)
					pledgeClass = COUNT;
				else if(isKnightCommander)
					pledgeClass = DUKE;
				else if(isKnight)
					pledgeClass = VISCOUNT;
				else
					pledgeClass = MARQUIS;
				break;
		}

		if(player.isHero() && pledgeClass.ordinal() < MARQUIS.ordinal())
			pledgeClass = MARQUIS;
		else if(player.isNoble() && pledgeClass.ordinal() < BARON.ordinal())
			pledgeClass = BARON;

		return pledgeClass;
	}
}