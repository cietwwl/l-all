package org.mmocore.gameserver.network.l2.components;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.Say2;

public class CustomChatMessage extends CustomMessage
{
	private final ChatType _type;
	public CustomChatMessage(String address, ChatType type)
	{
		super(address);
		_type = type;
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		return new Say2(0, _type, "", toString(player), null);
	}
}