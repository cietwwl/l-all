package org.mmocore.gameserver.data.htm;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.utils.HtmlUtils;
import org.mmocore.gameserver.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Кэширование html диалогов.
 *
 * @author G1ta0
 * @reworked VISTALL
 * В кеше список вот так
 * admin/admhelp.htm
 * admin/admin.htm
 * admin/admserver.htm
 * admin/banmenu.htm
 * admin/charmanage.htm
 */
public class HtmCache
{
	public static final int DISABLED = 0; // кеширование отключено (только для тестирования)
	public static final int LAZY = 1; // диалоги кешируются по мере обращения
	public static final int ENABLED = 2; // все диалоги кешируются при загрузке сервера

	private static final Logger _log = LoggerFactory.getLogger(HtmCache.class);

	private final static HtmCache _instance = new HtmCache();

	public static HtmCache getInstance()
	{
		return _instance;
	}

	private Cache[] _cache = new Cache[Language.VALUES.length];

	private HtmCache()
	{
		for(int i = 0; i < _cache.length; i++)
			_cache[i] = CacheManager.getInstance().getCache(getClass().getName() + "." + Language.VALUES[i].name());
	}

	public void reload()
	{
		clear();

		switch(Config.HTM_CACHE_MODE)
		{
			case ENABLED:
				for(Language lang : Language.VALUES)
				{
					File root = new File(Config.DATAPACK_ROOT, "data/html-" + lang.getShortName());
					if(!root.exists())
					{
						_log.info("HtmCache: Not find html dir for lang: " + lang);
						continue;
					}
					load(lang, root, root.getAbsolutePath() + "/");
				}
				for(int i = 0; i < _cache.length; i++)
				{
					Cache c = _cache[i];
					_log.info(String.format("HtmCache: parsing %d documents; lang: %s.", c.getSize(), Language.VALUES[i]));
				}
				break;
			case LAZY:
				_log.info("HtmCache: lazy cache mode.");
				break;
			case DISABLED:
				_log.info("HtmCache: disabled.");
				break;
		}
	}

	private void load(Language lang, File f, final String rootPath)
	{
		if(!f.exists())
		{
			_log.info("HtmCache: dir not exists: " + f);
			return;
		}
		File[] files = f.listFiles();

		//FIXME [VISTALL] может лучше использовать Apache FileUtils?
		for(File file : files)
		{
			if(file.isDirectory())
				load(lang, file, rootPath);
			else
			{
				if(file.getName().endsWith(".htm"))
					try
					{
						putContent(lang, file, rootPath);
					}
					catch(IOException e)
					{
						_log.error("HtmCache: file error: " + e, e);
					}
			}
		}
	}

	private void putContent(Language lang, File f, final String rootPath) throws IOException
	{
		String content = FileUtils.readFileToString(f, "UTF-8");

		String path = f.getAbsolutePath().substring(rootPath.length()).replace("\\", "/");

		_cache[lang.ordinal()].put(new Element(path.toLowerCase(), HtmlUtils.bbParse(content)));
	}

	/**
	 * Получить html.
	 *
	 * @param fileName путь до html относительно data/html-LANG
	 * @param player
	 * @return существующий диалог, либо null и сообщение об ошибке в лог, если диалога не существует
	 */
	public String getHtml(String fileName, Player player)
	{           
		Language lang = player == null ? Language.RUSSIAN : player.getLanguage();               
		String cache = getCache(fileName, lang);
		if(cache == null)
			_log.warn("Dialog: " + "data/html-" + lang.getShortName() + "/" + fileName + " not found.");
		return cache;
	}

	/**
	 * Получить существующий html.
	 *
	 * @param fileName путь до html относительно data/html-LANG
	 * @param player
	 * @return null если диалога не существует
	 */
	public String getIfExists(String fileName, Player player)
	{          
		Language lang = player == null ? Language.RUSSIAN : player.getLanguage();              
		return getCache(fileName, lang);
	}

	private String getCache(String file, Language lang)
	{
		if(file == null)
			return null;

		final String fileLower = file.toLowerCase();
		String cache = get(lang, fileLower);

		if(cache == null)
		{
			switch(Config.HTM_CACHE_MODE)
			{
				case ENABLED:
					break;
				case LAZY:
					cache = loadLazy(lang, file);
					if(cache == null && lang != Language.RUSSIAN)
						cache = loadLazy(Language.RUSSIAN, file);
					break;
				case DISABLED:
					cache = loadDisabled(lang, file);
					if(cache == null && lang != Language.RUSSIAN)
						cache = loadDisabled(Language.RUSSIAN, file);
					break;
			}
		}

		return cache;
	}

	private String loadDisabled(Language lang, String file)
	{
		String cache = null;
		File f = new File(Config.DATAPACK_ROOT, "data/html-" + lang.getShortName() + "/" + file);
		if(f.exists())
			try
			{
				cache = FileUtils.readFileToString(f, "UTF-8");
				cache = HtmlUtils.bbParse(cache);
			}
			catch(IOException e)
			{
				_log.info("HtmCache: File error: " + file + " lang: " + lang);
			}
		return cache;
	}

	private String loadLazy(Language lang, String file)
	{
		String cache = null;
		File f = new File(Config.DATAPACK_ROOT, "data/html-" + lang.getShortName() + "/" + file);
		if(f.exists())
			try
			{
				cache = FileUtils.readFileToString(f, "UTF-8");
				cache = HtmlUtils.bbParse(cache);

				_cache[lang.ordinal()].put(new Element(file, cache));
			}
			catch(IOException e)
			{
				_log.info("HtmCache: File error: " + file + " lang: " + lang);
			}
		return cache;
	}

	private String get(Language lang, String f)
	{
		Element element = _cache[lang.ordinal()].get(f);

		if(element == null)
			element = _cache[Language.RUSSIAN.ordinal()].get(f);

		return element == null ? null : (String) element.getObjectValue();
	}

	public void clear()
	{
		for(int i = 0; i < _cache.length; i++)
			_cache[i].removeAll();
	}
}
