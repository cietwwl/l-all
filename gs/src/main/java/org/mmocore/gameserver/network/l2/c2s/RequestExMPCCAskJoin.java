package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.CommandChannel;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExAskJoinMPCC;

public class RequestExMPCCAskJoin extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Player target = World.getPlayer(_name);

		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		Player resultTarget = CommandChannel.checkAndAskToCreateChannel(activeChar, target, false);

		Party activeParty = activeChar.getParty();

		if(resultTarget != null && activeParty.isInCommandChannel())
		{
			if(activeParty.getCommandChannel().getChannelLeader() != activeChar)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
				return;
			}

			new Request(L2RequestType.CHANNEL, activeChar, target).setTimeout(10000L);
			target.sendPacket(new ExAskJoinMPCC(activeChar.getName()));
		}
	}
}