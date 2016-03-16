package org.mmocore.gameserver.templates.item;

import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.stats.funcs.FuncTemplate;
import org.mmocore.gameserver.templates.StatsSet;

public final class WeaponTemplate extends ItemTemplate
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _kamaelConvert;
	private final int _rndDam;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _enchantBonus;
	private int _critical;

	public enum WeaponType implements ItemType
	{
		NONE(1, "Shield", null),
		SWORD(2, "Sword", Stats.SWORD_WPN_VULNERABILITY),
		BLUNT(3, "Blunt", Stats.BLUNT_WPN_VULNERABILITY),
		DAGGER(4, "Dagger", Stats.DAGGER_WPN_VULNERABILITY),
		BOW(5, "Bow", Stats.BOW_WPN_VULNERABILITY),
		POLE(6, "Pole", Stats.POLE_WPN_VULNERABILITY),
		ETC(7, "Etc", null),
		FIST(8, "Fist", Stats.FIST_WPN_VULNERABILITY),
		DUAL(9, "Dual Sword", Stats.DUAL_WPN_VULNERABILITY),
		DUALFIST(10, "Dual Fist", Stats.FIST_WPN_VULNERABILITY),
		BIGSWORD(11, "Big Sword", Stats.SWORD_WPN_VULNERABILITY), // Two Handed Swords
		PET(12, "Pet", Stats.FIST_WPN_VULNERABILITY),
		ROD(13, "Rod", null),
		BIGBLUNT(14, "Big Blunt", Stats.BLUNT_WPN_VULNERABILITY),
		CROSSBOW(15, "Crossbow", Stats.CROSSBOW_WPN_VULNERABILITY),
		RAPIER(16, "Rapier", Stats.DAGGER_WPN_VULNERABILITY),
		ANCIENTSWORD(17, "Ancient Sword", Stats.SWORD_WPN_VULNERABILITY), // Kamael 2h sword
		DUALDAGGER(18, "Dual Dagger", Stats.DAGGER_WPN_VULNERABILITY);

		public final static WeaponType[] VALUES = values();
		                               
		private final long _mask;
		private final String _name;
		private final Stats _defence;

		private WeaponType(int id, String name, Stats defence)
		{
			_mask = 1L << id;
			_name = name;
			_defence = defence;
		}

		public long mask()
		{
			return _mask;
		}

		public Stats getDefence()
		{
			return _defence;
		}

		@Override
		public String toString()
		{
			return _name;
		}
	}

	public WeaponTemplate(StatsSet set)
	{
		super(set);
		type = set.getEnum("type", WeaponType.class);
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_kamaelConvert = set.getInteger("kamael_convert", 0);

		_rndDam = set.getInteger("rnd_dam", 0);
		// DS: На практике, несмотря на различие во времени atk_reuse, все луки и арбалеты стреляют одинаково.
		// Может лучше в датапаке поменять ?
		_atkReuse = type == WeaponType.BOW ? 1500 : type == WeaponType.CROSSBOW ? 820 : set.getInteger("atk_reuse", 0);
		_mpConsume = set.getInteger("mp_consume", 0);

		if(getItemType() == WeaponType.NONE)
		{
			_type1 = TYPE1_SHIELD_ARMOR;
			_type2 = TYPE2_SHIELD_ARMOR;
		}
		else
		{
			_type1 = TYPE1_WEAPON_RING_EARRING_NECKLACE;
			_type2 = TYPE2_WEAPON;
		}

		if(getItemType() == WeaponType.PET)
		{
			_type1 = ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE;

			if(_bodyPart == ItemTemplate.SLOT_WOLF)
				_type2 = ItemTemplate.TYPE2_PET_WOLF;
			else if(_bodyPart == ItemTemplate.SLOT_GWOLF)
				_type2 = ItemTemplate.TYPE2_PET_GWOLF;
			else if(_bodyPart == ItemTemplate.SLOT_HATCHLING)
				_type2 = ItemTemplate.TYPE2_PET_HATCHLING;
			else
				_type2 = ItemTemplate.TYPE2_PET_STRIDER;

			_bodyPart = ItemTemplate.SLOT_R_HAND;
		}

		switch (getCrystalType())
		{
			case D:
				_enchantBonus = 90;
				break;
			case C:
				_enchantBonus = 45;
				break;
			case B:
				_enchantBonus = 67;
				break;
			case A:
				_enchantBonus = 145;
				break;
			case S:
			case S80:
			case S84:
				_enchantBonus = 250;
				break;
			default:
				_enchantBonus = 0;
		}
	}

	/**
	 * Returns the type of Weapon
	 * @return L2WeaponType
	 */
	@Override
	public WeaponType getItemType()
	{
		return (WeaponType) type;
	}

	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the Weapon
	 */
	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}

	/**
	 * Returns the quantity of SoulShot used.
	 * @return int
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	/**
	 * Returns the quatity of SpiritShot used.
	 * @return int
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}

	public int getCritical()
	{
		return _critical;
	}

	/**
	 * Returns the random damage inflicted by the weapon
	 * @return int
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}

	/**
	 * Return the Attack Reuse Delay of the L2Weapon.<BR><BR>
	 * @return int
	 */
	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}

	/**
	 * Returns the MP consumption with the weapon
	 * @return int
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * Возвращает разницу между длиной этого оружия и стандартной, то есть x-40
	 */
	public int getAttackRange()
	{
		switch(getItemType())
		{
			case BOW:
				return 460;
			case CROSSBOW:
				return 360;
			case POLE:
				return 40;
			default:
				return 0;
		}
	}

	@Override
	public void attachFunc(FuncTemplate f)
	{
		//TODO для параметров set с дп,может считать стат с L2ItemInstance? (VISTALL)
		if(f._stat == Stats.CRITICAL_BASE && f._order == 0x08)
		{
			_critical = (int)Math.round(f._value / 10);
		}
		super.attachFunc(f);
	}

	public int getKamaelConvert()
	{
		return _kamaelConvert;
	}

	@Override
	public final int getCrystalCount(int enchantLevel, boolean enchantFail)
	{
		int result = getCrystalCount();
		if (enchantFail)
			result /= 2;

		if (result > 0 && enchantLevel > 0)
		{
			result += _enchantBonus * enchantLevel;
			final int overEnchant = enchantLevel - 3;
			if (overEnchant > 0)
				result += _enchantBonus * overEnchant;
		}
		return result;
	}
}