package org.mmocore.gameserver.network.l2.c2s;

import java.util.Calendar;

import org.mmocore.gameserver.Announcements;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.MailDAO;
import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.instancemanager.CoupleManager;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.instancemanager.PetitionManager;
import org.mmocore.gameserver.instancemanager.PlayerMessageStack;
import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.model.entity.SevenSigns;
import org.mmocore.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import org.mmocore.gameserver.model.entity.events.impl.DominionSiegeEvent;
import org.mmocore.gameserver.model.entity.residence.ClanHall;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.mail.Mail;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.*;
import org.mmocore.gameserver.network.l2.s2c.ConfirmDlg;
import org.mmocore.gameserver.skills.AbnormalEffect;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.ClanTable;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.GameStats;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.TradeHelper;
import org.mmocore.gameserver.utils.Util;
import org.napile.pair.primitive.IntObjectPair;

public class EnterWorld extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//readS(); - клиент всегда отправляет строку "narcasse"
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}

		GameStats.incrementPlayerEnterGame();

		boolean first = activeChar.entering;

		if(first)
		{
			activeChar.setUptime(System.currentTimeMillis());
			activeChar.setOnlineStatus(true);

			if(Config.SAVE_GM_EFFECTS)
			{
				if(activeChar.getPlayerAccess().GodMode)
				{
					//hide
					if(activeChar.getVarB("gm_hide"))
					{
						activeChar.setInvisible(SpecialEffectState.GM);
					}
				}
			}

			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.spawnMe();

			if(activeChar.isInStoreMode())
				if(!TradeHelper.validateStore(activeChar))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}

			activeChar.setRunning();
			activeChar.standUp();
			activeChar.startTimers();
		}

		if (client.getState() == GameClient.GameClientState.ENTER_GAME)
			client.setState(GameClient.GameClientState.IN_GAME);

		activeChar.sendPacket(new ExBR_PremiumState(activeChar, activeChar.hasBonus()));

		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar), new ExGetBookMarkInfo(activeChar));
		activeChar.sendItemList(false);
		activeChar.sendPacket(new ShortCutInit(activeChar), new SkillList(activeChar), new SkillCoolTime(activeChar));
		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

		Announcements.getInstance().showAnnouncements(activeChar);

		if(first)
			activeChar.getListeners().onEnter();

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

		if(first && activeChar.getCreateTime() > 0)
		{
			Calendar create = Calendar.getInstance();
			create.setTimeInMillis(activeChar.getCreateTime());
			Calendar now = Calendar.getInstance();

			int day = create.get(Calendar.DAY_OF_MONTH);
			if(create.get(Calendar.MONTH) == Calendar.FEBRUARY && day == 29)
				day = 28;

			int myBirthdayReceiveYear = activeChar.getVarInt(Player.MY_BIRTHDAY_RECEIVE_YEAR, 0);
			if(create.get(Calendar.MONTH) == now.get(Calendar.MONTH) && create.get(Calendar.DAY_OF_MONTH) == day)
			{
				if((myBirthdayReceiveYear == 0 && create.get(Calendar.YEAR) != now.get(Calendar.YEAR)) || myBirthdayReceiveYear > 0 && myBirthdayReceiveYear != now.get(Calendar.YEAR))
				{
					Mail mail = new Mail();
					mail.setSenderId(1);
					mail.setSenderName(StringHolder.getInstance().getString(activeChar, "birthday.npc"));
					mail.setReceiverId(activeChar.getObjectId());
					mail.setReceiverName(activeChar.getName());
					mail.setTopic(StringHolder.getInstance().getString(activeChar, "birthday.title"));
					mail.setBody(StringHolder.getInstance().getString(activeChar, "birthday.text"));

					ItemInstance item = ItemFunctions.createItem(21169);
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(1L);
					item.save();

					mail.addAttachment(item);
					mail.setUnread(true);
					mail.setType(Mail.SenderType.BIRTHDAY);
					mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					mail.save();

					activeChar.setVar(Player.MY_BIRTHDAY_RECEIVE_YEAR, String.valueOf(now.get(Calendar.YEAR)), -1);
				}
			}
		}

		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);

			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}

		// engage and notify Partner
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			activeChar.processQuestEvent(255, "UC", null);
			activeChar.restoreDisableSkills();

			activeChar.updatePremiumItems();
		}

		activeChar.sendPacket(new L2FriendList(activeChar), new ExStorageMaxCount(activeChar), new QuestList(activeChar), new ExBasicActionList(activeChar), new EtcStatusUpdate(activeChar));
		if(activeChar.getEvent(DominionSiegeEvent.class) != null)
			activeChar.sendPacket(ExDominionChannelSet.ACTIVE);

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();

		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			if(activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				SkillEntry castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && activeChar.getAnimationEndTime() > 0)
					sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}

			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));

			if(activeChar.isMoving || activeChar.isFollow)
				sendPacket(activeChar.movePacket());

			if(activeChar.getMountNpcId() != 0)
				sendPacket(new Ride(activeChar));

			if(activeChar.isFishing())
				activeChar.stopFishing();
		}

		activeChar.entering = false;
		activeChar.sendUserInfo(true);

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitType(activeChar, ChangeWaitType.WT_SITTING));
		if(activeChar.isInStoreMode())
			sendPacket(activeChar.getPrivateStoreMsgPacket(activeChar));

		if(activeChar.isDead())
			sendPacket(new Die(activeChar));

		activeChar.unsetVar("offline");

		// на всякий случай
		activeChar.sendActionFailed();

		if(first)
		{
			if(Config.SAVE_GM_EFFECTS)
			{
				if(activeChar.getPlayerAccess().GodMode)
				{
					//silence
					if(activeChar.getVarB("gm_silence"))
					{
						activeChar.setMessageRefusal(true);
						activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
					}
					//invul
					if(activeChar.getVarB("gm_invul"))
					{
						activeChar.setInvul(SpecialEffectState.GM);
						activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
						activeChar.sendMessage("You are immortal now.");
					}
					//undying
					if(activeChar.getVarB("gm_undying"))
					{
						activeChar.setUndying(SpecialEffectState.GM);
						activeChar.sendMessage("Undying state has been enabled.");
					}
					//gmspeed
					int gmspeed = activeChar.getVarInt("gm_gmspeed", 0);
					if(gmspeed >= 1 && gmspeed <= 4)
						activeChar.doCast(SkillTable.getInstance().getSkillEntry(7029, gmspeed), activeChar, true);
				}
			}
		}

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		sendPacket(new ClientSetTime(activeChar), new ExSetCompassZoneCode(activeChar));

		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)//FIXME [G1ta0] бардак
			sendPacket(new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));

		if(activeChar.isCursedWeaponEquipped())
			CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());

		if(!first)
		{
			//Персонаж вылетел во время просмотра
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == Player.OBSERVER_STARTING)
				{
					if(activeChar.isInOlympiadObserverMode())
						sendPacket(new TeleportToLocation(activeChar, activeChar.getObservePoint().getLoc()));
					else
						sendPacket(new ObserverStart(activeChar.getObservePoint().getLoc()));
				}
				else if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
					activeChar.returnFromObserverMode();
				else if(activeChar.isInOlympiadObserverMode())
					activeChar.leaveOlympiadObserverMode(true);
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar, false);

			if(activeChar.getServitor() != null)
				sendPacket(new PetInfo(activeChar.getServitor()));

			if(activeChar.isInParty())
			{
				Servitor memberPet;
				//sends new member party window for all members
				//we do all actions before adding member to a list, this speeds things up a little
				sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));

				for(Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
						sendPacket(new PartySpelled(member, true));
						if((memberPet = member.getServitor()) != null)
							sendPacket(new PartySpelled(memberPet, true));

						sendPacket(new RelationChanged().add(member, activeChar));
						if(memberPet != null)
							memberPet.broadcastCharInfoImpl(activeChar);
					}

				// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
				if(activeChar.getParty().isInCommandChannel())
					sendPacket(ExMPCCOpen.STATIC);
			}

			for(int shotId : activeChar.getAutoSoulShot())
				sendPacket(new ExAutoSoulShot(shotId, true));

			for(Effect e : activeChar.getEffectList().getAllFirstEffects())
				if(e.getSkill().getTemplate().isToggle())
					sendPacket(new MagicSkillLaunched(activeChar, e.getSkill().getId(), e.getSkill().getLevel(), activeChar));

			activeChar.broadcastCharInfo();
		}
		else
			activeChar.sendUserInfo(); // Отобразит права в клане

		activeChar.updateEffectIcons();
		activeChar.updateStats();

		if(Config.ALT_PCBANG_POINTS_ENABLED)
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));

		if(!activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(Config.EX_GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);

		activeChar.sendVoteSystemInfo();
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
		activeChar.getNevitSystem().onEnterWorld();

		checkNewMail(activeChar);

		if(Config.EX_CHANGE_NAME_DIALOG)
		{
			Clan clan = activeChar.getClan();
			if(clan == null || clan.getLeaderId(Clan.SUBUNIT_MAIN_CLAN) != activeChar.getObjectId())
				return;

			String name = clan.getUnitName(Clan.SUBUNIT_MAIN_CLAN);
			if (!Util.isMatchingRegexp(name, Config.CLAN_NAME_TEMPLATE))
				activeChar.sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_CLAN_NAME, ExNeedToChangeName.REASON_INVALID, name));
			else if(ClanTable.getInstance().getClansSizeByName(name) > 1)
				activeChar.sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_CLAN_NAME, ExNeedToChangeName.REASON_EXISTS, name));
		}
	}

	private static void notifyClanMembers(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		SubUnit subUnit = activeChar.getSubUnit();
		if(clan == null || subUnit == null)
			return;

		UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
		if(member == null)
			return;

		member.setPlayerInstance(activeChar, false);

		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		L2GameServerPacket msg = new SystemMessage(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
		PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for(Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
				clanMember.sendPacket(new SystemMessage(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
			else if(clanMember.getObjectId() == apprentice)
				clanMember.sendPacket(new SystemMessage(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
			else
				clanMember.sendPacket(msg);
		}

		if(!activeChar.isClanLeader())
			return;

		ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
			return;

		if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
			return;

		if(clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < clanHall.getRentalFee())
			activeChar.sendPacket(new SystemMessage(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(clanHall.getRentalFee()));
	}

	private void checkNewMail(Player activeChar)
	{
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
			if(mail.isUnread())
			{
				sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
	}
}