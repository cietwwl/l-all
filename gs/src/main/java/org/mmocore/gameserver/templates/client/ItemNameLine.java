package org.mmocore.gameserver.templates.client;

import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.utils.Language;

/**
 * @author VISTALL
 * @date 20:45/02.09.2011
 */
public class ItemNameLine
{
	private int _itemId;
	private int _color;

	private String _name;
	private String _augmentName;

	public ItemNameLine(Language lang, int itemId, String name, int color)
	{
		_itemId = itemId;
		_color = color;

		_name = name;
		_augmentName = String.format(StringHolder.getInstance().getString("augmented.s1", lang), name);
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getColor()
	{
		return _color;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getAugmentName()
	{
		return _augmentName;
	}
}
