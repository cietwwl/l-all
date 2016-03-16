package org.mmocore.gameserver.model.items;

import org.mmocore.gameserver.model.base.Element;


public class ItemAttributes
{
	private int _fire;
	private int _water;
	private int _wind;
	private int _earth;
	private int _holy;
	private int _unholy;
	private int _lockValue = -1;

	public ItemAttributes()
	{
		this(0, 0, 0, 0, 0, 0, -1);
	}

	public ItemAttributes(int fire, int water, int wind, int earth, int holy, int unholy)
	{
		this(fire, water, wind, earth, holy, unholy, -1);
	}

	public ItemAttributes(int fire, int water, int wind, int earth, int holy, int unholy, int lockValue)
	{
		_fire = fire;
		_water = water;
		_wind = wind;
		_earth = earth;
		_holy = holy;
		_unholy = unholy;
		_lockValue = lockValue;
	}

	public int getFire() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _fire;
	}

	public void setFire(int fire)
	{
		_fire = fire;
	}

	public int getWater() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _water;
	}

	public void setWater(int water)
	{
		_water = water;
	}

	public int getWind() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _wind;
	}

	public void setWind(int wind)
	{
		_wind = wind;
	}

	public int getEarth() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _earth;
	}

	public void setEarth(int earth)
	{
		_earth = earth;
	}

	public int getHoly() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _holy;
	}

	public void setHoly(int holy)
	{
		_holy = holy;
	}

	public int getUnholy() // возвращает незаблокированное значение атрибута, использовать только для сохранения и отладки
	{
		return _unholy;
	}

	public void setUnholy(int unholy)
	{
		_unholy = unholy;
	}

	public Element getElement()
	{
		if(_fire > 0)
			return Element.FIRE;
		else if(_water > 0)
			return Element.WATER;
		else if(_wind > 0)
			return Element.WIND;
		else if(_earth > 0)
			return Element.EARTH;
		else if(_holy > 0)
			return Element.HOLY;
		else if(_unholy > 0)
			return Element.UNHOLY;

		return Element.NONE;
	}

	public int getValue()
	{
		if(_fire > 0)
			return getLockedValue(_fire);
		else if(_water > 0)
			return getLockedValue(_water);
		else if(_wind > 0)
			return getLockedValue(_wind);
		else if(_earth > 0)
			return getLockedValue(_earth);
		else if(_holy > 0)
			return getLockedValue(_holy);
		else if(_unholy > 0)
			return getLockedValue(_unholy);

		return 0;
	}

	public void setValue(Element element, int value)
	{
		switch(element)
		{
			case FIRE:
				_fire = value;
				break;
			case WATER:
				_water = value;
				break;
			case WIND:
				_wind = value;
				break;
			case EARTH:
				_earth = value;
				break;
			case HOLY:
				_holy = value;
				break;
			case UNHOLY:
				_unholy = value;
				break;
		}
	}

	public int getValue(Element element)
	{
		switch(element)
		{
			case FIRE:
				return getLockedValue(_fire);
			case WATER:
				return getLockedValue(_water);
			case WIND:
				return getLockedValue(_wind);
			case EARTH:
				return getLockedValue(_earth);
			case HOLY:
				return getLockedValue(_holy);
			case UNHOLY:
				return getLockedValue(_unholy);
			default:
				return 0;
		}
	}

	private final int getLockedValue(int value)
	{
		if (value <= 0)
			return value;

		return _lockValue < 0 ? value : _lockValue;
	}

	public ItemAttributes clone()
	{
		return new ItemAttributes(_fire, _water, _wind, _earth, _holy, _unholy, _lockValue);
	}

	public boolean isLocked()
	{
		return _lockValue >= 0;
	}

	public void lockValue(int value)
	{
		_lockValue = value;
	}
}
