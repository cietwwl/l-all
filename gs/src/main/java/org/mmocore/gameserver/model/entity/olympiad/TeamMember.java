package org.mmocore.gameserver.model.entity.olympiad;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.entity.Hero;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.events.impl.DuelEvent;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExAutoSoulShot;
import org.mmocore.gameserver.network.l2.s2c.ExOlympiadMatchEnd;
import org.mmocore.gameserver.network.l2.s2c.ExOlympiadMode;
import org.mmocore.gameserver.network.l2.s2c.Revive;
import org.mmocore.gameserver.network.l2.s2c.SkillCoolTime;
import org.mmocore.gameserver.network.l2.s2c.SkillList;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.SkillEntryType;
import org.mmocore.gameserver.skills.TimeStamp;
import org.mmocore.gameserver.tables.SkillTreeTable;
import org.mmocore.gameserver.templates.InstantZone;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.Log;

public class TeamMember
{
	private static final OlympiadPlayerListeners LISTENERS = new OlympiadPlayerListeners();

	private String _name = StringUtils.EMPTY;
	private String _clanName = StringUtils.EMPTY;
	private int _classId;
	private double _damage;
	private boolean _isDead;

	private final int _objId;
	private final OlympiadGame _game;
	private final CompType _type;
	private final int _side;

	private Player _player;
	private Location _returnLoc = null;

	public boolean isDead()
	{
		return _isDead;
	}

	public void doDie()
	{
		_isDead = true;
	}

	public TeamMember(int obj_id, String name, Player player, OlympiadGame game, int side)
	{
		_objId = obj_id;
		_name = name;
		_game = game;
		_type = game.getType();
		_side = side;

		_player = player;
		if(_player == null)
			return;

		_clanName = player.getClan() == null ? StringUtils.EMPTY : player.getClan().getName();
		_classId = player.getActiveClassId();

		player.setOlympiadSide(side);
		player.setOlympiadGame(game);
	}

	public StatsSet getStat()
	{
		return Olympiad._nobles.get(_objId);
	}

	public void incGameCount()
	{
		StatsSet set = getStat();
		switch(_type)
		{
			case TEAM:
				set.set(Olympiad.GAME_TEAM_COUNT, set.getInteger(Olympiad.GAME_TEAM_COUNT) + 1);
				break;
			case CLASSED:
				set.set(Olympiad.GAME_CLASSES_COUNT, set.getInteger(Olympiad.GAME_CLASSES_COUNT) + 1);
				break;
			case NON_CLASSED:
				set.set(Olympiad.GAME_NOCLASSES_COUNT, set.getInteger(Olympiad.GAME_NOCLASSES_COUNT) + 1);
				break;
		}
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
		{
			StatsSet stat = getStat();
			int points = stat.getInteger(Olympiad.POINTS);
			int diff = Math.min(OlympiadGame.MAX_POINTS_LOOSE, points / _type.getLooseMult());
			stat.set(Olympiad.POINTS, points - diff);
			Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

			// TODO: Снести подробный лог после исправления беспричинного отъёма очков.
			Player player = _player;
			if(player == null)
				Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
			else
			{
				if(player.isLogoutStarted())
					Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
				if(!player.isConnected())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(player.getOlympiadGame() == null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGame() == null", "olympiad");
				if(player.getOlympiadObserveGame() != null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadObserveGame() != null", "olympiad");
			}
		}
	}

	public boolean checkPlayer()
	{
		Player player = _player;
		if(player == null || player.isLogoutStarted() || player.getOlympiadGame() == null || player.isInObserverMode())
			return false;
		return true;
	}

	public void portPlayerToArena()
	{
		Player player = _player;
		if(!checkPlayer() || player.isTeleporting())
		{
			_player = null;
			return;
		}

		DuelEvent duel = player.getEvent(DuelEvent.class);
		if (duel != null)
			duel.abortDuel(player);

		_returnLoc = player._stablePoint == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player._stablePoint;

		if(player.isDead())
			player.setPendingRevive(true);
		if(player.isSitting())
			player.standUp();
		if (player.isRiding() || player.isFlying())
			player.dismount();

		player.setTarget(null);
		player.setIsInOlympiadMode(true);
		player.setUndying(SpecialEffectState.TRUE);
		player.addListener(LISTENERS);

		player.leaveParty();

		Reflection ref = _game.getReflection();
		InstantZone instantZone = ref.getInstancedZone();

		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());

		player._stablePoint = _returnLoc;
		player.teleToLocation(tele, ref);

		if(_type == CompType.TEAM)
			player.setTeam(_side == 1 ? TeamType.BLUE : TeamType.RED);

		player.sendPacket(new ExOlympiadMode(_side));
	}

	public void portPlayerBack()
	{
		Player player = _player;
		if(player == null)
			return;

		player.setOlympiadSide(-1); // эти параметры ставятся в конструкторе и должны очищаться всегда
		player.setOlympiadGame(null);

		if (_returnLoc == null) // игрока не портнуло на стадион
			return;

		player.setIsInOlympiadMode(false);
		player.setOlympiadCompStarted(false);
		player.setUndying(SpecialEffectState.FALSE);
		player.removeListener(LISTENERS);

		if(_type == CompType.TEAM)
			player.setTeam(TeamType.NONE);

		removeBuffs(true);

		for (ItemInstance item : player.getInventory().getItems())
			item.unlockEnchantAndAttribute();

		// Возвращаем клановые скиллы если репутация положительная.
		if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
			player.enableSkillsByEntryType(SkillEntryType.CLAN);

		// Add Hero Skills
		if(player.isHero())
			Hero.addSkills(player);

		SkillTreeTable.unlockMaxEnchant(player, false);

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new Revive(player));
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());

