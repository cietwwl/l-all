package org.mmocore.gameserver.scripts;

import java.util.Map;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.mail.Mail;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.s2c.ExNoticePostArrived;
import org.mmocore.gameserver.network.l2.s2c.NpcSay;
import org.mmocore.gameserver.utils.ChatUtils;
import org.mmocore.gameserver.utils.HtmlUtils;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.NpcUtils;

@Deprecated
public class Functions
{
	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
			return;

		HtmlMessage msg = npc == null ? new HtmlMessage(5) : new HtmlMessage(npc);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(HtmlUtils.bbParse(text));

		if(arg != null && arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i = +2)
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));

		self.sendPacket(msg);
	}

	public static void showFromStringHolder(String message, Player self)
	{
		show(StringHolder.getInstance().getString(self, message), self, null);
	}

	// Белый чат
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		npcSayInRange(npc, range, NpcString.NONE, text);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, Config.CHAT_RANGE);
	}
	
	// Белый чат
	public static void npcSayInRange(NpcInstance npc, int range, NpcString npcString, String... params)
	{
		ChatUtils.say(npc, range, npcString, params);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		npcSayInRange(npc, Config.CHAT_RANGE, npcString, params);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
	{
		npcSayToPlayer(npc, player, NpcString.NONE, text);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		player.sendPacket(new NpcSay(npc, ChatType.NPC_ALL, npcString, params));
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, String text)
	{
		npcShout(npc, NpcString.NONE, text);
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		ChatUtils.shout(npc, npcString, params);
	}

	//TODO [VISTALL] use NpcUtils
	public static NpcInstance spawn(Location loc, int npcId)
	{
		return spawn(loc, npcId, ReflectionManager.DEFAULT);
	}

	@Deprecated
	public static NpcInstance spawn(Location loc, int npcId, Reflection reflection)
	{
		return NpcUtils.spawnSingle(npcId, loc, reflection, 0);
	}

	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
			return;
		if(title == null)
			return;
		if(items.keySet().size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		for(Map.Entry<Integer, Long> itm : items.entrySet())
		{
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
	}
}