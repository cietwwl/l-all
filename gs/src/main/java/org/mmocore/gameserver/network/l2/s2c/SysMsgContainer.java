package org.mmocore.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.entity.residence.Residence;
import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.StaticObjectInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 14:27/08.03.2011
 */
public abstract class SysMsgContainer<T extends SysMsgContainer<T>> extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(SysMsgContainer.class);

	public static enum Types
	{
		TEXT, //0
		NUMBER,  //1
		NPC_NAME, //2
		ITEM_NAME, //3
		SKILL_NAME, //4
		RESIDENCE_NAME,   //5
		LONG,		 //6
		ZONE_NAME,	//7
		ITEM_NAME_WITH_AUGMENTATION,	//8
		ELEMENT_NAME,	//9
		INSTANCE_NAME,   //10  d
		STATIC_OBJECT_NAME,	//11
		PLAYER_NAME, //12 S
		SYSTEM_STRING, //13 d
		NPC_STRING //14 dsssss
	}

	protected SystemMsg _message;
	protected List<IArgument> _arguments;

	protected SysMsgContainer(SystemMsg message)
	{
		if(message == null)
			throw new IllegalArgumentException("SystemMsg is null");

		_message = message;
		_arguments = new ArrayList<IArgument>(_message.size());
	}

	protected void writeElements()
	{
		if(_message.size() != _arguments.size())
			Log.debug("Wrong count of arguments: " + _message, new Exception());

		writeD(_message.id());
		writeD(_arguments.size());
		for(IArgument argument : _arguments)
			argument.write(this);
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(_message.size() != _arguments.size())
		{
			Log.debug("Wrong count of arguments: " + _message, new Exception());
			return null;
		}
		else
			return this;
	}
	//==================================================================================================
	public T addName(GameObject object)
	{
		if(object == null)
			return add(new StringArgument(null));

		if(object.isNpc())
			return add(new NpcNameArgument(((NpcInstance)object).getNpcId() + 1000000));
		else if(object.isPlayer())
			return add(new PlayerNameArgument((Player)object));
		else if(object.isItem())
			return addItemName(((ItemInstance)object).getItemId());
		else if(object.isDoor())
			return add(new StaticObjectNameArgument(((DoorInstance)object).getDoorId()));
		else if(object instanceof Servitor)
			return add(new NpcNameArgument(((Servitor)object).getNpcId() + 1000000));
		else if(object instanceof StaticObjectInstance)
			return add(new StaticObjectNameArgument(((StaticObjectInstance)object).getUId()));

		return add(new StringArgument(object.getName()));
	}

	public T addInstanceName(int id)
	{
		return add(new InstanceNameArgument(id));
	}

	public T addSysString(int id)
	{
		return add(new SysStringArgument(id));
	}

	public T addNpcString(NpcString npcString, String... arg)
	{
		return add(new NpcStringArgument(npcString, arg));
	}

	public T addSkillName(SkillEntry skill)
	{
		return addSkillName(skill.getDisplayId(), skill.getDisplayLevel());
	}

	public T addSkillName(int id, int level)
	{
		return add(new SkillArgument(id, level));
	}

	public T addItemName(int item_id)
	{
		return add(new ItemNameArgument(item_id));
	}

	public T addItemNameWithAugmentation(ItemInstance item)
	{
		return add(new ItemNameWithAugmentationArgument(item.getItemId(), item.getAugmentations()));
	}

	public T addZoneName(Creature c)
	{
		return addZoneName(c.getX(), c.getY(), c.getZ());
	}

	public T addZoneName(Location loc)
	{
		return add(new ZoneArgument(loc.x, loc.y, loc.z));
	}

	public T addZoneName(int x, int y, int z)
	{
		return add(new ZoneArgument(x, y, z));
	}

	public T addResidenceName(Residence r)
	{
		return add(new ResidenceArgument(r.getId()));
	}

	public T addResidenceName(int i)
	{
		return add(new ResidenceArgument(i));
	}

	public T addElementName(int i)
	{
		return add(new ElementNameArgument(i));
	}

	public T addElementName(Element i)
	{
		return add(new ElementNameArgument(i.getId()));
	}

	public T addNumber(int i)
	{
		return add(new IntegerArgument(i));
	}

	public T addNumber(long i)
	{
		return add(new LongArgument(i));
	}

	public T addString(String t)
	{
		return add(new StringArgument(t));
	}

	@SuppressWarnings("unchecked")
	protected T add(IArgument arg)
	{
		_arguments.add(arg);

		return (T)this;
	}
	//==================================================================================================
	// Суппорт классы, собственна реализация (не L2jFree)
	//==================================================================================================

	private static abstract class IArgument
	{
		void write(SysMsgContainer<?> m)
		{
			m.writeD(getType().ordinal());

			writeData(m);
		}

		abstract Types getType();

		abstract void writeData(SysMsgContainer<?> message);
	}


	private static class IntegerArgument extends IArgument
	{
		private final int _data;

		public IntegerArgument(int da)
		{
			_data = da;
		}

		@Override
		public void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_data);
		}

		@Override
		Types getType()
		{
			return Types.NUMBER;
		}
	}

	private static class NpcNameArgument extends IntegerArgument
	{
		public NpcNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.NPC_NAME;
		}
	}

	private static class ItemNameArgument extends IntegerArgument
	{
		public ItemNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME;
		}
	}

	private static class ItemNameWithAugmentationArgument extends IArgument
	{
		private final int _itemId;
		private final int[] _augmentations;

		public ItemNameWithAugmentationArgument(int itemId, int[] augmentations)
		{
			_itemId = itemId;
			_augmentations = augmentations;
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME_WITH_AUGMENTATION;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_itemId);
			message.writeH(_augmentations[0]);
			message.writeH(_augmentations[1]);
		}
	}

	private static class InstanceNameArgument extends IntegerArgument
	{
		public InstanceNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.INSTANCE_NAME;
		}
	}

	private static class NpcStringArgument extends IntegerArgument
	{
		private final String[] _arguments = new String[5];

		public NpcStringArgument(NpcString npcString, String... arg)
		{
			super(npcString.getId());
			System.arraycopy(arg, 0, _arguments, 0, arg.length);
		}

		@Override
		public void writeData(SysMsgContainer<?> message)
		{
			super.writeData(message);
			for(String a : _arguments)
				message.writeS(a);
		}

		@Override
		Types getType()
		{
			return Types.NPC_STRING;
		}
	}

	private static class SysStringArgument extends IntegerArgument
	{
		public SysStringArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.SYSTEM_STRING;
		}
	}

	private static class ResidenceArgument extends IntegerArgument
	{
		public ResidenceArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.RESIDENCE_NAME;
		}
	}

	private static class StaticObjectNameArgument extends IntegerArgument
	{
		public StaticObjectNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.STATIC_OBJECT_NAME;
		}
	}

	private static class LongArgument extends IArgument
	{
		private final long _data;

		public LongArgument(long da)
		{
			_data = da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeQ(_data);
		}

		@Override
		Types getType()
		{
			return Types.LONG;
		}
	}

	private static class StringArgument extends IArgument
	{
		private final String _data;

		public StringArgument(String da)
		{
			_data = da == null ? "null" : da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeS(_data);
		}

		@Override
		Types getType()
		{
			return Types.TEXT;
		}
	}

	private static class SkillArgument extends IArgument
	{
		private final int _skillId;
		private final int _skillLevel;

		public SkillArgument(int t1, int t2)
		{
			_skillId = t1;
			_skillLevel = t2;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_skillId);
			message.writeD(_skillLevel);
		}

		@Override
		Types getType()
		{
			return Types.SKILL_NAME;
		}
	}

	private static class ZoneArgument extends IArgument
	{
		private final int _x;
		private final int _y;
		private final int _z;

		public ZoneArgument(int t1, int t2, int t3)
		{
			_x = t1;
			_y = t2;
			_z = t3;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_x);
			message.writeD(_y);
			message.writeD(_z);
		}

		@Override
		Types getType()
		{
			return Types.ZONE_NAME;
		}
	}

	private static class ElementNameArgument extends IntegerArgument
	{
		public ElementNameArgument(int type)
		{
			super(type);
		}

		@Override
		Types getType()
		{
			return Types.ELEMENT_NAME;
		}
	}

	private static class PlayerNameArgument extends StringArgument
	{
		public PlayerNameArgument(Player player)
		{
			super(player.isCursedWeaponEquipped() ? player.getTransformationName() : player.getName());
		}

		@Override
		Types getType()
		{
			return Types.PLAYER_NAME;
		}
	}
}
