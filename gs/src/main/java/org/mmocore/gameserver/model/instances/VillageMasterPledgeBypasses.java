package org.mmocore.gameserver.model.instances;

import java.util.Collection;
import java.util.StringTokenizer;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.entity.events.impl.CastleSiegeEvent;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.entity.residence.Dominion;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.pledge.Alliance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.ClanChangeLeaderRequest;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.PledgeReceiveSubPledgeCreated;
import org.mmocore.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import org.mmocore.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import org.mmocore.gameserver.network.l2.s2c.PledgeStatusChanged;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.SiegeUtils;
import org.mmocore.gameserver.utils.Util;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author VISTALL
 * @date 18:25/12.04.2012
 */
public class VillageMasterPledgeBypasses
{
	private static IntObjectMap<String[]> RENAME_DIALOGS = new HashIntObjectMap<String[]>();

	private static final int START_RENAME_RENAME_UNIT = 0;
	private static final int OK_RENAME_RENAME_UNIT = 1;
	private static final int ERROR_NO_UNIT_RENAME_UNIT = 2;

	static
	{
		RENAME_DIALOGS.put(Clan.SUBUNIT_ACADEMY, new String[]{"pledge/pl_ch_rename_aca.htm", "pledge/pl_rename_ok_aca.htm", "pledge/pl_err_rename_aca.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_ROYAL1, new String[]{"pledge/pl_ch_rename100.htm", "pledge/pl_rename_ok_sub1.htm", "pledge/pl_err_rename_sub.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_ROYAL2, new String[]{"pledge/pl_ch_rename200.htm", "pledge/pl_rename_ok_sub1.htm", "pledge/pl_err_rename_sub.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_KNIGHT1, new String[]{"pledge/pl_ch_rename1001.htm", "pledge/pl_rename_ok_sub2.htm", "pledge/pl_err_rename_sub2.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_KNIGHT2, new String[]{"pledge/pl_ch_rename1002.htm", "pledge/pl_rename_ok_sub2.htm", "pledge/pl_err_rename_sub2.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_KNIGHT3, new String[]{"pledge/pl_ch_rename2001.htm", "pledge/pl_rename_ok_sub2.htm", "pledge/pl_err_rename_sub2.htm"});
		RENAME_DIALOGS.put(Clan.SUBUNIT_KNIGHT4, new String[]{"pledge/pl_ch_rename2002.htm", "pledge/pl_rename_ok_sub2.htm", "pledge/pl_err_rename_sub2.htm"});
	}

	public static void createClan(NpcInstance npc, Player player, String clanName)
	{
		if(player.getLevel() < 10 || player.getClan() != null)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_MEET_THE_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return;
		}

		if(!player.canCreateClan())
		{
			player.sendPacket(SystemMsg.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return;
		}

		if(clanName.length() > 16)
		{
			player.sendPacket(SystemMsg.CLAN_NAMES_LENGTH_IS_INCORRECT);
			return;
		}

		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			player.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID);
			return;
		}

		Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if(clan == null)
		{
			player.sendPacket(SystemMsg.THIS_NAME_ALREADY_EXISTS);
			return;
		}

		player.sendPacket(clan.listAll());
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.updatePledgeClass();
		player.broadcastCharInfo();

		npc.showChatWindow(player, "pledge/pl006.htm");
	}

	public static void showClanSkillList(NpcInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		NpcInstance.showAcquireList(AcquireType.CLAN, player);
	}

	public static void levelUpClan(NpcInstance npc, Player player)
	{
		if(!VillageMasterPledgeBypasses.checkPlayerForClanLeader(npc, player))
			return;

		Clan clan = player.getClan();
		if(clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOUR_CLAN_LEVEL_CANNOT_BE_INCREASED);
			return;
		}

		boolean increaseClanLevel = false;

		switch(clan.getLevel())
		{
			case 0:
				// Upgrade to 1
				if(player.getSp() >= 20000 && player.reduceAdena(650000, true))
				{
					player.setSp(player.getSp() - 20000);
					increaseClanLevel = true;
				}
				break;
			case 1:
				// Upgrade to 2
				if(player.getSp() >= 100000 && player.reduceAdena(2500000, true))
				{
					player.setSp(player.getSp() - 100000);
					increaseClanLevel = true;
				}
				break;
			case 2:
				// Upgrade to 3
				// itemid 1419 == Blood Mark
				if(player.getSp() >= 350000 && ItemFunctions.deleteItem(player, 1419, 1))
				{
					player.setSp(player.getSp() - 350000);
					increaseClanLevel = true;
				}
				break;
			case 3:
				// Upgrade to 4
				// itemid 3874 == Alliance Manifesto
				if(player.getSp() >= 1000000 && ItemFunctions.deleteItem(player, 3874, 1))
				{
					player.setSp(player.getSp() - 1000000);
					increaseClanLevel = true;
				}
				break;
			case 4:
				// Upgrade to 5
				// itemid 3870 == Seal of Aspiration
				if(player.getSp() >= 2500000 && ItemFunctions.deleteItem(player, 3870, 1))
				{
					player.setSp(player.getSp() - 2500000);
					increaseClanLevel = true;
				}
				break;
			case 5:
				// Upgrade to 6
				if(clan.getReputationScore() >= 5000 && clan.getAllSize() >= 30)
				{
					clan.incReputation(-5000, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 6:
				// Upgrade to 7
				if(clan.getReputationScore() >= 10000 && clan.getAllSize() >= 50)
				{
					clan.incReputation(-10000, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 7:
				// Upgrade to 8
				if(clan.getReputationScore() >= 20000 && clan.getAllSize() >= 80)
				{
					clan.incReputation(-20000, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 8:
				// Upgrade to 9
				// itemId 9910 == Blood Oath
				if(clan.getReputationScore() >= 40000 && clan.getAllSize() >= 120)
					if(ItemFunctions.deleteItem(player, 9910, 150L))
					{
						clan.incReputation(-40000, false, "LvlUpClan");
						increaseClanLevel = true;
					}
				break;
			case 9:
				// Upgrade to 10
				// itemId 9911 == Blood Alliance
				if(clan.getReputationScore() >= 40000 && clan.getAllSize() >= 140)
					if(ItemFunctions.deleteItem(player, 9911, 5))
					{
						clan.incReputation(-40000, false, "LvlUpClan");
						increaseClanLevel = true;
					}
				break;
			case 10:
				// Upgrade to 11
				if(clan.getReputationScore() >= 75000 && clan.getAllSize() >= 170 && clan.getCastle() > 0)
				{
					Castle castle = ResidenceHolder.getInstance().getResidence(clan.getCastle());
					Dominion dominion = castle.getDominion();
					if(dominion.getLordObjectId() == player.getObjectId())
					{
						clan.incReputation(-75000, false, "LvlUpClan");
						increaseClanLevel = true;
					}
				}
				break;
		}

		if(increaseClanLevel)
		{
			clan.setLevel(clan.getLevel() + 1);
			clan.updateClanInDB();

			player.broadcastCharInfo();

			npc.doCast(SkillTable.getInstance().getSkillEntry(5103, 1), player, true);

			if(clan.getLevel() >= SiegeUtils.MIN_CLAN_SIEGE_LEVEL)
				SiegeUtils.addSiegeSkills(player);

			if(clan.getLevel() == 5)
				player.sendPacket(SystemMsg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

			// notify all the members about it
			PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
			PledgeStatusChanged ps = new PledgeStatusChanged(clan);
			for(UnitMember mbr : clan)
				if(mbr.isOnline())
				{
					mbr.getPlayer().updatePledgeClass();
					mbr.getPlayer().sendPacket(SystemMsg.YOUR_CLANS_LEVEL_HAS_INCREASED, pu, ps);
					mbr.getPlayer().broadcastCharInfo();
				}
		}
		else
			npc.showChatWindow(player, "pledge/pl016.htm");
	}

	protected static void renameSubPledgeCheck(NpcInstance npc, Player player, String command)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		StringTokenizer tokenizer = new StringTokenizer(command);
		tokenizer.nextElement();
		int val = Integer.parseInt(tokenizer.nextToken());
		String[] dialogs = RENAME_DIALOGS.get(val);
		if(dialogs == null)
			return;

		Clan clan = player.getClan();
		SubUnit subUnit = clan.getSubUnit(val);
		if(subUnit == null)
		{
			npc.showChatWindow(player, dialogs[ERROR_NO_UNIT_RENAME_UNIT]);
			return;
		}

		npc.showChatWindow(player, dialogs[START_RENAME_RENAME_UNIT]);
	}

	public static void renameSubPledge(NpcInstance npc, Player player, String command)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		StringTokenizer tokenizer = new StringTokenizer(command);
		tokenizer.nextElement();
		int val = Integer.parseInt(tokenizer.nextToken());
		String name = tokenizer.nextToken();
		String[] dialogs = RENAME_DIALOGS.get(val);
		if(dialogs == null)
			return;

		Clan clan = player.getClan();
		SubUnit subUnit = clan.getSubUnit(val);
		if(subUnit == null)
		{
			npc.showChatWindow(player, dialogs[ERROR_NO_UNIT_RENAME_UNIT]);
			return;
		}

		if(!Util.isMatchingRegexp(name, Config.CLAN_NAME_TEMPLATE))
			return;

		Collection<SubUnit> subPledge = clan.getAllSubUnits();
		for(SubUnit element : subPledge)
			if(element.getName().equals(name))
			{
				npc.showChatWindow(player, "pledge/pl_already_subname.htm");
				return;
			}

		if(ClanTable.getInstance().getClanByName(name) != null)
		{
			npc.showChatWindow(player, "pledge/pl_already_subname.htm");
			return;
		}

		subUnit.setName(name, true);
		clan.broadcastClanStatus(true, true, false);
		npc.showChatWindow(player, dialogs[OK_RENAME_RENAME_UNIT]);
	}

	public static void upgradeSubPledge(NpcInstance npc, Player player, int subpledgeId)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		Clan clan = player.getClan();
		SubUnit subUnit = clan.getSubUnit(subpledgeId);
		if(subUnit == null || subUnit.isUpgraded())
		{
			npc.showChatWindow(player, "pledge/pl_upgrade_err_sub2.htm");
			return;
		}

		int clanLevel;
		int reputation;
		switch(subpledgeId)
		{
			case Clan.SUBUNIT_ROYAL1:
			case Clan.SUBUNIT_ROYAL2:
				clanLevel = 11;
				reputation = 7500;
				break;
			case Clan.SUBUNIT_KNIGHT1:
			case Clan.SUBUNIT_KNIGHT2:
			case Clan.SUBUNIT_KNIGHT3:
			case Clan.SUBUNIT_KNIGHT4:
				clanLevel = 9;
				reputation = 5000;
				break;
			default:
				throw new IllegalArgumentException();
		}

		if(clan.getLevel() < clanLevel || clan.getReputationScore() < reputation)
		{
			npc.showChatWindow(player, "pledge/pl_upgrade_err_sub2.htm");
			return;
		}

		subUnit.setUpgraded(true, true);
		clan.incReputation(-reputation, false, npc.toString() + ":upgradeSubPledge()");

		npc.showChatWindow(player, "pledge/pl_upgrade_ok_sub2.htm");
	}

	public static boolean checkPlayerForClanLeader(NpcInstance npc, Player player)
	{
		if(player.getClan() == null)
		{
			npc.showChatWindow(player, "pledge/pl_no_pledgeman.htm");
			return false;
		}

		if(!player.isClanLeader())
		{
			npc.showChatWindow(player, "pledge/pl_err_master.htm");
			return false;
		}
		return true;
	}

	protected static void dissolveClan(NpcInstance npc, Player player)
	{
		if(player == null || player.getClan() == null)
			return;

		Clan clan = player.getClan();

		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		if(clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}

		if(!clan.canDisband())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION);
			return;
		}

		if(clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
			return;
		}
		if(clan.isAtWar())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
			return;
		}
		if(clan.getCastle() != 0 || clan.getHasHideout() != 0 || clan.getHasFortress() != 0)
		{
			player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS);
			return;
		}

		for(Residence r : ResidenceHolder.getInstance().getResidences())
		{
			if(r.getSiegeEvent().getSiegeClan(SiegeEvent.ATTACKERS, clan) != null || r.getSiegeEvent().getSiegeClan(SiegeEvent.DEFENDERS, clan) != null || r.getSiegeEvent().getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, clan) != null)
			{
				player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
				return;
			}
		}

		clan.placeForDisband();
		clan.broadcastClanStatus(true, true, false);
		npc.showChatWindow(player, "pledge/pl009.htm");
	}

