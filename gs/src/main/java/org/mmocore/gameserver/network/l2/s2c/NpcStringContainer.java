package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.network.l2.components.NpcString;

/**
 * @author VISTALL
 * @date 16:43/25.03.2011
 */
public abstract class NpcStringContainer extends L2GameServerPacket
{
	protected final NpcString _npcString;
	protected final String[] _parameters = new String[5];

	protected NpcStringContainer(NpcString npcString, String... arg)
	{
		_npcString = npcString;
		System.arraycopy(arg, 0, _parameters, 0, arg.length);
	}

	protected void writeElements()
	{
		writeD(_npcString.getId());
		for(String st : _parameters)
			writeS(st);
	}
}
