package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.items.ItemInstance;

public class ExVariationResult extends L2GameServerPacket
{
	public static final ExVariationResult CLOSE = new ExVariationResult();
	public static final ExVariationResult FAIL = new ExVariationResult(ItemInstance.EMPTY_AUGMENTATIONS);
	private int[] _augmentations;
	private boolean _openWindow;

	public ExVariationResult(int[] augmentations)
	{
		_augmentations = augmentations;
		_openWindow = true;
	}

	public ExVariationResult()
	{
		_augmentations = ItemInstance.EMPTY_AUGMENTATIONS;
		_openWindow = false;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x56);
		writeD(_augmentations[0]);
		writeD(_augmentations[1]);
		writeD(_openWindow);
	}
}