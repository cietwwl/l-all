package org.mmocore.gameserver.instancemanager.games;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang3.tuple.MutablePair;
import org.mmocore.gameserver.dao.CharacterMinigameScoreDAO;
import org.mmocore.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date  15:15/15.10.2010
 * @see ext.properties
 */
public class MiniGameScoreManager
{
	public static class MiniGameScore
	{
		private int _objectId;
		private String _name;
		private int _score;

		public MiniGameScore(int objectId, String name, int score)
		{
			_objectId = objectId;
			_name = name;
			_score = score;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public String getName()
		{
			return _name;
		}

		public int getScore()
		{
			return _score;
		}

		public void setScore(int score)
		{
			_score = score;
		}

		@Override
		public boolean equals(Object o)
		{
			return !(o == null || o.getClass() != MiniGameScore.class) && ((MiniGameScore) o).getObjectId() == getObjectId();
		}
	}

	private NavigableSet<MiniGameScore> _scores = new ConcurrentSkipListSet<MiniGameScore>(new Comparator<MiniGameScore>()
	{
		@Override
		public int compare(MiniGameScore o1, MiniGameScore o2)
		{
			return o2.getScore() - o1.getScore();
		}
	});

	private static MiniGameScoreManager _instance = new MiniGameScoreManager();

	public static MiniGameScoreManager getInstance()
	{
		return _instance;
	}

	private MiniGameScoreManager()
	{
		//
	}

	public void addScore(Player player, int score)
	{
		MiniGameScore miniGameScore = null;
		for(MiniGameScore $miniGameScore : _scores)
			if($miniGameScore.getObjectId() == player.getObjectId())
				miniGameScore = $miniGameScore;

		if(miniGameScore == null)
			_scores.add(new MiniGameScore(player.getObjectId(), player.getName(), score));
		else
		{
			// текущий меньше тот которого есть
			if(miniGameScore.getScore() > score)
				return;

			miniGameScore.setScore(score);
		}

		CharacterMinigameScoreDAO.getInstance().replace(player.getObjectId(), score);
	}

	public void addScore(int objectId, int score, String name)
	{
		_scores.add(new MiniGameScore(objectId, name, score));
	}

	public NavigableSet<MiniGameScore> getScores()
	{
		return _scores;
	}
}
