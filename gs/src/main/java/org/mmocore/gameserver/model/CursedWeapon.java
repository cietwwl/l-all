package org.mmocore.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Skill.AddedSkill;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.Earthquake;
import org.mmocore.gameserver.network.l2.s2c.ExRedSky;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.SkillList;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Location;


public class CursedWeapon
{
	private final String _name;
	private String _transformationName;

	private final int _itemId, _skillMaxLevel;
	private final int _skillId;
	private int _dropRate, _disapearChance;
	private int _durationMin, _durationMax, _durationLost;
	private int _transformationId, _transformationTemplateId;
	private int _stageKills, _nbKills = 0, _playerKarma = 0, _playerPkKills = 0;

	private CursedWeaponState _state = CursedWeaponState.NONE;
	private Location _loc = null;
	private long _endTime = 0;
	private ItemInstance _item = null;

	private int _playerObjectId = 0;
	private Player _player = null;

	public enum CursedWeaponState
	{
		NONE,
		ACTIVATED,
		DROPPED,
	}

	public CursedWeapon(int itemId, int skillId, String name)
	{
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
	}

	public void initWeapon()
	{
		zeroOwner();
		setState(CursedWeaponState.NONE);
		_endTime = 0;
		_item = null;
		_nbKills = 0;
	}

	/** Выпадение оружия из монстра */
	public void create(NpcInstance attackable, Player killer)
	{
		_item = ItemFunctions.createItem(_itemId);
		if(_item != null)
		{
			zeroOwner();
			setState(CursedWeaponState.DROPPED);

			if(_endTime == 0)
				_endTime = System.currentTimeMillis() + getRndDuration() * 60000;

			_item.dropToTheGround(attackable, Location.findPointToStay(attackable, 100));
			_loc = _item.getLoc();
			_item.setDropTime(0);

			// RedSky and Earthquake
			L2GameServerPacket redSky = new ExRedSky(10);
			L2GameServerPacket eq = new Earthquake(killer.getLoc(), 30, 12);
			for(Player player : GameObjectsStorage.getPlayers())
				player.sendPacket(redSky, eq);
		}
	}

