package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;

/**
 * @author VISTALL
 * @date 12:08/05.03.2011
 */
public class ExDominionWarStart  extends L2GameServerPacket
{
	private int _objectId;
	private int _territoryId, _disguisedTerritoryId;
	private boolean _isDisguised;
	private boolean _battlefieldChatActive;

	public ExDominionWarStart(Player player)
	{
		_objectId = player.getObjectId();

		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			_battlefieldChatActive = siegeEvent.hasState(DominionSiegeEvent.BATTLEFIELD_CHAT_STATE);
			_territoryId = siegeEvent.isInProgress() ? siegeEvent.getId() : 0;

			_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(_objectId);
			if(_isDisguised)
				_disguisedTerritoryId = siegeEvent.getId();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xA3);
		writeD(_objectId);
		writeD(_battlefieldChatActive);
		writeD(_territoryId);
		writeD(_isDisguised);
		writeD(_disguisedTerritoryId);
	}
}
