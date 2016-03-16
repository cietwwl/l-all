package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.instancemanager.games.MiniGameScoreManager;
import org.mmocore.gameserver.model.Player;

/**
 * @author VISTALL
 * @date  19:55:45/25.05.2010
 */
public class RequestExBR_MiniGameInsertScore extends L2GameClientPacket
{
	private int _score;

	@Override
	protected void readImpl() throws Exception
	{
		_score = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_JAPAN_MINIGAME)
			return;

		MiniGameScoreManager.getInstance().addScore(player, _score);
	}
}