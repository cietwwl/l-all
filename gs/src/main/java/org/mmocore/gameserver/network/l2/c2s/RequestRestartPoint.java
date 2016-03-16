package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.entity.residence.ClanHall;
import org.mmocore.gameserver.model.entity.residence.Fortress;
import org.mmocore.gameserver.model.entity.residence.ResidenceFunction;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.Die;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.TeleportUtils;
import org.napile.pair.primitive.IntObjectPair;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(_restartType == null || activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		switch(_restartType)
		{
			case AGATHION:
				if(activeChar.isAgathionResAvailable())
					activeChar.doRevive(100);
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			case FIXED:
				if(activeChar.getPlayerAccess().ResurectFixed)
					activeChar.doRevive(100);
				else if (!(Config.ALT_DISABLE_FEATHER_ON_SIEGES_AND_EPIC && (activeChar.isOnSiegeField() || activeChar.isInZone(ZoneType.epic))))
				{
					if(!activeChar.getInventory().isLockedItem(13300) && ItemFunctions.deleteItem(activeChar, 13300, 1))
					{
						activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
						activeChar.doRevive(100);
					}
					else if(!activeChar.getInventory().isLockedItem(10649) && ItemFunctions.deleteItem(activeChar, 10649, 1))
					{
						activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
						activeChar.doRevive(100);
					}
					else
						activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			default:
				Location loc = defaultLoc(_restartType, activeChar);
				if(loc != null)
				{
					IntObjectPair<OnAnswerListener> ask = activeChar.getAskListener(false);
					if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
						activeChar.getAskListener(true);

					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
		}
	}

	//FIXME [VISTALL] вынести куда то?
	// телепорт к флагу, не обрабатывается, по дефалту
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();

		switch(restartType)
		{
			case TO_CLANHALL:
				if(clan != null && clan.getHasHideout() != 0)
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
					if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_CASTLE:
				if(clan != null && clan.getCastle() != 0)
				{
					Castle castle = activeChar.getCastle();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FORTRESS:
				if(clan != null && clan.getHasFortress() != 0)
				{
					Fortress fort = activeChar.getFortress();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
					if(fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_VILLAGE:
			default:
				loc = TeleportUtils.getRestartLocation(activeChar, restartType);
				break;
		}
		return loc;
	}
}
