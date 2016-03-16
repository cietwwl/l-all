package org.mmocore.gameserver.network.l2.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Даный класс является обработчиком серверных интернациональных сообщений.
 */
public class CustomMessage implements IBroadcastPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CustomMessage.class);

	private static final String[] PARAMS = {"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}"};

	private String _address;
	private List<String> _args;

	public CustomMessage(String address)
	{
		_address = address;
	}

	public CustomMessage addString(String arg)
	{
		if(_args == null)
			_args = new ArrayList<String>();
		_args.add(arg);
		return this;
	}

	public CustomMessage addNumber(int i)
	{
		return addString(String.valueOf(i));
	}

	public CustomMessage addNumber(long l)
	{
		return addString(String.valueOf(l));
	}
	
	public String toString(Player player)
	{
		return toString(player.getLanguage());
	}
	
	public String toString(Language lang)
	{
		StrBuilder msg = null;

		String text = StringHolder.getInstance().getString(_address, lang);
		if(text != null)
		{
			msg = new StrBuilder(text);

			if(_args != null)
			{
				for(int i = 0; i < _args.size(); i++)
					msg.replaceFirst(PARAMS[i], _args.get(i));
			}
		}

		if(StringUtils.isEmpty(msg))
		{
			_log.warn("CustomMessage: string: " + _address + " not found for lang: " + lang + "!");
			return StringUtils.EMPTY;
		}

		return msg.toString();
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		return new SystemMessage(SystemMsg.S1).addString(toString(player));
	}
}