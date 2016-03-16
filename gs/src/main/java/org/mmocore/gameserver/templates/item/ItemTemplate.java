package org.mmocore.gameserver.templates.item;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.commons.time.cron.SchedulingPattern;
import org.mmocore.gameserver.handler.items.IItemHandler;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.stats.StatTemplate;
import org.mmocore.gameserver.stats.conditions.Condition;
import org.mmocore.gameserver.stats.funcs.FuncTemplate;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;
import org.mmocore.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<BR>
 * Mother class of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI>
 */
public abstract class ItemTemplate extends StatTemplate
{
	public static enum ReuseType
	{
		NORMAL(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME)
				{

					@Override
					public long next(ItemInstance item)
					{
						return System.currentTimeMillis() + item.getTemplate().getReuseDelay();
					}
				},
		EVERY_DAY_AT_6_30(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_S1S_REUSE_TIME)
				{
					private final SchedulingPattern _pattern = new SchedulingPattern("30 6 * * *");

					@Override
					public long next(ItemInstance item)
					{
						return _pattern.next(System.currentTimeMillis());
					}
				};

		private SystemMsg[] _messages;

		ReuseType(SystemMsg... msg)
		{
			_messages = msg;
		}

		public abstract long next(ItemInstance item);

		public SystemMsg[] getMessages()
		{
			return _messages;
		}
	}

	public static enum ItemClass
	{
		ALL,
		WEAPON,
		ARMOR,
		JEWELRY,
		ACCESSORY,
		/** Soul/Spiritshot, Potions, Scrolls */
		CONSUMABLE,
		/** Common craft matherials */
		MATHERIALS,
		/** Special (item specific) craft matherials */
		PIECES,
		/** Crafting recipies */
		RECIPIES,
		/** Skill learn books */
		SPELLBOOKS,
		/** Dyes, lifestones */
		MISC,
		/** All other */
		OTHER
	}

	public static final int ITEM_ID_PC_BANG_POINTS = -100;
	public static final int ITEM_ID_CLAN_REPUTATION_SCORE = -200;
	public static final int ITEM_ID_FAME = -300;
	public static final int ITEM_ID_ADENA = 57;

	/** Item ID для замковых корон */
	public static final int[] ITEM_ID_CASTLE_CIRCLET = { 0, // no castle - no circlet.. :)
			6838, // Circlet of Gludio
			6835, // Circlet of Dion
			6839, // Circlet of Giran
			6837, // Circlet of Oren
			6840, // Circlet of Aden
			6834, // Circlet of Innadril
			6836, // Circlet of Goddard
			8182, // Circlet of Rune
			8183, // Circlet of Schuttgart
	};

	public static final int ITEM_ID_FORMAL_WEAR = 6408;

	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_OTHER = 2;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;

	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int TYPE2_PET_WOLF = 6;
	public static final int TYPE2_PET_HATCHLING = 7;
	public static final int TYPE2_PET_STRIDER = 8;
	public static final int TYPE2_NODROP = 9;
	public static final int TYPE2_PET_GWOLF = 10;
	public static final int TYPE2_PENDANT = 11;
	public static final int TYPE2_PET_BABY = 12;

	public static final int SLOT_NONE = 0x00000;
	public static final int SLOT_UNDERWEAR = 0x00001;

	public static final int SLOT_R_EAR = 0x00002;
	public static final int SLOT_L_EAR = 0x00004;

	public static final int SLOT_NECK = 0x00008;

	public static final int SLOT_R_FINGER = 0x00010;
	public static final int SLOT_L_FINGER = 0x00020;

	public static final int SLOT_HEAD = 0x00040;
	public static final int SLOT_R_HAND = 0x00080;
	public static final int SLOT_L_HAND = 0x00100;
	public static final int SLOT_GLOVES = 0x00200;
	public static final int SLOT_CHEST = 0x00400;
	public static final int SLOT_LEGS = 0x00800;
	public static final int SLOT_FEET = 0x01000;
	public static final int SLOT_BACK = 0x02000;
	public static final int SLOT_LR_HAND = 0x04000;
	public static final int SLOT_FULL_ARMOR = 0x08000;
	public static final int SLOT_HAIR = 0x10000;
	public static final int SLOT_FORMAL_WEAR = 0x20000;
	public static final int SLOT_DHAIR = 0x40000;
	public static final int SLOT_HAIRALL = 0x80000;
	public static final int SLOT_R_BRACELET = 0x100000;
	public static final int SLOT_L_BRACELET = 0x200000;
	public static final int SLOT_DECO = 0x400000;
	public static final int SLOT_BELT = 0x10000000;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GWOLF = -104;
	public static final int SLOT_PENDANT = -105;

