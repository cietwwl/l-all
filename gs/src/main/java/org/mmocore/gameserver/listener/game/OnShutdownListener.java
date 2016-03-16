package org.mmocore.gameserver.listener.game;

import org.mmocore.gameserver.listener.GameListener;

public interface OnShutdownListener extends GameListener
{
	public void onShutdown();
}
