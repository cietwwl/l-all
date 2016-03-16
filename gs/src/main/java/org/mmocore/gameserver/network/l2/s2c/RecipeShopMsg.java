package org.mmocore.gameserver.network.l2.s2c;

import org.apache.commons.lang3.StringUtils;
import org.mmocore.gameserver.model.Player;

public class RecipeShopMsg extends L2GameServerPacket
{
	private final int _objectId;
	private final String _storeName;

	public RecipeShopMsg(Player player, boolean showName)
	{
		_objectId = player.getObjectId();
		_storeName = showName ? StringUtils.defaultString(player.getManufactureName()) : StringUtils.EMPTY;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe1);
		writeD(_objectId);
		writeS(_storeName);
	}
}