		// Обновляем скилл лист, после добавления скилов
		player.sendPacket(new SkillList(player));
		player.sendPacket(new ExOlympiadMode(0));
		player.sendPacket(new ExOlympiadMatchEnd());

		player._stablePoint = null;
		player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
	}

	public void preparePlayer()
	{
		Player player = _player;
		if(player == null)
			return;

		if(player.isInObserverMode())
			if(player.isInOlympiadObserverMode())
				player.leaveOlympiadObserverMode(true);
			else
				player.leaveObserverMode();

		// Un activate clan skills
		if(player.getClan() != null)
			player.disableSkillsByEntryType(SkillEntryType.CLAN);

		// Remove Hero Skills
		if(player.isHero())
			Hero.removeSkills(player);

		// Удаляем баффы и чужие кубики
		removeBuffs(true);

		// unsummon agathion
		if(player.getAgathionId() > 0)
			player.setAgathion(0);

		if (Config.ALT_OLY_MAX_SKILL_ENCHANT)
			SkillTreeTable.lockMaxEnchant(player, false);

		// Сброс кулдауна всех скилов, время отката которых меньше 15 минут
		for(TimeStamp sts : player.getSkillReuses())
		{
			if(sts == null)
				continue;
			SkillEntry skill = player.getKnownSkill(sts.getId());
			if(skill == null || skill.getLevel() != sts.getLevel())
				continue;
			if(skill.getTemplate().getReuseDelay() <= 15 * 60000L)
				player.enableSkill(skill);
		}

		// Обновляем скилл лист, после удаления скилов
		player.sendPacket(new SkillList(player));
		// Обновляем куллдаун, после сброса
		player.sendPacket(new SkillCoolTime(player));

		for (ItemInstance item : player.getInventory().getItems())
			item.lockEnchantAndAttribute(Config.ALT_OLY_PHYS_WEAPON_ENCHANT_LOCK, Config.ALT_OLY_MAGIC_WEAPON_ENCHANT_LOCK, Config.ALT_OLY_ARMOR_ENCHANT_LOCK, Config.ALT_OLY_ACCESSORY_ENCHANT_LOCK, Config.ALT_OLY_WEAPON_ATTRIBUTE_LOCK, Config.ALT_OLY_ARMOR_ATTRIBUTE_LOCK);

		// Remove Hero weapons
		player.getInventory().refreshEquip();

		// remove bsps/sps/ss automation
		Set<Integer> activeSoulShots = player.getAutoSoulShot();
		for(int itemId : activeSoulShots)
		{
			player.removeAutoSoulShot(itemId);
			player.sendPacket(new ExAutoSoulShot(itemId, false));
		}

		// Разряжаем заряженные соул и спирит шоты
		ItemInstance weapon = player.getActiveWeaponInstance();
		if(weapon != null)
		{
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);
		}

		heal();
	}

	public void startComp()
	{
		Player player = _player;
		if(player == null)
			return;
		_player.setOlympiadCompStarted(true);
	}

	public void stopComp()
	{
		Player player = _player;
		if(player == null)
			return;
		_player.setOlympiadCompStarted(false);
	}

	public void heal()
	{
		Player player = _player;
		if(player == null)
			return;

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
		
	}

	public void removeBuffs(boolean fromSummon)
	{
		Player player = _player;
		if(player == null)
			return;

		player.abortAttack(true, false);
		if(player.isCastingNow())
			player.abortCast(true, true);

		for(Effect e : player.getEffectList().getAllEffects())
		{
			if (e == null)
				continue;
			if (e.getEffectType() == EffectType.Cubic && player.getSkillLevel(e.getSkill().getId()) > 0)
				continue;
			if (e.getSkill().getTemplate().isToggle())
				continue;
			player.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill()));
			e.exit();
		}

		if (player.isFrozen())
			player.stopFrozen();

		Servitor servitor = player.getServitor();
		if(servitor != null)
		{
			servitor.abortAttack(true, false);
			if(servitor.isCastingNow())
				servitor.abortCast(true, true);

			if (fromSummon)
			{
				if(servitor.isPet())
					servitor.unSummon(false, false);
				else
					servitor.getEffectList().stopAllEffects();
			}

			if (servitor.isFrozen())
				servitor.stopFrozen();
		}
	}

	public void saveNobleData()
	{
		OlympiadDatabase.saveNobleData(_objId);
	}

	public void logout()
	{
		_player = null;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getName()
	{
		return _name;
	}

	public void addDamage(double d)
	{
		_damage += d;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getObjectId()
	{
		return _objId;
	}
}