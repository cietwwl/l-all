package org.mmocore.authserver;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mmocore.commons.threading.RunnableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpBanManager
{
	private static final Logger _log = LoggerFactory.getLogger(IpBanManager.class);

	private static final IpBanManager _instance = new IpBanManager();

	public static final IpBanManager getInstance()
	{
		return _instance;
	}

	private final Map<String, Long> bannedIps = new HashMap<String, Long>();
	private final Map<String, Deque<Long>> loginIps = new HashMap<String, Deque<Long>>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private IpBanManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl()
			{
				long currentMillis = System.currentTimeMillis();

				writeLock.lock();
				try
				{
					//Чистка просроченных банов
					for(Iterator<Long> itr = bannedIps.values().iterator(); itr.hasNext();)
					{
						Long banExpire = itr.next();
						if(banExpire.longValue() < currentMillis)
							itr.remove();
					}
					//Чистка ошибочных входов
					for(Iterator<Deque<Long>> itr = loginIps.values().iterator(); itr.hasNext();)
					{
						int count = 0;
						long timeExpire = currentMillis - 24*60*60*1000L; //время устаревших входов
						Deque<Long> logins = itr.next();
						for(Iterator<Long> deitr =  logins.descendingIterator();deitr.hasNext();)
						{
							Long lt = deitr.next();
							count++;
							if(lt.longValue() < timeExpire || count > Config.LOGIN_FAIL_BURST)
								deitr.remove();
						}
						if(logins.isEmpty())
							itr.remove();
					}
				}
				finally
				{
					writeLock.unlock();
				}
			}

		}, 1000L, 1000L);
	}

	public boolean isIpBanned(String ip)
	{
		readLock.lock();
		try
		{
			Long banExpire;
			if((banExpire = bannedIps.get(ip)) == null)
				return false;

			return banExpire.longValue() > System.currentTimeMillis();
		}
		finally
		{
			readLock.unlock();
		}
	}

	private boolean antibrute(String ip)
	{
		boolean result = true;

		writeLock.lock();
		try
		{
			Deque<Long> logins;
			if((logins = loginIps.get(ip)) == null)
				loginIps.put(ip, logins = new LinkedList<Long>());

			long currentTime = System.currentTimeMillis();
			long firstTryTime = currentTime;
			int loginFailCount = 0;

			for(Iterator<Long> itr =  logins.descendingIterator();itr.hasNext();)
			{
				Long lt = itr.next();
				firstTryTime = lt.longValue();
				loginFailCount++;
				if(Config.LOGIN_FAIL_BURST == loginFailCount)
					break;
			}

			loginFailCount -= ((currentTime - firstTryTime) / Config.LOGIN_FAIL_TIME) * Config.LOGIN_FAIL_COUNT;

			if (Config.LOGIN_FAIL_BURST <= loginFailCount)
			{
				_log.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000L + " seconds.");
				bannedIps.put(ip, Long.valueOf(currentTime + Config.IP_BAN_TIME));
				result = false;
			}

			logins.addLast(Long.valueOf(currentTime));
		}
		finally
		{
			writeLock.unlock();
		}

		return result;
	}

	public boolean tryLogin(String ip, boolean success)
	{
		if(isIpBanned(ip))
			return false;

		if(!success)
			return antibrute(ip);

		return true;
	}
}
