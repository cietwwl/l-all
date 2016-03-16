package org.mmocore.gameserver.handler.voicecommands.impl;

import static org.mmocore.gameserver.model.Zone.ZoneType.no_restart;
import static org.mmocore.gameserver.model.Zone.ZoneType.no_summon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.instancemanager.CoupleManager;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone;
import org.mmocore.gameserver.model.entity.Couple;
import org.mmocore.gameserver.model.entity.events.impl.SingleMatchEvent;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ConfirmDlg;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillUse;
import org.mmocore.gameserver.network.l2.s2c.SetupGauge;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.AbnormalEffect;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wedding implements IVoicedCommandHandler
{
	private static class CoupleAnswerListener implements OnAnswerListener
	{
		private HardReference<Player> _initiatorRef;

		public CoupleAnswerListener(Player initiator)
		{
			_initiatorRef = initiator.getRef();
		}

		@Override
		public void sayYes(Player player)
		{
			Player initiator;
			if((initiator = _initiatorRef.get()) == null)
				return;

			CoupleManager.getInstance().createCouple(initiator, player);
			initiator.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerYes"));
		}

		@Override
		public void sayNo(Player player)
		{
			Player initiator;
			if((initiator = _initiatorRef.get()) == null)
				return;

			initiator.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerNo"));
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Wedding.class);

	private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.ALLOW_WEDDING)
			return false;

		if(command.startsWith("engage"))
			return engage(activeChar);
		else if(command.startsWith("divorce"))
			return divorce(activeChar);
		else if(command.startsWith("gotolove"))
			return goToLove(activeChar);
		return false;
	}

	public boolean divorce(Player activeChar)
	{
		if(activeChar.getPartnerId() == 0)
			return false;

		int _partnerId = activeChar.getPartnerId();
		long adenaAmount = 0;

		if(activeChar.isMaried())
		{
			adenaAmount = Math.abs(activeChar.getAdena() / 100 * Config.WEDDING_DIVORCE_COSTS - 10);
			activeChar.reduceAdena(adenaAmount, true);
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Divorced"));

		}
		else
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Disengaged"));

		activeChar.setMaried(false);
		activeChar.setPartnerId(0);
		Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
		couple.divorce();
		couple = null;

		Player partner = GameObjectsStorage.getPlayer(_partnerId);

		if(partner != null)
		{
			partner.setPartnerId(0);
			if(partner.isMaried())
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDivorce"));
			else
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDisengage"));
			partner.setMaried(false);

			// give adena
			if(adenaAmount > 0)
				partner.addAdena(adenaAmount, true);
		}
		return true;
	}

	public boolean engage(final Player activeChar)
	{
		// check target
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NoneTargeted"));
			return false;
		}
		// check if target is a L2Player
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.OnlyAnotherPlayer"));
			return false;
		}
		// check if player is already engaged
		if(activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.AlreadyEngaged"));
			if(Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				// Head
				// lets recycle the sevensigns debuffs
				int skillId;

				int skillLevel = 1;

				if(activeChar.getLevel() > 40)
					skillLevel = 2;

				if(activeChar.isMageClass())
					skillId = 4361;
				else
					skillId = 4362;

				SkillEntry skill = SkillTable.getInstance().getSkillEntry(skillId, skillLevel);

				if(activeChar.getEffectList().getEffectsBySkill(skill) == null)
				{
					skill.getEffects(activeChar, activeChar, false, false);
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(skillId, skillLevel));
				}
			}
			return false;
		}

		final Player ptarget = (Player) activeChar.getTarget();

		// check if player target himself
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.EngagingYourself"));
			return false;
		}

		if(ptarget.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyMarried"));
			return false;
		}

		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged"));
			return false;
		}

		IntObjectPair<OnAnswerListener> entry = ptarget.getAskListener(false);
		if(entry != null && entry.getValue() instanceof CoupleAnswerListener)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyAsked"));
			return false;
		}

		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged"));
			return false;
		}

		if(ptarget.getSex() == activeChar.getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.SameSex"));
			return false;
		}

		//TODO [G1ta0] Реализовать нормальный список друзей
		boolean FoundOnFriendList = false;
		int objectId;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
			statement.setInt(1, ptarget.getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				objectId = rset.getInt("friend_id");
				if(objectId == activeChar.getObjectId())
				{
					FoundOnFriendList = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		if(!FoundOnFriendList)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NotInFriendlist"));
			return false;
		}

		ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString(new CustomMessage("voicedcommandhandlers.Wedding.AskEngage").toString(ptarget));
		ptarget.ask(packet, new CoupleAnswerListener(activeChar));
		return true;
	}

	public boolean goToLove(Player activeChar)
	{
		if(!Config.WEDDING_TELEPORT_ENABLED)
		{
			activeChar.sendMessage(new CustomMessage("common.Disabled"));
			return false;
		}

		if(!activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.YoureNotMarried"));
			return false;
		}

		if(activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerNotInDB"));
			return false;
		}

		Player partner = GameObjectsStorage.getPlayer(activeChar.getPartnerId());
		if(partner == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerOffline"));
			return false;
		}

		if(partner.isInOlympiadMode() || partner.isFestivalParticipant() || activeChar.isMovementDisabled() || activeChar.isMuted(null) || activeChar.isInOlympiadMode() || activeChar.getEvent(SingleMatchEvent.class) != null || partner.getEvent(SingleMatchEvent.class) != null || activeChar.isFestivalParticipant() || activeChar.getPlayer().isTerritoryFlagEquipped() || partner.isInZone(no_summon))
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}

		if(activeChar.isInParty() && activeChar.getParty().isInDimensionalRift() || partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}

		if(activeChar.getTeleMode() != 0 || activeChar.getReflection() != ReflectionManager.DEFAULT)
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}

		// "Нельзя вызывать персонажей в/из зоны свободного PvP"
		// "в зоны осад"
		// "на Олимпийский стадион"
		// "в зоны определенных рейд-боссов и эпик-боссов"
		// в режиме обсервера или к обсерверу и т.п.
		if(partner.isInZoneBattle() || partner.isInZone(Zone.ZoneType.SIEGE) || partner.isInZone(no_restart) || partner.isInOlympiadMode() || activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(no_restart) || activeChar.isInOlympiadMode() || partner.getReflection() != ReflectionManager.DEFAULT || partner.isInZone(no_summon) || activeChar.isOutOfControl() || partner.isOutOfControl())
		{
			activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		if(!activeChar.reduceAdena(Config.WEDDING_TELEPORT_PRICE, true))
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}


		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL;

		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.sendActionFailed();
		activeChar.stopMove();
		activeChar.startParalyzed();

		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Teleport").addNumber(teleportTimer / 60));
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		// SoE Animation section
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0));
		activeChar.sendPacket(new SetupGauge(activeChar, SetupGauge.BLUE, teleportTimer));
		// End SoE Animation section

		// continue execution later
		ThreadPoolManager.getInstance().schedule(new EscapeFinalizer(activeChar, partner.getLoc()), teleportTimer * 1000L);
		return true;
	}

	static class EscapeFinalizer extends RunnableImpl
	{
		private Player _activeChar;
		private Location _loc;

		EscapeFinalizer(Player activeChar, Location loc)
		{
			_activeChar = activeChar;
			_loc = loc;
		}

		@Override
		public void runImpl() throws Exception
		{
			_activeChar.stopParalyzed();
			if(_activeChar.isDead())
				return;
			_activeChar.teleToLocation(_loc);
		}
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}