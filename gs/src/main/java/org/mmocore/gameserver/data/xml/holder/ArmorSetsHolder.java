package org.mmocore.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.model.ArmorSet;


public final class ArmorSetsHolder extends AbstractHolder
{
	private static final ArmorSetsHolder _instance = new ArmorSetsHolder();

	public static ArmorSetsHolder getInstance()
	{
		return _instance;
	}

	private List<ArmorSet> _armorSets = new ArrayList<ArmorSet>();

	public void addArmorSet(ArmorSet armorset)
	{
		_armorSets.add(armorset);
	}

	public ArmorSet getArmorSet(int chestItemId)
	{
		for(ArmorSet as : _armorSets)
			if(as.getChestItemIds().contains(chestItemId))
				return as;
		return null;
	}

	@Override
	public int size()
	{
		return _armorSets.size();
	}

	@Override
	public void clear()
	{
		_armorSets.clear();
	}
}
