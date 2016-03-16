package org.mmocore.gameserver.listener.zone;

import org.mmocore.commons.listener.Listener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Zone;

public interface OnZoneEnterLeaveListener extends Listener<Zone>
{
	public void onZoneEnter(Zone zone, Creature actor);

	public void onZoneLeave(Zone zone, Creature actor);
}
