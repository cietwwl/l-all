package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;

/**
 * @author VISTALL
 */
public class ExPartyMemberRenamed extends L2GameServerPacket
{
	private int _objectId, _dominionId;
	private boolean _isDisguised;

	public ExPartyMemberRenamed(Player player)
	{
		_objectId = player.getObjectId();
		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(player.getObjectId());
			_dominionId = siegeEvent.getId();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xA6);
		writeD(_objectId);
		writeD(_isDisguised);
		writeD(_isDisguised ? _dominionId : 0);
	}
}