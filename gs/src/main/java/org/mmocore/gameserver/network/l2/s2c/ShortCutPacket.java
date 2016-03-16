package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.skills.TimeStamp;

/**
 * @author VISTALL
 * @date 7:48/29.03.2011
 */
public abstract class ShortCutPacket extends L2GameServerPacket
{
	public static ShortcutInfo convert(Player player, ShortCut shortCut)
	{
		ShortcutInfo shortcutInfo = null;
		int page = shortCut.getSlot() + shortCut.getPage() * 12;
		switch(shortCut.getType())
		{
			case ShortCut.TYPE_ITEM:
				int reuseGroup = -1, currentReuse = 0, reuse = 0;
				int[] augmentationId = ItemInstance.EMPTY_AUGMENTATIONS;
				ItemInstance item = player.getInventory().getItemByObjectId(shortCut.getId());
				if(item != null)
				{
					augmentationId = item.getAugmentations();
					reuseGroup = item.getTemplate().getDisplayReuseGroup();
					if(item.getTemplate().getReuseDelay() > 0)
					{
						TimeStamp timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
						if(timeStamp != null)
						{
							currentReuse = (int)Math.round(timeStamp.getReuseCurrent() / 1000.);
							reuse = (int)Math.round(timeStamp.getReuseBasic() / 1000.);
						}
					}
				}
				shortcutInfo = new ItemShortcutInfo(shortCut.getType(), page, shortCut.getId(), reuseGroup, currentReuse, reuse, augmentationId, shortCut.getCharacterType());
				break;
			case ShortCut.TYPE_SKILL:
				shortcutInfo = new SkillShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getDisplayLevel(), shortCut.getCharacterType());
				break;
			default:
				shortcutInfo = new ShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getCharacterType());
				break;
		}
		return shortcutInfo;
	}

	protected static class ItemShortcutInfo extends ShortcutInfo
	{
		private int _reuseGroup;
		private int _currentReuse;
		private int _basicReuse;
		private int[] _augmentations;

		public ItemShortcutInfo(int type, int page, int id, int reuseGroup, int currentReuse, int basicReuse, int[] augmentations, int characterType)
		{
			super(type, page, id, characterType);
			_reuseGroup = reuseGroup;
			_currentReuse = currentReuse;
			_basicReuse = basicReuse;
			_augmentations = augmentations;
		}

		@Override
		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_characterType);
			p.writeD(_reuseGroup);
			p.writeD(_currentReuse);
			p.writeD(_basicReuse);
			p.writeH(_augmentations[0]);
			p.writeH(_augmentations[1]);
		}
	}

	protected static class SkillShortcutInfo extends ShortcutInfo
	{
		private final int _level;

		public SkillShortcutInfo(int type, int page, int id, int level, int characterType)
		{
			super(type, page, id, characterType);
			_level = level;
		}

		public int getLevel()
		{
			return _level;
		}

		@Override
		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_level);
			p.writeC(0x00);
			p.writeD(_characterType);
		}
	}

	protected static class ShortcutInfo
	{
		protected final int _type;
		protected final int _page;
		protected final int _id;
		protected final int _characterType;

		public ShortcutInfo(int type, int page, int id, int characterType)
		{
			_type = type;
			_page = page;
			_id = id;
			_characterType = characterType;
		}

		protected void write(ShortCutPacket p)
		{
			p.writeD(_type);
			p.writeD(_page);
			write0(p);
		}

		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_characterType);
		}
	}
}
