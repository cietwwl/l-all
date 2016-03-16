package org.mmocore.gameserver.model;

/**
* @author VISTALL
* @date 20:21/03.02.2012
*/
public class CharSelectInfoPaperdollItem
{
	private int _itemId;
	private int[] _augmentations;
	private int _enchantLevel;

	public CharSelectInfoPaperdollItem(int itemId, int[] augmentations, int enchantLevel)
	{
		_itemId = itemId;
		_augmentations = augmentations;
		_enchantLevel = enchantLevel;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int[] getAugmentations()
	{
		return _augmentations;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
}