	// Все слоты, используемые броней.
	public static final int SLOTS_ARMOR = SLOT_HEAD|SLOT_L_HAND|SLOT_GLOVES|SLOT_CHEST|SLOT_LEGS|SLOT_FEET|SLOT_BACK|SLOT_FULL_ARMOR;
	// Все слоты, используемые бижей.
	public static final int SLOTS_JEWELRY = SLOT_R_EAR|SLOT_L_EAR|SLOT_NECK|SLOT_R_FINGER|SLOT_L_FINGER;

	public static final int CRYSTAL_NONE = 0;
	public static final int CRYSTAL_D = 1458;
	public static final int CRYSTAL_C = 1459;
	public static final int CRYSTAL_B = 1460;
	public static final int CRYSTAL_A = 1461;
	public static final int CRYSTAL_S = 1462;

	public static enum Grade
	{
		NONE(CRYSTAL_NONE, 0),
		D(CRYSTAL_D, 1),
		C(CRYSTAL_C, 2),
		B(CRYSTAL_B, 3),
		A(CRYSTAL_A, 4),
		S(CRYSTAL_S, 5),
		S80(CRYSTAL_S, 5),
		S84(CRYSTAL_S, 5);

		/** ID соответствующего грейду кристалла */
		public final int cry;
		/** ID грейда, без учета уровня S */
		public final int externalOrdinal;

		private Grade(int crystal, int ext)
		{
			cry = crystal;
			externalOrdinal = ext;
		}
	}

	public static final int ATTRIBUTE_NONE = -2;
	public static final int ATTRIBUTE_FIRE = 0;
	public static final int ATTRIBUTE_WATER = 1;
	public static final int ATTRIBUTE_WIND = 2;
	public static final int ATTRIBUTE_EARTH = 3;
	public static final int ATTRIBUTE_HOLY = 4;
	public static final int ATTRIBUTE_DARK = 5;

	protected final int _itemId;
	private final ItemClass _class;
	protected final String _name;
	protected final String _addname;
	protected final String _icon;
	protected final String _icon32;
	protected int _type1; // needed for item list (inventory)
	protected int _type2; // different lists for armor, weapon, etc
	private final int _weight;

	protected final Grade _crystalType; // default to none-grade

	private final int _durability;
	protected int _bodyPart;
	private final int _referencePrice;
	private final int _crystalCount;

	private final boolean _temporal;
	private final boolean _stackable;
	private final boolean _magicWeapon;
	
	private final boolean _petFood;
	private final boolean _hideConsumeMessage;

	private final int _flags;

	private final ReuseType _reuseType;
	private final int _reuseDelay;
	private final int _reuseGroup;
	private final int _agathionEnergy;
	private final int _equipReuseDelay;

	protected SkillEntry[] _skills;
	private SkillEntry _enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	public ItemType type;

	private int[] _baseAttributes = new int[6];
	private IntObjectMap<int[]> _enchantOptions = Containers.emptyIntObjectMap();

	private Condition[] _conditions = Condition.EMPTY_ARRAY;
	private IItemHandler _handler = IItemHandler.DEFAULT;

	private IntObjectMap<AugmentationInfo> _augmentationInfos = Containers.emptyIntObjectMap();
	/**
	 * Constructor<?> of the L2Item that fill class variables.<BR><BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>type</LI>
	 * <LI>_itemId</LI>
	 * <LI>_name</LI>
	 * <LI>_type1 & _type2</LI>
	 * <LI>_weight</LI>
	 * <LI>_crystallizable</LI>
	 * <LI>_stackable</LI>
	 * <LI>_materialType & _crystalType & _crystlaCount</LI>
	 * <LI>_durability</LI>
	 * <LI>_bodypart</LI>
	 * <LI>_referencePrice</LI>
	 * <LI>_sellable</LI>

	 * @param set : StatsSet corresponding to a set of couples (key,value) for description of the item
	 */
	protected ItemTemplate(final StatsSet set)
	{
		_itemId = set.getInteger("item_id");
		_class = set.getEnum("class", ItemClass.class, ItemClass.OTHER);
		_name = set.getString("name");
		_addname = set.getString("add_name", "");
		_icon = set.getString("icon", "");
		_icon32 = "<img src=icon." + _icon + " width=32 height=32>";
		_weight = set.getInteger("weight", 0);
		_stackable = set.getBool("stackable", false);
		_crystalType = set.getEnum("crystal_type", Grade.class, Grade.NONE); // default to none-grade
		_durability = set.getInteger("durability", -1);
		_temporal = set.getBool("temporal", false);
		_bodyPart = set.getInteger("bodypart", 0);
		_referencePrice = set.getInteger("price", 0);
		_crystalCount = set.getInteger("crystal_count", 0);
		_reuseType = set.getEnum("reuse_type", ReuseType.class, ReuseType.NORMAL);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_reuseGroup = set.getInteger("delay_share_group", -_itemId);
		_agathionEnergy = set.getInteger("agathion_energy", 0);
		_equipReuseDelay = set.getInteger("equip_reuse_delay", -1);
		_magicWeapon = set.getBool("is_magic_weapon", false);
		_petFood = set.getBool("is_pet_food", false);
		_hideConsumeMessage = set.getBool("hide_consume_message", false);

		int flags = 0;
		for(ItemFlags f : ItemFlags.VALUES)
		{
			if(set.getBool(f.lcname(), f.getDefaultValue()))
				flags |= f.mask();
		}

		_flags = flags;
		_funcTemplates = FuncTemplate.EMPTY_ARRAY;
		_skills = SkillEntry.EMPTY_ARRAY;
	}

