package org.mmocore.gameserver.network.authcomm;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReceivablePacket extends org.mmocore.commons.net.nio.ReceivablePacket<AuthServerCommunication>
{
	private static final Logger _log = LoggerFactory.getLogger(ReceivablePacket.class);

	@Override
	public AuthServerCommunication getClient()
	{
		return AuthServerCommunication.getInstance();
	}

	@Override
	protected ByteBuffer getByteBuffer()
	{
		return getClient().getReadBuffer();
	}

	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return false;
	}

	@Override
	public final void run()
	{
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	protected abstract void readImpl();

	protected abstract void runImpl();

	protected void sendPacket(SendablePacket sp)
	{
		getClient().sendPacket(sp);
	}
}
