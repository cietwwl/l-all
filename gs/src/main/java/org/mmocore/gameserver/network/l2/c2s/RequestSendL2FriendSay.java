package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.s2c.L2FriendSay;
import org.mmocore.gameserver.utils.Log;

/**
 * Recieve Private (Friend) Message
 * Format: c SS
 * S: Message
 * S: Receiving Player
 */
public class RequestSendL2FriendSay extends L2GameClientPacket
{
	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS(2048);
		_reciever = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getNoChannel() > 0)
		{
			if(ArrayUtils.contains(Config.BAN_CHANNEL_LIST, ChatType.FRIENDTELL))
			{
				if(activeChar.getNoChannelRemained() > 0)
				{
					long timeRemained = activeChar.getNoChannelRemained() / 60000L + 1;
					activeChar.sendMessage(new CustomMessage("common.ChatBanned").addNumber(timeRemained));
					return;
				}
				activeChar.updateNoChannel(0);
			}
		}

		Player targetPlayer = World.getPlayer(_reciever);
		if(targetPlayer == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}

		if(targetPlayer.isBlockAll())
		{
			activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}

		if(!activeChar.getFriendList().getList().containsKey(targetPlayer.getObjectId()))
			return;

		if(activeChar.canTalkWith(targetPlayer))
		{
			L2FriendSay frm = new L2FriendSay(activeChar.getName(), _reciever, _message);
			targetPlayer.sendPacket(frm);

			Log.LogChat("FRIENDTELL", activeChar.getName(), _reciever, _message, 0);
		}
	}
}