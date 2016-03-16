package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.AccountPlayerInfo;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;

/**
 * @author VISTALL
 * @date 20:24/16.05.2011
 */
public class PackageToList extends L2GameServerPacket
{
	private IntObjectMap<AccountPlayerInfo> _characters = Containers.emptyIntObjectMap();

	public PackageToList(Player player)
	{
		_characters = player.getAccountChars();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xC8);
		writeD(_characters.size());
		for(IntObjectPair<AccountPlayerInfo> entry : _characters.entrySet())
		{
			writeD(entry.getKey());
			writeS(entry.getValue().getName());
		}
	}
}
