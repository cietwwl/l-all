package org.mmocore.gameserver.instancemanager.itemauction;

import org.mmocore.commons.lang.ArrayUtils;

public enum ItemAuctionState
{
	CREATED,
	STARTED,
	FINISHED;

	public static final ItemAuctionState stateForStateId(int stateId)
	{
		return ArrayUtils.valid(values(), stateId);
	}
}