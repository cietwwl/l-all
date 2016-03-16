package org.mmocore.gameserver.templates.item;

/**
 * @author VISTALL
 * @date 20:51/11.01.2011
 */
public enum ItemFlags
{
	DESTROYABLE(true),  // возможность уничтожить
	CRYSTALLIZABLE(false), // возможность разбить на кристалы
	DROPABLE(true),     // возможность дропнуть
	FREIGHTABLE(false),     // возможность передать в рамках аккаунта
	ENCHANTABLE(true),  // возможность заточить
	ATTRIBUTABLE(true), // возможность заточить атрибутом
	SELLABLE(true),     // возможность продать
	TRADEABLE(true),     // возможность передать
	STOREABLE(true);      // возможность положить в ВХ

	public static final ItemFlags[] VALUES = values();

	private final int _mask;
	private final boolean _defaultValue;
	private final String _name;

	ItemFlags(boolean defaultValue)
	{
		_defaultValue = defaultValue;
		_mask = 1 << ordinal();
		_name = name().toLowerCase();
	}

	public int mask()
	{
		return _mask;
	}

	public boolean getDefaultValue()
	{
		return _defaultValue;
	}

	public String lcname()
	{
		return _name;
	}
}
