package org.mmocore.gameserver.model.actor.recorder;

import org.mmocore.gameserver.model.Servitor;

/**
 * @author G1ta0
 */
public class SummonStatsChangeRecorder extends CharStatsChangeRecorder<Servitor>
{
	public SummonStatsChangeRecorder(Servitor actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
			_activeChar.broadcastCharInfo();
		else if ((_changes & SEND_CHAR_INFO) == SEND_CHAR_INFO)
			_activeChar.sendPetInfo();
	}
}
