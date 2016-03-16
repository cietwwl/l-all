package org.mmocore.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import org.mmocore.gameserver.instancemanager.games.MiniGameScoreManager;
import org.mmocore.gameserver.model.Player;


/**
 * @author VISTALL
 * @date  0:07:05/10.04.2010
 */
public class ExBR_MiniGameLoadScores extends L2GameServerPacket
{
	private int _place;
	private int _score;
	private int _lastScore;

	private List<MiniGameScoreManager.MiniGameScore> _entries;

	public ExBR_MiniGameLoadScores(Player player)
	{
		int i = 1;

		NavigableSet<MiniGameScoreManager.MiniGameScore> score = MiniGameScoreManager.getInstance().getScores();
		_entries = new ArrayList<MiniGameScoreManager.MiniGameScore>(score.size() >= 100 ? 100 : score.size());

		MiniGameScoreManager.MiniGameScore last = score.isEmpty() ? null : score.last();
		if(last != null)
			_lastScore = last.getScore();

		for(MiniGameScoreManager.MiniGameScore entry : score)
		{
			if(i > 100)
				break;

			if(entry.getObjectId() == player.getObjectId())
			{
				_place = i;
				_score = entry.getScore();
			}
			_entries.add(entry);
			i++;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xDD);
		writeD(_place); // place of last big score of player
		writeD(_score); // last big score of player
		writeD(_entries.size()); //
		writeD(_lastScore); //last score of list
		for(int i = 0; i < _entries.size(); i ++)
		{
			MiniGameScoreManager.MiniGameScore pair = _entries.get(i);
			writeD(i + 1);
			writeS(pair.getName());
			writeD(pair.getScore());
		}
	}
}