	/**
	 * Returns the itemType.
	 * @return Enum
	 */
	public ItemType getItemType()
	{
		return type;
	}

	public String getIcon()
	{
		return _icon;
	}

	/**
	 * Возвращает готовую для отображения в html строку вида
	 * <img src=icon.иконка width=32 height=32>
	 */
	public String getIcon32()
	{
		return _icon32;
	}

	/**
	 * Returns the durability of th item
	 * @return int
	 */
	public final int getDurability()
	{
		return _durability;
	}

	public final boolean isTemporal()
	{
		return _temporal;
	}

	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public final int getItemId()
	{
		return _itemId;
	}

	public abstract long getItemMask();

	/**
	 * Returns the type 2 of the item
	 * @return int
	 */
	public final int getType2()
	{
		return _type2;
	}

	public final int getBaseAttributeValue(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return _baseAttributes[element.getId()];
	}

	public void setBaseAtributeElements(int[] val)
	{
		_baseAttributes = val;
	}

	public final int getType2ForPackets()
	{
		int type2 = _type2;
		switch(_type2)
		{
			case TYPE2_PET_WOLF:
			case TYPE2_PET_HATCHLING:
			case TYPE2_PET_STRIDER:
			case TYPE2_PET_GWOLF:
			case TYPE2_PET_BABY:
				if(_bodyPart == ItemTemplate.SLOT_CHEST)
					type2 = TYPE2_SHIELD_ARMOR;
				else
					type2 = TYPE2_WEAPON;
				break;
			case TYPE2_PENDANT:
				type2 = TYPE2_ACCESSORY;
				break;
		}
		return type2;
	}

	/**
	 * Returns the weight of the item
	 * @return int
	 */
	public final int getWeight()
	{
		return _weight;
	}

	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public final Grade getCrystalType()
	{
		return _crystalType;
	}

