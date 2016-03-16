package org.mmocore.gameserver.instancemanager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.mmocore.gameserver.dao.OlympiadHistoryDAO;
import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.entity.Hero;
import org.mmocore.gameserver.model.entity.olympiad.OlympiadHistoryEntry;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.HtmlUtils;
import org.mmocore.gameserver.utils.TimeUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 * @date 20:32/02.05.2011
 */
public class OlympiadHistoryManager
{
	private static final OlympiadHistoryManager _instance = new OlympiadHistoryManager();

	private IntObjectMap<List<OlympiadHistoryEntry>> _historyNew = new CHashIntObjectMap<List<OlympiadHistoryEntry>>();
	private IntObjectMap<List<OlympiadHistoryEntry>> _historyOld = new CHashIntObjectMap<List<OlympiadHistoryEntry>>();

	public static OlympiadHistoryManager getInstance()
	{
		return _instance;
	}

	OlympiadHistoryManager()
	{
		Map<Boolean, List<OlympiadHistoryEntry>> historyList = OlympiadHistoryDAO.getInstance().select();
		for(Map.Entry<Boolean, List<OlympiadHistoryEntry>> entry : historyList.entrySet())
			for(OlympiadHistoryEntry history : entry.getValue())
				addHistory(entry.getKey(), history);
	}

	/**
	 * Старую за преведущих 2 месяца удаляет, а за преведущий
	 */
	public void switchData()
	{
		_historyOld.clear();

		_historyOld.putAll(_historyNew);

		_historyNew.clear();

		OlympiadHistoryDAO.getInstance().switchData();
	}

	public void saveHistory(OlympiadHistoryEntry history)
	{
		addHistory(false, history);

		OlympiadHistoryDAO.getInstance().insert(history);
	}

	public void addHistory(boolean old, OlympiadHistoryEntry history)
	{
		IntObjectMap<List<OlympiadHistoryEntry>> map = old ? _historyOld : _historyNew;

		addHistory0(map, history.getObjectId1(), history);
		addHistory0(map, history.getObjectId2(), history);
	}

	private void addHistory0(IntObjectMap<List<OlympiadHistoryEntry>> map, int objectId, OlympiadHistoryEntry history)
	{
		List<OlympiadHistoryEntry> historySet = map.get(objectId);
		if(historySet == null)
			map.put(objectId, historySet = new CopyOnWriteArrayList<OlympiadHistoryEntry> ());

		historySet.add(history);
	}

	public void showHistory(Player player, int targetClassId, int page)
	{
		final int perpage = 15;

		Map.Entry<Integer, StatsSet> entry = Hero.getInstance().getHeroStats(targetClassId);
		if(entry == null)
			return;

		List<OlympiadHistoryEntry> historyList = _historyOld.get(entry.getKey());
		if(historyList == null)
			historyList = Collections.emptyList();

		HtmlMessage html = new HtmlMessage(5);
		html.setFile("olympiad/monument_hero_history.htm");

		int allStatWinner = 0;
		int allStatLoss = 0;
		int allStatTie = 0;
		for(OlympiadHistoryEntry h : historyList)
		{
			if(h.getGameStatus() == 0)
				allStatTie ++;
			else
			{
				int team = entry.getKey() == h.getObjectId1() ? 1 : 2;
				if(h.getGameStatus() == team)
					allStatWinner ++;
				else
					allStatLoss ++;
			}
		}

		html.replace("%wins%", String.valueOf(allStatWinner));
		html.replace("%ties%", String.valueOf(allStatTie));
		html.replace("%losses%", String.valueOf(allStatLoss));

		int min = perpage * (page - 1);
		int max = perpage * page;

		int currentWinner = 0;
		int currentLoss = 0;
		int currentTie = 0;

		final StringBuilder b = new StringBuilder(500);

		for(int i = 0 ; i < historyList.size(); i++)
		{
			OlympiadHistoryEntry history = historyList.get(i);
			if(history.getGameStatus() == 0)
				currentTie ++;
			else
			{
				int team = entry.getKey() == history.getObjectId1() ? 1 : 2;
				if(history.getGameStatus() == team)
					currentWinner ++;
				else
					currentLoss ++;
			}

			if(i < min)
				continue;

			if(i >= max)
				break;

			b.append("<tr><td>");

			int team = history.getObjectId1() == entry.getKey() ? 1 : 2;
			String list = null;
			if(history.getGameStatus() == 0)
				list = HtmCache.getInstance().getHtml("olympiad/monument_hero_history_list_draw.htm", player);
			else if(team == history.getGameStatus())
				list = HtmCache.getInstance().getHtml("olympiad/monument_hero_history_list_victory.htm", player);
			else
				list = HtmCache.getInstance().getHtml("olympiad/monument_hero_history_list_loss.htm", player);

			StrBuilder sb = new StrBuilder(list);
			sb.replaceAll("%classId%", String.valueOf(team == 1 ? history.getClassId2() : history.getClassId1()));
			sb.replaceAll("%name%", team == 1 ? history.getName2() : history.getName1());
			sb.replaceAll("%date%", TimeUtils.toSimpleFormat(history.getGameStartTime()));
			sb.replaceAll("%time%", String.format("%02d:%02d", history.getGameTime() / 60, history.getGameTime() % 60));
			sb.replaceAll("%victory_count%", String.valueOf(currentWinner));
			sb.replaceAll("%tie_count%", String.valueOf(currentTie));
			sb.replaceAll("%loss_count%", String.valueOf(currentLoss));
			b.append(sb);

			b.append("</td></tr");
		}

		if(min > 0)
		{
			html.replace("%buttprev%", HtmlUtils.PREV_BUTTON);
			html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page - 1));
		}
		else
			html.replace("%buttprev%", StringUtils.EMPTY);

		if(historyList.size() > max)
		{
			html.replace("%buttnext%", HtmlUtils.NEXT_BUTTON);
			html.replace("%next_bypass%", "_match?class=" + targetClassId + "&page=" + (page + 1));
		}
		else
			html.replace("%buttnext%", StringUtils.EMPTY);

		html.replace("%list%", b.toString());

		player.sendPacket(html);
	}
}
