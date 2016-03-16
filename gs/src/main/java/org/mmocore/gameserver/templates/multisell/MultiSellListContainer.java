package org.mmocore.gameserver.templates.multisell;

import java.util.ArrayList;
import java.util.List;


public class MultiSellListContainer
{
	private int _listId;
	private boolean _showall = true;
	private boolean _keep_enchanted = false;
	private boolean _is_dutyfree = false;
	private boolean _nokey = false;
	private boolean _allowBBS = true;
	private boolean _disabled = false;
	private List<MultiSellEntry> _entries = new ArrayList<MultiSellEntry>();
	private int _npcObjectId = -1;

	public void setListId(int listId)
	{
		_listId = listId;
	}

	public int getListId()
	{
		return _listId;
	}

	public void setShowAll(boolean bool)
	{
		_showall = bool;
	}

	public boolean isShowAll()
	{
		return _showall;
	}

	public void setNoTax(boolean bool)
	{
		_is_dutyfree = bool;
	}

	public boolean isNoTax()
	{
		return _is_dutyfree;
	}

	public void setNoKey(boolean bool)
	{
		_nokey = bool;
	}

	public boolean isNoKey()
	{
		return _nokey;
	}

	public void setKeepEnchant(boolean bool)
	{
		_keep_enchanted = bool;
	}

	public boolean isKeepEnchant()
	{
		return _keep_enchanted;
	}

	public boolean isBBSAllowed()
	{
		return _allowBBS;
	}

	public void setBBSAllowed(boolean bool)
	{
		_allowBBS = bool;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean b)
	{
		_disabled = b;
	}

	public void addEntry(MultiSellEntry e)
	{
		_entries.add(e);
	}

	public List<MultiSellEntry> getEntries()
	{
		return _entries;
	}

	public boolean isEmpty()
	{
		return _entries.isEmpty();
	}

	public int getNpcObjectId()
	{
		return _npcObjectId;
	}

	public void setNpcObjectId(int id)
	{
		_npcObjectId = id;
	}
}
