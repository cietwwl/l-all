package org.mmocore.gameserver.scripts;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ClassUtils;
import org.mmocore.commons.compiler.Compiler;
import org.mmocore.commons.compiler.MemoryClassLoader;
import org.mmocore.commons.listener.Listener;
import org.mmocore.commons.listener.ListenerList;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.bypass.Bypass;
import org.mmocore.gameserver.handler.bypass.BypassHolder;
import org.mmocore.gameserver.listener.ScriptListener;
import org.mmocore.gameserver.listener.script.OnInitScriptListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scripts
{
	public class ScriptListenerImpl extends ListenerList<Scripts>
	{
		public void init()
		{
			for(Listener<Scripts> listener : getListeners())
				if(OnInitScriptListener.class.isInstance(listener))
					((OnInitScriptListener) listener).onInit();
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Scripts.class);

	private static final Scripts _instance = new Scripts();

	public static Scripts getInstance()
	{
		return _instance;
	}

	private final Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();
	private final ScriptListenerImpl _listeners = new ScriptListenerImpl();

	private Scripts()
	{
		load();
	}

	/**
	 * Вызывается при загрузке сервера. Загрузает все скрипты в data/scripts. Не инициирует объекты и обработчики.
	 *
	 * @return true, если загрузка прошла успешно
	 */
	private void load()
	{
		_log.info("Scripts: Loading...");

		List<Class<?>> classes = load(new File(Config.DATAPACK_ROOT, "data/scripts"));
		if(classes.isEmpty())
		{
			throw new Error("Failed loading scripts!");
		}

		_log.info("Scripts: Loaded " + classes.size() + " classes.");

		Class<?> clazz;
		for(int i = 0; i < classes.size(); i++)
		{
			clazz = classes.get(i);
			_classes.put(clazz.getName(), clazz);
		}
	}

	/**
	 * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
	 */
	public void init()
	{
		for(Class<?> clazz : _classes.values())
			init(clazz);

		_listeners.init();
	}

	/**
	 * Загрузить все классы в data/scripts/target
	 *
	 * @param target путь до класса, или каталога со скриптами
	 * @return список загруженых скриптов
	 */
	public List<Class<?>> load(File target)
	{
		Collection<File> scriptFiles = Collections.emptyList();

		if(target.isFile())
		{
			scriptFiles = new ArrayList<File>(1);
			scriptFiles.add(target);
		}
		else if(target.isDirectory())
		{
			scriptFiles = FileUtils.listFiles(target, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
		}

		if(scriptFiles.isEmpty())
			return Collections.emptyList();

		List<Class<?>> classes = new ArrayList<Class<?>>();
		Compiler compiler = new Compiler();

		if(compiler.compile(scriptFiles))
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				//Вложенные класс
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;

				try
				{
					Class<?> clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;
					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					_log.error("Scripts: Can't load script class: " + name, e);
					classes.clear();
					break;
				}
			}
		}

		return classes;
	}


	private Object init(Class<?> clazz)
	{
		Object o = null;

		try
		{
			if(ClassUtils.isAssignable(clazz, ScriptListener.class))
			{
				o = clazz.newInstance();

				_listeners.add((ScriptListener)o);
			}

			for(Method method : clazz.getMethods())
				if(method.isAnnotationPresent(Bypass.class))
				{
					Bypass an = method.getAnnotation(Bypass.class);
					if(o == null)
						o = clazz.newInstance();
					Class[] par = method.getParameterTypes();
					if(par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class)
					{
						_log.error("Wrong parameters for bypass method: " + method.getName() + ", class: " + clazz.getSimpleName());
						continue;
					}

					BypassHolder.getInstance().registerBypass(an.value(), o, method);
				}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return o;
	}


	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}
}