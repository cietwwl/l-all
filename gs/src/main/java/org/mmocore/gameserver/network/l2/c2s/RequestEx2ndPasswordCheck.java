package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.network.l2.s2c.Ex2ndPasswordCheck;

/**
 * @author VISTALL
 */
public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//
	}
	
	@Override
	protected void runImpl()
	{
		if(Config.EX_2ND_PASSWORD_CHECK)
			sendPacket(Ex2ndPasswordCheck.PASSWORD_OK);
	}
}
