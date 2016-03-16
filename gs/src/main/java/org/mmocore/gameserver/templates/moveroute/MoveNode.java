package org.mmocore.gameserver.templates.moveroute;

import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 22:12/24.10.2011
 */
public class MoveNode extends Location
{
	private static final long serialVersionUID = 8291528118019681063L;

	private final NpcString _npcString;
	private final ChatType _chatType;
	private final long _delay;
	private final int _socialId;

	public MoveNode(int x, int y, int z, NpcString npcString, int socialId, long delay, ChatType chatType)
	{
		super(x, y, z);
		_npcString = npcString;
		_socialId = socialId;
		_delay = delay;
		_chatType = chatType;
	}

	public NpcString getNpcString()
	{
		return _npcString;
	}

	public long getDelay()
	{
		return _delay;
	}

	public int getSocialId()
	{
		return _socialId;
	}

	public ChatType getChatType()
	{
		return _chatType;
	}
}
