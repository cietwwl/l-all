package org.mmocore.authserver.network.l2;

import java.nio.channels.SocketChannel;

import org.mmocore.authserver.IpBanManager;
import org.mmocore.authserver.ThreadPoolManager;
import org.mmocore.authserver.network.l2.s2c.Init;
import org.mmocore.commons.net.nio.impl.IAcceptFilter;
import org.mmocore.commons.net.nio.impl.IClientFactory;
import org.mmocore.commons.net.nio.impl.IMMOExecutor;
import org.mmocore.commons.net.nio.impl.MMOConnection;


public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	@Override
	public void execute(Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}

	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		final L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	@Override
	public boolean accept(SocketChannel sc)
	{
		return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
	}
}