	public static void restoreClan(VillageMasterInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		Clan clan = player.getClan();
		if(!clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.THERE_ARE_NO_REQUESTS_TO_DISPERSE);
			return;
		}

		clan.unPlaceDisband();
		clan.broadcastClanStatus(true, true, false);
		npc.showChatWindow(player, "pledge/pl012.htm");
	}

	protected static boolean createAlly(Player player, String allyName)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			return false;
		}
		if(player.getClan().getAllyId() != 0)
		{
			player.sendPacket(SystemMsg.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
			return false;
		}
		if(player.getClan().isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_NO_ALLIANCE_CAN_BE_CREATED);
			return false;
		}
		if(allyName.length() > 16)
		{
			player.sendPacket(SystemMsg.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			return false;
		}
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
		{
			player.sendPacket(SystemMsg.INCORRECT_ALLIANCE_NAME__PLEASE_TRY_AGAIN);
			return false;
		}
		if(player.getClan().getLevel() < 5)
		{
			player.sendPacket(SystemMsg.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return false;
		}
		if(ClanTable.getInstance().getAllyByName(allyName) != null)
		{
			player.sendPacket(SystemMsg.THAT_ALLIANCE_NAME_ALREADY_EXISTS);
			return false;
		}
		if(!player.getClan().canCreateAlly())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_OF_DISSOLUTION);
			return false;
		}

		Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
		if(alliance == null)
			return false;

		player.broadcastCharInfo();

		return true;
	}

	public static boolean createSubPledge(NpcInstance npc, Player player, String clanName, final int pledgeType, int minClanLvl, String leaderName)
	{
		UnitMember subLeader = null;

		Clan clan = player.getClan();

		if(clan == null || !player.isClanLeader())
			return false;

		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
			return false;

		Collection<SubUnit> subPledge = clan.getAllSubUnits();
		for(SubUnit element : subPledge)
			if(element.getName().equals(clanName))
			{
				player.sendPacket(SystemMsg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
				return false;
			}

		if(ClanTable.getInstance().getClanByName(clanName) != null)
		{
			player.sendPacket(SystemMsg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
			return false;
		}

		if(clan.getLevel() < minClanLvl)
		{
			player.sendPacket(SystemMsg.THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET);
			return false;
		}

		SubUnit unit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);

		if(pledgeType != Clan.SUBUNIT_ACADEMY)
		{
			subLeader = unit.getUnitMember(leaderName);
			if(subLeader == null)
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader"));
				return false;
			}
			else if(subLeader.isLeaderOf() != Clan.SUBUNIT_NONE)
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader"));
				return false;
			}
		}

		int result = clan.createSubPledge(player, pledgeType, subLeader, clanName);
		if(result == Clan.SUBUNIT_NONE)
		{
			switch(pledgeType)
			{
				case Clan.SUBUNIT_ACADEMY:
					player.sendPacket(SystemMsg.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
					break;
				case Clan.SUBUNIT_ROYAL1:
					npc.showChatWindow(player, "pl_err_more_sub.htm");
					break;
				case Clan.SUBUNIT_KNIGHT1:
					npc.showChatWindow(player, "pl_err_more_sub.htm");
					break;
			}
			return false;
		}

		clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(clan.getSubUnit(result)));

		if(subLeader != null)
		{
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
			if(subLeader.isOnline())
			{
				subLeader.getPlayer().updatePledgeClass();
				subLeader.getPlayer().broadcastCharInfo();
			}
		}
		return true;
	}

	public static boolean changeLeader(NpcInstance npc, Player player, int pledgeId, String leaderName)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return false;

		Clan clan = player.getClan();

		SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		SubUnit subUnit = clan.getSubUnit(pledgeId);

		UnitMember subLeader = mainUnit.getUnitMember(leaderName);
		if(subUnit == null || subLeader == null || subLeader.isLeaderOf() != Clan.SUBUNIT_NONE)
		{
			npc.showChatWindow(player, "pledge/pl_err_man.htm");
			return false;
		}

		if(pledgeId == Clan.SUBUNIT_MAIN_CLAN)
		{
			ClanChangeLeaderRequest request = ClanTable.getInstance().getRequest(clan.getClanId());
			if(request != null)
			{
				npc.showChatWindow(player, "pledge/pl_transfer_already.htm");
				return false;
			}

			request = new ClanChangeLeaderRequest(clan.getClanId(), subLeader.getObjectId(), System.currentTimeMillis() + Clan.CHANGE_LEADER_TIME);

			ClanTable.getInstance().addRequest(request);

			npc.showChatWindow(player, "pledge/pl_transfer_success.htm");
		}
		else
		{
			subUnit.setLeader(subLeader, true);
			clan.broadcastClanStatus(true, true, false);

			if(subLeader.isOnline())
			{
				subLeader.getPlayer().updatePledgeClass();
				subLeader.getPlayer().broadcastCharInfo();
			}

			npc.showChatWindow(player, "pledge/pl_create_ok_submaster.htm");
		}
		return true;
	}

	public static void cancelLeaderChange(VillageMasterInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;

		Clan clan = player.getClan();

		ClanChangeLeaderRequest request = ClanTable.getInstance().getRequest(clan.getClanId());
		if(request == null)
		{
			npc.showChatWindow(player, "pledge/pl_not_transfer.htm");
			return;
		}

		ClanTable.getInstance().cancelRequest(request, false);

		npc.showChatWindow(player, "pledge/pl_cancel_success.htm");
	}
}
