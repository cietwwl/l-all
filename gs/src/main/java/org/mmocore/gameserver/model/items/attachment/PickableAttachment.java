package org.mmocore.gameserver.model.items.attachment;

import org.mmocore.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 0:50/04.06.2011
 */
public interface PickableAttachment extends ItemAttachment
{
	boolean canPickUp(Player player);

	void pickUp(Player player);
}
