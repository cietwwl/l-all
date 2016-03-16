package org.mmocore.gameserver.config.parser;

import gnu.trove.TIntArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.mmocore.commons.data.xml.AbstractFileParser;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.chat.ChatFilters;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilter;
import org.mmocore.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchChatChannels;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchChatLimit;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchFloodLimit;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchLogicalAnd;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchLogicalNot;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchLogicalOr;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchLogicalXor;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchMaps;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchMinJobLevel;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchMinLevel;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchMinLiveTime;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchMinOnlineTime;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchPremiumState;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchRecipientLimit;
import org.mmocore.gameserver.model.chat.chatfilter.matcher.MatchWords;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * Парсер фильтров чата.
 *
 * @author G1ta0
 */
public class ChatFilterParser extends AbstractFileParser<ChatFilters>
{
	private static ChatFilterParser _instance = new ChatFilterParser();

	public static ChatFilterParser getInstance()
	{
		return _instance;
	}

	protected ChatFilterParser()
	{
		super(ChatFilters.getinstance());
	}

	protected List<ChatFilterMatcher> parseMatchers(Element n) throws Exception
	{
		List<ChatFilterMatcher> matchers = new ArrayList<ChatFilterMatcher>();
		for(Iterator<Element> nItr = n.elementIterator(); nItr.hasNext();)
		{
			Element e = nItr.next();

			if(e.getName().equals("Channels"))
			{
				List<ChatType> channels = new ArrayList<ChatType>();
				StringTokenizer st = new StringTokenizer(e.getText(), ",");
				while(st.hasMoreTokens())
					channels.add(ChatType.valueOf(st.nextToken()));
				matchers.add(new MatchChatChannels(channels.toArray(new ChatType[channels.size()])));
			}
			else if(e.getName().equals("Maps"))
			{
				TIntArrayList maps = new TIntArrayList();
				StringTokenizer st = new StringTokenizer(e.getText(), ",");
				while(st.hasMoreTokens())
				{
					String[] map = st.nextToken().split("_");
					maps.add(Integer.parseInt(map[0]));
					maps.add(Integer.parseInt(map[1]));
				}
				matchers.add(new MatchMaps( maps.toNativeArray()));
			}
			else if(e.getName().equals("Words"))
			{
				List<String> words = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(e.getText());
				while(st.hasMoreTokens())
					words.add(st.nextToken());
				matchers.add(new MatchWords(words.toArray(new String[words.size()])));
			}
			else if(e.getName().equals("ExcludePremium"))
			{
				matchers.add(new MatchPremiumState(Boolean.parseBoolean(e.getText())));
			}
			else if(e.getName().equals("Level"))
			{
				matchers.add(new MatchMinLevel(Integer.parseInt(e.getText())));
			}
			else if(e.getName().equals("JobLevel"))
			{
				matchers.add(new MatchMinJobLevel(Integer.parseInt(e.getText())));
			}
			else if(e.getName().equals("OnlineTime"))
			{
				matchers.add(new MatchMinOnlineTime(Integer.parseInt(e.getText())));
			}
			else if(e.getName().equals("LiveTime"))
			{
				matchers.add(new MatchMinLiveTime(Integer.parseInt(e.getText())));
			}
			else if(e.getName().endsWith("Limit"))
			{
				int limitCount = 0;
				int limitTime = 0;
				int limitBurst = 0;

				for(Iterator<Element> eItr = e.elementIterator(); eItr.hasNext();)
				{
					Element d = eItr.next();
					if(d.getName().equals("Count"))
						limitCount = Integer.parseInt(d.getText());
					else if(d.getName().equals("Time"))
						limitTime = Integer.parseInt(d.getText());
					else if(d.getName().equals("Burst"))
						limitBurst = Integer.parseInt(d.getText());
				}

				if(limitCount < 1)
					throw new IllegalArgumentException("Limit Count < 1!");
				if(limitTime < 1)
					throw new IllegalArgumentException("Limit Time  < 1!");
				if(limitBurst < 1)
					throw new IllegalArgumentException("Limit Burst < 1!");

				if(e.getName().equals("Limit"))
					matchers.add(new MatchChatLimit(limitCount, limitTime, limitBurst));
				else if(e.getName().equals("FloodLimit"))
					matchers.add(new MatchFloodLimit(limitCount, limitTime, limitBurst));
				else if(e.getName().equals("RecipientLimit"))
					matchers.add(new MatchRecipientLimit(limitCount, limitTime, limitBurst));
			}
			else if(e.getName().equals("Or"))
			{
				List<ChatFilterMatcher> matches = parseMatchers(e);
				matchers.add(new MatchLogicalOr(matches.toArray(new ChatFilterMatcher[matches.size()])));
			}
			else if(e.getName().equals("And"))
			{
				List<ChatFilterMatcher> matches = parseMatchers(e);
				matchers.add(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[matches.size()])));
			}
			else if(e.getName().equals("Not"))
			{
				List<ChatFilterMatcher> matches = parseMatchers(e);
				if(matches.size() == 1)
					matchers.add(new MatchLogicalNot(matches.get(0)));
				else
					matchers.add(new MatchLogicalNot(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[matches.size()]))));
			}
			else if(e.getName().equals("Xor"))
			{
				List<ChatFilterMatcher> matches = parseMatchers(e);
				matchers.add(new MatchLogicalXor(matches.toArray(new ChatFilterMatcher[matches.size()])));
			}
		}
		return matchers;
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			ChatFilterMatcher matcher;
			int action = ChatFilter.ACTION_NONE;
			String value = null;

			Element filterElement = iterator.next();
			for(Iterator<Element> filterItr = filterElement.elementIterator(); filterItr.hasNext();)
			{
				Element e = filterItr.next();

				if(e.getName().equals("Action"))
				{
					String banStr = e.getText();
					if(banStr.equals("BanChat"))
						action = ChatFilter.ACTION_BAN_CHAT;
					else if(banStr.equals("WarnMsg"))
						action = ChatFilter.ACTION_WARN_MSG;
					else if(banStr.equals("ReplaceMsg"))
						action = ChatFilter.ACTION_REPLACE_MSG;
					else if(banStr.equals("RedirectMsg"))
						action = ChatFilter.ACTION_REDIRECT_MSG;
				}
				else if(e.getName().equals("BanTime"))
				{
					value = String.valueOf(Integer.parseInt(e.getText()));
				}
				else if(e.getName().equals("RedirectChannel"))
				{
					value = ChatType.valueOf(e.getText()).toString();
				}
				else if(e.getName().equals("ReplaceMsg"))
				{
					value = e.getText();
				}
				else if(e.getName().equals("WarnMsg"))
				{
					value = e.getText();
				}
			}

			List<ChatFilterMatcher> matchers = parseMatchers(filterElement);
			if(matchers.isEmpty())
				throw new IllegalArgumentException("No matchers defined for a filter!");
			if(matchers.size() == 1)
				matcher = matchers.get(0);
			else
				matcher = new MatchLogicalAnd(matchers.toArray(new ChatFilterMatcher[matchers.size()]));

			getHolder().add(new ChatFilter(matcher, action, value));
		}
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.CHATFILTERS_CONFIG_FILE);
	}

	@Override
	public String getDTDFileName()
	{
		return "chatfilters.dtd";
	}
}
