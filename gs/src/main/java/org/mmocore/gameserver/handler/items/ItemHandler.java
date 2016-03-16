package org.mmocore.gameserver.handler.items;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.templates.item.ItemTemplate;

public class ItemHandler extends AbstractHolder
{
	private static final ItemHandler _instance = new ItemHandler();

	public static ItemHandler getInstance()
	{
		return _instance;
	}

	private ItemHandler()
	{
		//
	}

	public void registerItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for(int itemId : ids)
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
			if(template == null)
				warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
			else if(template.getHandler() != IItemHandler.DEFAULT)
				warn("Duplicate handler for item: " + itemId + "(" + template.getHandler().getClass().getSimpleName() + "," + handler.getClass().getSimpleName() + ")");
			else
				template.setHandler(handler);
		}
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{

	}
}
