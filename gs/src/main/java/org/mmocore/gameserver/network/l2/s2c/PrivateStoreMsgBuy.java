package org.mmocore.gameserver.network.l2.s2c;


import org.apache.commons.lang3.StringUtils;
import org.mmocore.gameserver.model.Player;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private final int _objId;
	private final String _name;

	/**
	 * Название личного магазина покупки
	 * @param player
	 */
	public PrivateStoreMsgBuy(Player player, boolean showName)
	{
		_objId = player.getObjectId();
		_name = showName ? StringUtils.defaultString(player.getBuyStoreName()) : StringUtils.EMPTY;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xBF);
		writeD(_objId);
		writeS(_name);
	}
}