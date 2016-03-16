package org.mmocore.gameserver.utils;

import org.mmocore.gameserver.Announcements;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;

public final class AdminFunctions
{
	public final static Location JAIL_SPAWN = new Location(-114648, -249384, -2984);

	private AdminFunctions() {}

	/**
	 * Кикнуть игрока из игры.
	 *
	 * @param player - имя игрока
	 * @param reason - причина кика
	 * @return true если успешно, иначе false
	 */
	public static boolean kick(String player, String reason)
	{
		Player plyr = World.getPlayer(player);
		if (plyr == null)
			return false;

		return kick(plyr, reason);
	}


	public static boolean kick(Player player, String reason)
	{
		if(Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
			if(player.isCursedWeaponEquipped())
			{
				player.setPvpFlag(0);
				CursedWeaponsManager.getInstance().dropPlayer(player);
			}

		if(player.isInOfflineMode())
			player.setOfflineMode(false);

		player.kick();

		return true;
	}

	public static String banChat(Player adminChar, String adminName, String charName, int val, String reason, boolean silent)
	{
		Player player = World.getPlayer(charName);

		if(player != null)
			charName = player.getName();
		else if(CharacterDAO.getInstance().getObjectIdByName(charName) == 0)
			return "Игрок " + charName + " не найден.";

		if((adminName == null || adminName.isEmpty()) && adminChar != null)
			adminName = adminChar.getName();

		if(reason == null || reason.isEmpty())
			reason = "не указана"; // if no args, then "не указана" default.

		String result, announce = null;
		if(val == 0) //unban
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat)
				return "Вы не имеете прав на снятие бана чата.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " снял бан чата с игрока " + charName + "." : "С игрока " + charName + " снят бан чата.";
			Log.add("Moderator " + adminName + " remove chat ban from player " + charName + ".", "banchat", adminChar);
			result = "Вы сняли бан чата с игрока " + charName + ".";
		}
		else if(val < 0)
		{
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + "." : "Забанен чат игроку " + charName + " на бессрочный период, причина: " + reason + ".";
			Log.add("Moderator " + adminName + " disable chat for player " + charName + ", reason: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на бессрочный период.";
		}
		else
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat && (player == null || player.getNoChannel() != 0))
				return "Вы не имеете права изменять время бана.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + "." : "Забанен чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".";
			Log.add("Moderator " + adminName + " add chat ban to player " + charName + " for " + val + " minutes, reason: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на " + val + " минут.";
		}

		if(player != null)
			updateNoChannel(player, val, reason, silent);
		else
			AutoBan.ChatBan(charName, val, reason, adminName);

		if(announce != null && !silent)
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(announce);
			else
				Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);

		return result;
	}

	private static void updateNoChannel(Player player, int time, String reason, boolean silent)
	{
		player.updateNoChannel(time * 60000L);
		if (time < 0)
			player.broadcastPrivateStoreInfo();

		if(silent)
			return;

		if(time == 0)
			player.sendMessage(new CustomMessage("common.ChatUnBanned"));
		else if(time > 0)
		{
			if(reason == null || reason.isEmpty())
				player.sendMessage(new CustomMessage("common.ChatBanned").addNumber(time));
			else
				player.sendMessage(new CustomMessage("common.ChatBannedWithReason").addNumber(time).addString(reason));
		}
		else if(reason == null || reason.isEmpty())
			player.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
		else
			player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason").addString(reason));
	}
}
