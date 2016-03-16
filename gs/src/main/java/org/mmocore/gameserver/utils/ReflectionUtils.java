package org.mmocore.gameserver.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mmocore.gameserver.data.xml.holder.InstantZoneHolder;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.CommandChannel;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Party;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.PlayerGroup;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.events.impl.SingleMatchEvent;
import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.model.quest.QuestState;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.InstantZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 2:14/30.06.2011
 */
public class ReflectionUtils
{
	private static final Logger _log = LoggerFactory.getLogger(ReflectionUtils.class);

	/**
	 * Использовать акуратно возращает дверь нулевого рефлекта
	 * @param id
	 * @return
	 */
	public static DoorInstance getDoor(int id)
	{
		return ReflectionManager.DEFAULT.getDoor(id);
	}

	/**
	 * Использовать акуратно возращает зону нулевого рефлекта
	 * @param name
	 * @return
	 */
	public static Zone getZone(String name)
	{
		return ReflectionManager.DEFAULT.getZone(name);
	}

	public static List<Zone> getZonesByType(Zone.ZoneType zoneType)
	{
		Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
		if(zones.isEmpty())
			return Collections.emptyList();

		List<Zone> zones2 = new ArrayList<Zone>(5);
		for(Zone z : zones)
			if(z.getType() == zoneType)
				zones2.add(z);

		return zones2;
	}

	/**
	 * Вход или повторный вход в инстансзону со всеми проверками. Возвращает рефлект только если это первый вход.
	 */
	public static Reflection simpleEnterInstancedZone(Player player, int instancedZoneId)
	{
		final Reflection ar = player.getActiveReflection();
		if (ar == null)
		{
			if (!canEnterInstance(player, instancedZoneId))
				return null;

			return enterReflection(player, new Reflection(), InstantZoneHolder.getInstance().getInstantZone(instancedZoneId));
		}

		if (!canReenterInstance(player, instancedZoneId))
			return null;

		player.teleToLocation(ar.getTeleportLoc(), ar);
		return null;
	}

	/**
	 * Вход или повторный вход в инстансзону со всеми проверками. Возвращает рефлект только если это первый вход.
	 */
	public static Reflection simpleEnterInstancedZone(Player player, Class<? extends Reflection> refClass, int instancedZoneId)
	{
		final Reflection ar = player.getActiveReflection();
		if (ar == null)
		{
			if (!canEnterInstance(player, instancedZoneId))
				return null;

			try
			{
				return enterReflection(player, refClass.newInstance(), InstantZoneHolder.getInstance().getInstantZone(instancedZoneId));
			}
			catch (Exception e)
			{
				_log.error("Unable to create instanced zone: ", e);
			}
			return null;
		}

		if (!canReenterInstance(player, instancedZoneId))
			return null;

		player.teleToLocation(ar.getTeleportLoc(), ar);
		return null;
	}