	/**
	 * Returns the grade of the item.<BR><BR>
	 * <U><I>Concept :</I></U><BR>
	 * In fact, this fucntion returns the type of crystal of the item.
	 * @return int
	 */
	public final Grade getItemGrade()
	{
		return getCrystalType();
	}

	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _crystalCount;
	}

	public abstract int getCrystalCount(int enchantLevel, boolean enchantFail);

	/**
	 * Returns the name of the item
	 * @return String
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Returns the additional name of the item
	 * @return String
	 */
	public final String getAdditionalName()
	{
		return _addname;
	}

	/**
	 * Return the part of the body used with the item.
	 * @return int
	 */
	public final int getBodyPart()
	{
		return _bodyPart;
	}

	/**
	 * Returns the type 1 of the item
	 * @return int
	 */
	public final int getType1()
	{
		return _type1;
	}

	/**
	 * Returns if the item is stackable
	 * @return boolean
	 */
	public final boolean isStackable()
	{
		return _stackable;
	}

	/**
	 * Returns the price of reference of the item
	 * @return int
	 */
	public final int getReferencePrice()
	{
		return _referencePrice;
	}

	/**
	 * Returns if item is for hatchling
	 * @return boolean
	 */
	public boolean isForHatchling()
	{
		return _type2 == TYPE2_PET_HATCHLING;
	}

	/**
	 * Returns if item is for strider
	 * @return boolean
	 */
	public boolean isForStrider()
	{
		return _type2 == TYPE2_PET_STRIDER;
	}

	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForWolf()
	{
		return _type2 == TYPE2_PET_WOLF;
	}

	public boolean isForPetBaby()
	{
		return _type2 == TYPE2_PET_BABY;
	}

	/**
	 * Returns if item is for great wolf
	 * @return boolean
	 * [pchayka] GWolfs can wear wolf's armor&weapon
	 */
	public boolean isForGWolf()
	{
		return _type2 == TYPE2_PET_GWOLF || _type2 == TYPE2_PET_WOLF;
	}

	/**
	 *  Магическая броня для петов
	 */
	public boolean isPendant()
	{
		return _type2 == TYPE2_PENDANT;
	}

	public boolean isForPet()
	{
		return _type2 == TYPE2_PENDANT || _type2 == TYPE2_PET_HATCHLING || _type2 == TYPE2_PET_WOLF || _type2 == TYPE2_PET_STRIDER || _type2 == TYPE2_PET_GWOLF || _type2 == TYPE2_PET_BABY;
	}

	/**
	 * Add the L2Skill skill to the list of skills generated by the item
	 * @param skill : L2Skill
	 */
	public void attachSkill(SkillEntry skill)
	{
		_skills = ArrayUtils.add(_skills, skill);
	}

	public SkillEntry[] getAttachedSkills()
	{
		return _skills;
	}

	public SkillEntry getFirstSkill()
	{
		if(_skills.length > 0)
			return _skills[0];
		return null;
	}

	/**
	 * @return skill that player get when has equipped weapon +4  or more  (for duals SA)
	 */
	public SkillEntry getEnchant4Skill()
	{
		return _enchant4Skill;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getItemId());
		sb.append(" - ");
		sb.append(getName());
		if(!getAdditionalName().isEmpty())
		{
			sb.append(' ');
			sb.append('<').append(getAdditionalName()).append('>');
		}

		return sb.toString();
	}

	/**
	 * Определяет призрачный предмет или нет
	 * @return true, если предмет призрачный
	 */
	public boolean isShadowItem()
	{
		return _durability > 0 && !isTemporal();
	}

	public boolean isCommonItem()
	{
		return _name.startsWith("Common Item - ");
	}

	public boolean isSealedItem()
	{
		return _name.startsWith("Sealed");
	}

	public boolean isAltSeed()
	{
		return _name.contains("Alternative");
	}

	public ItemClass getItemClass()
	{
		return _class;
	}

	/**
	 * Является ли вещь аденой или камнем печати
	 */
	public boolean isAdena()
	{
		return _itemId == ItemTemplate.ITEM_ID_ADENA || _itemId == 6360 || _itemId == 6361 || _itemId == 6362;
	}

	public boolean isEquipment()
	{
		return _type1 != TYPE1_ITEM_QUESTITEM_ADENA;
	}

	public boolean isKeyMatherial()
	{
		return _class == ItemClass.PIECES;
	}

	public boolean isRecipe()
	{
		return _class == ItemClass.RECIPIES;
	}

	public boolean isTerritoryAccessory()
	{
		return _itemId >= 13740 && _itemId <= 13748 || _itemId >= 14592 && _itemId <= 14600 || _itemId >= 14664 && _itemId <= 14672 || _itemId >= 14801 && _itemId <= 14809 || _itemId >= 15282 && _itemId <= 15299;
	}

	public boolean isArrow()
	{
		return type == EtcItemType.ARROW;
	}

	public boolean isBelt()
	{
		return _bodyPart == SLOT_BELT;
	}

	public boolean isBracelet()
	{
		return _bodyPart == SLOT_R_BRACELET || _bodyPart == SLOT_L_BRACELET;
	}

	public boolean isUnderwear()
	{
		return _bodyPart == SLOT_UNDERWEAR;
	}

	public boolean isCloak()
	{
		return _bodyPart == SLOT_BACK;
	}

	public boolean isTalisman()
	{
		return _bodyPart == SLOT_DECO;
	}

	public boolean isHerb()
	{
		return type == EtcItemType.HERB;
	}

	public boolean isAttributeCrystal()
	{
		return _itemId == 9552 || _itemId == 9553 || _itemId == 9554 || _itemId == 9555 || _itemId == 9556 || _itemId == 9557;
	}

	public boolean isHeroWeapon()
	{
		return _itemId >= 6611 && _itemId <= 6621 || _itemId >= 9388 && _itemId <= 9390;
	}

	public boolean isCursed()
	{
		return CursedWeaponsManager.getInstance().isCursed(_itemId);
	}

	public boolean isMercenaryTicket()
	{
		return type == EtcItemType.MERCENARY_TICKET;
	}

	public boolean isRod()
	{
		return getItemType() == WeaponType.ROD;
	}

	public boolean isWeapon()
	{
		return getType2() == ItemTemplate.TYPE2_WEAPON;
	}

	public boolean isArmor()
	{
		return getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR;
	}

	public boolean isAccessory()
	{
		return getType2() == ItemTemplate.TYPE2_ACCESSORY;
	}

	public boolean isQuest()
	{
		return getType2() == ItemTemplate.TYPE2_QUEST;
	}

	/**
	 * gradeCheck - использовать пока не перепишется система заточки
	 * @param gradeCheck
	 * @return
	 */
	public boolean canBeEnchanted(@Deprecated boolean gradeCheck)
	{
		if(gradeCheck && getCrystalType() == Grade.NONE)
			return false;

		if(isCursed())
			return false;

		if(isQuest()) // DS: проверить и убрать
			return false;

		return isEnchantable();
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return getItemType() == EtcItemType.BAIT || getItemType() == EtcItemType.ARROW || getItemType() == EtcItemType.BOLT || !(getBodyPart() == 0 || this instanceof EtcItemTemplate);
	}

	public void setEnchant4Skill(SkillEntry enchant4Skill)
	{
		_enchant4Skill = enchant4Skill;
	}

	public boolean testCondition(Playable player, ItemInstance instance, boolean showMessage)
	{
		if(_conditions.length == 0)
			return true;

		Env env = new Env();
		env.character = player;
		env.item = instance;

		for (Condition con : _conditions)
		{
			if (!con.test(env))
			{
				if(showMessage && con.getSystemMsg() != null)
				{
					if(con.getSystemMsg().size() > 0)
						player.sendPacket(new SystemMessage(con.getSystemMsg()).addItemName(getItemId()));
					else
						player.sendPacket(con.getSystemMsg());
				}
				return false;
			}
		}

		return true;
	}

	public void addCondition(Condition condition)
	{
		_conditions = ArrayUtils.add(_conditions, condition);
	}

	/**
	 * Returns if the item is crystallizable
	 * @return boolean
	 */
	public final boolean isCrystallizable()
	{
		return getFlag(ItemFlags.CRYSTALLIZABLE);
	}

	public boolean isEnchantable()
	{
		return getFlag(ItemFlags.ENCHANTABLE);
	}

	public boolean isTradeable()
	{
		return getFlag(ItemFlags.TRADEABLE);
	}

	public boolean isDestroyable()
	{
		return getFlag(ItemFlags.DESTROYABLE);
	}

	public boolean isDropable()
	{
		return getFlag(ItemFlags.DROPABLE);
	}

	public final boolean isSellable()
	{
		return getFlag(ItemFlags.SELLABLE);
	}

	public final boolean isAttributable()
	{
		return getFlag(ItemFlags.ATTRIBUTABLE);
	}

	public final boolean isStoreable()
	{
		return getFlag(ItemFlags.STOREABLE);
	}

	public final boolean isFreightable()
	{
		return getFlag(ItemFlags.FREIGHTABLE);
	}

	private boolean getFlag(ItemFlags f)
	{
		return (_flags & f.mask()) == f.mask();
	}

	public IItemHandler getHandler()
	{
		return _handler;
	}

	public void setHandler(IItemHandler handler)
	{
		_handler = handler;
	}

	public int getReuseDelay()
	{
		return _reuseDelay;
	}

	public int getReuseGroup()
	{
		return _reuseGroup;
	}

	public int getDisplayReuseGroup()
	{
		return _reuseGroup < 0 ? -1 : _reuseGroup;
	}

	public int getAgathionEnergy()
	{
		return _agathionEnergy;
	}

	public int getEquipReuseDelay()
	{
		return _equipReuseDelay;
	}

	public void addEnchantOptions(int level, int[] options)
	{
		if(_enchantOptions.isEmpty())
			_enchantOptions = new HashIntObjectMap<int[]>();

		_enchantOptions.put(level, options);
	}

	public IntObjectMap<int[]> getEnchantOptions()
	{
		return _enchantOptions;
	}

	public ReuseType getReuseType()
	{
		return _reuseType;
	}

	public boolean isMagicWeapon()
	{
		return _magicWeapon;
	}

	public boolean isPetFood()
	{
		return _petFood;
	}

	public boolean isHideConsumeMessage()
	{
		return _hideConsumeMessage;
	}

	public void addAugmentationInfo(AugmentationInfo augmentationInfo)
	{
		if(_augmentationInfos.isEmpty())
			_augmentationInfos = new HashIntObjectMap<AugmentationInfo>();

		_augmentationInfos.put(augmentationInfo.getMineralId(), augmentationInfo);
	}

	public IntObjectMap<AugmentationInfo> getAugmentationInfos()
	{
		return _augmentationInfos;
	}
}