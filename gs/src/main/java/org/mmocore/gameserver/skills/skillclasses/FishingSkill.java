package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.FishTable;
import org.mmocore.gameserver.templates.FishTemplate;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.PositionUtils;


public class FishingSkill extends Skill
{
	public FishingSkill(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = (Player) activeChar;

		if(player.getSkillLevel(SKILL_FISHING_MASTERY) == -1)
			return false;

		if(player.isFishing())
		{
			player.stopFishing();
			player.sendPacket(SystemMsg.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
			return false;
		}

		if(player.isInBoat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT);
			return false;
		}

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
			return false;
		}

		Zone fishingZone = player.getZone(ZoneType.FISHING);
		if(fishingZone == null)
		{
			player.sendPacket(SystemMsg.YOU_CANT_FISH_HERE);
			return false;
		}

		if(player.isInWater())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_UNDER_WATER);
			return false;
		}

		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if(weaponItem == null || weaponItem.getItemType() != WeaponType.ROD)
		{
			//Fishing poles are not installed
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED);
			return false;
		}

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(SystemMsg.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			return false;
		}

		//Вычисляем координаты поплавка
		int rnd = Rnd.get(50) + 150;
		double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle - 90);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = -(int) (sin * rnd);
		int y1 = (int) (cos * rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		//z - уровень карты
		int z = GeoEngine.getHeight(x, y, player.getZ(), player.getGeoIndex()) + 1;

		// Проверяем, что поплавок оказался в воде
		// в зоне типа 2 можно рыбачить без воды, но если вода есть то ставим поплавок на ее поверхность
		boolean isInWater = fishingZone.getParams().getInteger("fishing_place_type") == 2;
		LazyArrayList<Zone> zones = LazyArrayList.newInstance();
		World.getZones(zones, new Location(x, y, z), player.getReflection());
		for(Zone zone : zones)
			if (zone.getType() == ZoneType.water)
			{
				//z - уровень воды
				z = zone.getTerritory().getZmax();
				isInWater = true;
				break;
			}
		LazyArrayList.recycle(zones);

		if(!isInWater)
		{
			player.sendPacket(SystemMsg.YOU_CANT_FISH_HERE);
			return false;
		}

		player.getFishing().setFishLoc(new Location(x, y, z));

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature caster, List<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;

		Player player = (Player) caster;

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(SystemMsg.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			return;
		}
		Zone zone = player.getZone(ZoneType.FISHING);
		if(zone == null)
			return;

		int distributionId = zone.getParams().getInteger("distribution_id");

		int lureId = lure.getItemId();

		int fishLvl = org.mmocore.gameserver.model.Fishing.getRandomFishLvl(player);
		int group = org.mmocore.gameserver.model.Fishing.getFishGroup(lureId);
		int type = org.mmocore.gameserver.model.Fishing.getRandomFishType(lureId, fishLvl, distributionId);

		List<FishTemplate> fishs = FishTable.getInstance().getFish(group, type, fishLvl);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}

		if(!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_BAIT);
			return;
		}

		int check = Rnd.get(fishs.size());
		FishTemplate fish = fishs.get(check);

		player.startFishing(fish, lureId);
	}
}