	public static boolean canEnterInstance(Player player, int instancedZoneId)
	{
		if(player.isDead())
			return false;

		if (player.getEvent(SingleMatchEvent.class) != null)
			return false;

		final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz == null)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}

		final IBroadcastPacket result = iz.canCreate();
		if (result != null)
		{
			player.sendPacket(result);
			return false;
		}

		final PlayerGroup pg = player.getPlayerGroup();
		if (pg.getGroupLeader() != player)
		{
			player.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
			return false;
		}

		final int count = pg.getMemberCount();
		if(iz.getMinParty() > 0 && count < iz.getMinParty())
		{
			if (!player.isGM())
			{
				if (player.getParty() != null)
				{
					if (iz.getMinParty() > 9 && player.getParty().getCommandChannel() == null)
						player.sendPacket(new SystemMessage(SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL));
					else
						player.sendPacket(new SystemMessage(SystemMsg.YOU_MUST_HAVE_A_MINIMUM_OF_S1_PEOPLE_TO_ENTER_THIS_INSTANT_ZONE).addNumber(iz.getMinParty()));
				}
				else if (iz.getMinParty() > 9)
					player.sendPacket(new SystemMessage(SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL));
				else
					player.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);

				return false;				
			}
			Log.LogCommand(player, null, "entered instance " + iz.getName(), true);
		}

		if(iz.getMaxParty() > 0 && count > iz.getMaxParty())
		{
			if (iz.getMaxParty() > 1)
				player.sendPacket(SystemMsg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
			else
				player.sendPacket(SystemMsg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);

			return false;
		}

		for (Player member : pg)
		{
			if(member != player && !player.isInRange(member, 500))
			{
				pg.broadCast(new SystemMessage(SystemMsg.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED).addName(member));
				return false;
			}

			SystemMsg msg = checkPlayer(member, iz);
			if(msg != null)
			{
				if(msg.size() > 0)
					pg.broadCast(new SystemMessage(msg).addName(member));
				else
					member.sendPacket(msg);

				return false;
			}
		}

		return true;
	}

	public static boolean canReenterInstance(Player player, int instancedZoneId)
	{
		if (player.getEvent(SingleMatchEvent.class) != null)
			return false;

		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		SystemMsg msg = reCheckPlayer(player, iz);
		if (msg != null)
		{
			if(msg.size() > 0)
				player.sendPacket(new SystemMessage(msg).addName(player));
			else
				player.sendPacket(msg);

			return false;
		}

		if(iz.isDispelBuffs())
			dispelBuffs(player);

		return true;
	}

	private static SystemMsg checkPlayer(Player player, InstantZone instancedZone)
	{
		if(player.getActiveReflection() != null)
			return SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON;

		if (instancedZone.getMinLevel() > 0 && player.getLevel() < instancedZone.getMinLevel())
			return SystemMsg.C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY;
		if (instancedZone.getMaxLevel() > 0 && player.getLevel() > instancedZone.getMaxLevel())
			return SystemMsg.C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY;

		if(player.isCursedWeaponEquipped() || player.isInFlyingTransform() || player.isTerritoryFlagEquipped())
			return SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;

		if(InstantZoneHolder.getInstance().getMinutesToNextEntrance(instancedZone.getId(), player) > 0)
			return SystemMsg.C1_MAY_NOT_REENTER_YET;

		if(instancedZone.getRemovedItemId() > 0 && instancedZone.getRemovedItemNecessity() && ItemFunctions.getItemCount(player, instancedZone.getRemovedItemId()) < 1)
			return SystemMsg.C1S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED;

		if(instancedZone.getRequiredQuestId() > 0)
		{
			QuestState qs = player.getQuestState(instancedZone.getRequiredQuestId());
			if(qs == null || qs.getState() != Quest.STARTED)
				return SystemMsg.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED;
		}

		return null;
	}

	private static SystemMsg reCheckPlayer(Player player, InstantZone instancedZone)
	{
		Reflection ar = player.getActiveReflection();
		if(ar != null)
		{
			InstantZone iz = player.getActiveReflection().getInstancedZone();
			if (iz != instancedZone)
				return SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON;
			if (ar.getPlayerCount() >= iz.getMaxParty())
				return SystemMsg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT;
		}

		if (instancedZone.getMinLevel() > 0 && player.getLevel() < instancedZone.getMinLevel())
			return SystemMsg.C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY;
		if (instancedZone.getMaxLevel() > 0 && player.getLevel() > instancedZone.getMaxLevel())
			return SystemMsg.C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY;

		if(player.isCursedWeaponEquipped() || player.isInFlyingTransform() || player.isTerritoryFlagEquipped())
			return SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;

		if(instancedZone.getRequiredQuestId() > 0)
		{
			QuestState qs = player.getQuestState(instancedZone.getRequiredQuestId());
			if(qs == null || qs.getState() != Quest.STARTED)
				return SystemMsg.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED;
		}

		return null;
	}

	private static void dispelBuffs(Player player)
	{
		for(Effect e : player.getEffectList().getAllEffects())
			if(!e.getSkill().getTemplate().isOffensive() && !e.getSkill().getTemplate().isNewbie() && e.isCancelable() && !e.getSkill().getTemplate().isPreservedOnDeath())
			{
				player.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		final Servitor servitor = player.getServitor();
		if(servitor != null)
			for(Effect e : servitor.getEffectList().getAllEffects())
				if(!e.getSkill().getTemplate().isOffensive() && !e.getSkill().getTemplate().isNewbie() && e.isCancelable() && !e.getSkill().getTemplate().isPreservedOnDeath())
					e.exit();
	}

	private static Reflection enterReflection(Player invoker, Reflection r, InstantZone iz)
	{
		r.init(iz);

		if(r.getReturnLoc() == null)
			r.setReturnLoc(invoker.getLoc());

		final Party party = invoker.getParty(); // TODO: DS: to PlayerGroup
		if (party != null)
		{
			final CommandChannel cc = party.getCommandChannel();
			if (cc != null)
			{
				cc.setReflection(r);
				r.setCommandChannel(cc);
			}
			else
			{
				party.setReflection(r);
				r.setParty(party);
			}
		}

		final PlayerGroup pg = invoker.getPlayerGroup();
		for (Player member : pg)
		{
			if(iz.getRemovedItemId() > 0)
				ItemFunctions.deleteItem(member, iz.getRemovedItemId(), iz.getRemovedItemCount());
			if(iz.getGiveItemId() > 0)
				ItemFunctions.addItem(member, iz.getGiveItemId(), iz.getGiveItemCount());
			if(iz.isDispelBuffs())
				dispelBuffs(member);
			if(iz.getSetReuseUponEntry())
				member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
			member.setVar("backCoords", invoker.getLoc().toXYZString(), -1);
			member.teleToLocation(iz.getTeleportCoord(), r);
		}

		return r;
	}
}
