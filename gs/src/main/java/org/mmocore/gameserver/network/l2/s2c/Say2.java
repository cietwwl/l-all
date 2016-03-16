package org.mmocore.gameserver.network.l2.s2c;

import java.util.regex.Matcher;

import org.mmocore.gameserver.cache.ItemInfoCache;
import org.mmocore.gameserver.data.client.holder.ItemNameLineHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.ItemInfo;
import org.mmocore.gameserver.network.l2.c2s.Say2C;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.components.SysString;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.templates.client.ItemNameLine;
import org.mmocore.gameserver.utils.Language;

public class Say2 extends NpcStringContainer implements IBroadcastPacket
{
	private Language _itemLinkLang;
	private ChatType _type;
	private SysString _sysString;
	private SystemMsg _systemMsg;

	private int _objectId;
	private String _charName;

	public Say2(int objectId, ChatType type, SysString st, SystemMsg sm)
	{
		super(NpcString.NONE);
		_objectId = objectId;
		_type = type;
		_sysString = st;
		_systemMsg = sm;
	}

	public Say2(int objectId, ChatType type, String charName, String text, Language itemLinkLang)
	{
		this(objectId, type, charName, NpcString.NONE, text);

		_itemLinkLang = itemLinkLang;
	}

	public Say2(int objectId, ChatType type, String charName, NpcString npcString, String... params)
	{
		super(npcString,  params);
		_objectId = objectId;
		_type = type;
		_charName = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4A);
		writeD(_objectId);
		writeD(_type.ordinal());
		switch(_type)
		{
			case SYSTEM_MESSAGE:
				writeD(_sysString.getId());
				writeD(_systemMsg.id());
				break;
			default:
				writeS(_charName);
				writeElements();
				break;
		}
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		Language lang = player.getLanguage();
		// если _itemLinkLang нулл(тоисть нету итем линков), или язык совпадает с языком игрока - возращаем this
		if(_itemLinkLang == null || _itemLinkLang == lang)
			return this;

		String text = null;
		Matcher m = Say2C.EX_ITEM_LINK_PATTERN.matcher(_parameters[0]);
		while(m.find())
		{
			int objectId = Integer.parseInt(m.group(1));
			ItemInfo itemInfo = ItemInfoCache.getInstance().get(objectId);
			if(itemInfo == null)
				return this;

			ItemNameLine line = ItemNameLineHolder.getInstance().get(lang, itemInfo.getItemId());
 			if(line != null)
			{
				String replace = line.getName();
				if(itemInfo.getAugmentationMineralId() > 0)
					replace = line.getAugmentName();

				text = (text == null ? _parameters[0] : text).replace(m.group(2), replace);
			}
		}

		return new Say2(_objectId, _type, _charName, _npcString, text);
	}
}