package org.mmocore.gameserver.listener.game;

import org.mmocore.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	public void onDay();

	public void onNight();
}
