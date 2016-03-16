package org.mmocore.commons.util.concurrent;

public interface Lockable
{
	/**
	 * Lock for access
	 */
	public void lock();
	/**
	 * Unlock after access
	 */
	public void unlock();
}