	/**
	 * Выпадение оружия из владельца, или исчезновение с определенной вероятностью.
	 * Вызывается при смерти игрока.
	 */
	public boolean dropIt(NpcInstance attackable, Player killer, Player owner)
	{
		if(Rnd.chance(_disapearChance))
			return false;

		Player player = getOnlineOwner();
		if(player == null)
		{
			if(owner == null)
				return false;
			player = owner;
		}

		ItemInstance oldItem;
		if((oldItem = player.getInventory().removeItemByItemId(_itemId, 1L)) == null)
			return false;

		player.setKarma(_playerKarma, true);
		player.setPkKills(_playerPkKills);
		player.setCursedWeaponEquippedId(0);
		player.setTransformation(0);
		player.setTransformationName(null);
		player.validateLocation(0);

		SkillEntry skill = SkillTable.getInstance().getSkillEntry(_skillId, player.getSkillLevel(_skillId));
		if(skill != null)
			for(AddedSkill s : skill.getTemplate().getAddedSkills())
				player.removeSkillById(s.id);

		player.removeSkillById(_skillId);

		player.abortAttack(true, false);

		zeroOwner();
		setState(CursedWeaponState.DROPPED);

		oldItem.dropToTheGround(player, Location.findPointToStay(player, 100));
		_loc = oldItem.getLoc();

		oldItem.setDropTime(0);
		_item = oldItem;

		player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_DROPPED_S1).addItemName(oldItem.getItemId()));
		player.broadcastUserInfo(true);
		player.broadcastPacket(new Earthquake(player.getLoc(), 30, 12));

		return true;
	}

	private void giveSkill(Player player)
	{
		for(SkillEntry s : getSkills(player))
		{
			player.addSkill(s, false);
			player.getTransformationSkills().put(s.getId(), s);
		}
		player.sendPacket(new SkillList(player));
	}

	private Collection<SkillEntry> getSkills(Player player)
	{
		int level = 1 + _nbKills / _stageKills;
		if(level > _skillMaxLevel)
			level = _skillMaxLevel;

		SkillEntry skill = SkillTable.getInstance().getSkillEntry(_skillId, level);
		List<SkillEntry> ret = new ArrayList<SkillEntry>();
		ret.add(skill);
		for(AddedSkill s : skill.getTemplate().getAddedSkills())
			ret.add(SkillTable.getInstance().getSkillEntry(s.id, s.level));
		return ret;
	}

	/** вызывается при загрузке оружия */
	public boolean reActivate()
	{
		if(getTimeLeft() <= 0)
		{
			if(getPlayerId() != 0) // to be sure, that cursed weapon will deleted in right way
				setState(CursedWeaponState.ACTIVATED);
			return false;
		}

		if(getPlayerId() == 0)
		{
			if(_loc == null || (_item = ItemFunctions.createItem(_itemId)) == null)
				return false;

			_item.dropMe(null, _loc);
			_item.setDropTime(0);

			setState(CursedWeaponState.DROPPED);
		}
		else
			setState(CursedWeaponState.ACTIVATED);
		return true;
	}

	public void activate(Player player, ItemInstance item)
	{
		if(isDropped() || getPlayerId() != player.getObjectId()) // оружие уже в руках игрока или новый игрок
		{
			setPlayerId(player.getObjectId());
			setPlayerKarma(player.getKarma());
			setPlayerPkKills(player.getPkKills());
		}

		setPlayer(player);
		setState(CursedWeaponState.ACTIVATED);

		player.leaveParty();
		if(player.isMounted())
			player.dismount();

		_item = item;

		player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
		player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_RHAND, null);
		player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_RHAND, _item);

		player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(_item.getItemId()));

		player.setTransformation(0);
		player.setCursedWeaponEquippedId(_itemId);
		player.setTransformation(_transformationId);
		player.setTransformationName(_transformationName);
		player.setTransformationTemplate(_transformationTemplateId);
		player.setKarma(9999999, true);
		player.setPkKills(_nbKills);

		if(_endTime == 0)
			_endTime = System.currentTimeMillis() + getRndDuration() * 60000;

		giveSkill(player);

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void increaseKills()
	{
		Player player = getOnlineOwner();
		if(player == null)
			return;

		_nbKills++;
		player.setPkKills(_nbKills);
		player.updateStats();
		if(_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
			giveSkill(player);
		_endTime -= _durationLost * 60000; // Reduce time-to-live
	}

	public void setDisapearChance(int disapearChance)
	{
		_disapearChance = disapearChance;
	}

	public void setDropRate(int dropRate)
	{
		_dropRate = dropRate;
	}

	public void setDurationMin(int duration)
	{
		_durationMin = duration;
	}

	public void setDurationMax(int duration)
	{
		_durationMax = duration;
	}

	public void setDurationLost(int durationLost)
	{
		_durationLost = durationLost;
	}

	public void setStageKills(int stageKills)
	{
		_stageKills = stageKills;
	}

	public void setTransformationId(int transformationId)
	{
		_transformationId = transformationId;
	}

	public int getTransformationId()
	{
		return _transformationId;
	}

	public void setTransformationTemplateId(int transformationTemplateId)
	{
		_transformationTemplateId = transformationTemplateId;
	}

	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	public void setNbKills(int nbKills)
	{
		_nbKills = nbKills;
	}

	public void setPlayerId(int playerId)
	{
		_playerObjectId = playerId;
	}

	public void setPlayerKarma(int playerKarma)
	{
		_playerKarma = playerKarma;
	}

	public void setPlayerPkKills(int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}

	public void setState(CursedWeaponState state)
	{
		_state = state;
	}

	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}

	public void setPlayer(Player player)
	{
		_player = player;
	}

	private void zeroOwner()
	{
		_player = null;
		_playerObjectId = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
	}

	public void setItem(ItemInstance item)
	{
		_item = item;
	}

	public void setLoc(Location loc)
	{
		_loc = loc;
	}

	public CursedWeaponState getState()
	{
		return _state;
	}

	public boolean isActivated()
	{
		return getState() == CursedWeaponState.ACTIVATED;
	}

	public boolean isDropped()
	{
		return getState() == CursedWeaponState.DROPPED;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public String getName()
	{
		return _name;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public ItemInstance getItem()
	{
		return _item;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public int getDropRate()
	{
		return _dropRate;
	}

	public int getPlayerId()
	{
		return _playerObjectId;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public int getPlayerKarma()
	{
		return _playerKarma;
	}

	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}

	public int getNbKills()
	{
		return _nbKills;
	}

	public int getStageKills()
	{
		return _stageKills;
	}

	/**
	 * Возвращает позицию (x, y, z)
	 * @return Location
	 */
	public Location getLoc()
	{
		return _loc;
	}

	public int getRndDuration()
	{
		if(_durationMin > _durationMax)
			_durationMax = 2 * _durationMin;
		return Rnd.get(_durationMin, _durationMax);
	}

	public boolean isActive()
	{
		return isActivated() || isDropped();
	}

	public int getLevel()
	{
		return Math.min(1 + _nbKills / _stageKills, _skillMaxLevel);
	}

	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}

	public Location getWorldPosition()
	{
		if(isActivated())
		{
			Player player = getOnlineOwner();
			if(player != null)
				return player.getLoc();
		}
		else if(isDropped())
			if(_item != null)
				return _item.getLoc();

		return null;
	}

	public Player getOnlineOwner()
	{
		Player player = getPlayer();
		return player != null && player.isOnline() ? player : null;
	}
}