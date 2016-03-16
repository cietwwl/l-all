package org.mmocore.gameserver.model.instances;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.SubClass;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.base.ClassType;
import org.mmocore.gameserver.model.base.PlayerClass;
import org.mmocore.gameserver.model.base.Race;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.entity.olympiad.Olympiad;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.model.pledge.UnitMember;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.CertificationFunctions;
import org.mmocore.gameserver.utils.HtmlUtils;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.SiegeUtils;


public final class VillageMasterInstance extends NpcInstance
{
	public VillageMasterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("create_clan_check"))
		{
			if(player.getLevel() <= 9)
				showChatWindow(player, "pledge/pl002.htm");
			else if(player.isClanLeader())
				showChatWindow(player, "pledge/pl003.htm");
			else if(player.getClan() != null)
				showChatWindow(player, "pledge/pl004.htm");
			else
				showChatWindow(player, "pledge/pl005.htm");
		}
		else if(command.equals("disband_clan_check"))
		{
			if(VillageMasterPledgeBypasses.checkPlayerForClanLeader(this, player))
				showChatWindow(player, "pledge/pl007.htm");
		}
		else if(command.equals("restore_clan_check"))
		{
			if(VillageMasterPledgeBypasses.checkPlayerForClanLeader(this, player))
				showChatWindow(player, "pledge/pl010.htm");
		}
		else if(command.startsWith("subpledge_upgrade"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command);
			tokenizer.nextElement();

			int val = Integer.parseInt(tokenizer.nextToken());

			VillageMasterPledgeBypasses.upgradeSubPledge(this, player, val);
		}
		else if(command.startsWith("subpledge_rename_check"))
			VillageMasterPledgeBypasses.renameSubPledgeCheck(this, player, command);
		else if(command.startsWith("subpledge_rename"))
			VillageMasterPledgeBypasses.renameSubPledge(this, player, command);
		else if(command.startsWith("create_clan"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				VillageMasterPledgeBypasses.createClan(this, player, val);
			}
		}
		else if(command.startsWith("create_academy"))
		{
			if(command.length() > 15)
			{
				String sub = command.substring(15, command.length());
				if(VillageMasterPledgeBypasses.createSubPledge(this, player, sub, Clan.SUBUNIT_ACADEMY, 5, ""))
					showChatWindow(player, "pledge/pl_create_ok_aca.htm");
				else
					showChatWindow(player, "pledge/pl_err_aca.htm");
			}
		}
		else if(command.startsWith("create_royal"))
		{
			if(command.length() > 15)
			{
				String[] sub = command.substring(13, command.length()).split(" ", 2);
				if(sub.length == 2)
				{
					if(VillageMasterPledgeBypasses.createSubPledge(this, player, sub[1], Clan.SUBUNIT_ROYAL1, 6, sub[0]))
						showChatWindow(player, "pledge/pl_create_ok_sub1.htm");
					else
						showChatWindow(player, "pledge/pl_err_sub.htm");
				}
			}
		}
		else if(command.startsWith("create_knight"))
		{
			if(command.length() > 16)
			{
				String[] sub = command.substring(14, command.length()).split(" ", 2);
				if(sub.length == 2)
				{
					if(VillageMasterPledgeBypasses.createSubPledge(this, player, sub[1], Clan.SUBUNIT_KNIGHT1, 7, sub[0]))
						showChatWindow(player, "pledge/pl_create_ok_sub2.htm");
					else
						showChatWindow(player, "pledge/pl_err_sub2.htm");
				}
			}
		}
		else if(command.startsWith("change_leader"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command);
			if(tokenizer.countTokens() != 3)
				return;

			tokenizer.nextToken();

			VillageMasterPledgeBypasses.changeLeader(this, player, Integer.parseInt(tokenizer.nextToken()), tokenizer.nextToken());
		}
		else if(command.startsWith("check_subpledge_exists"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command);
			tokenizer.nextToken();

			if(!VillageMasterPledgeBypasses.checkPlayerForClanLeader(this, player))
				return;

			int subunitId = Integer.parseInt(tokenizer.nextToken());
			String errorDialog = tokenizer.nextToken();
			String nextDialog = tokenizer.nextToken();

			Clan clan = player.getClan();
			SubUnit subUnit = clan.getSubUnit(subunitId);
			if(subUnit == null)
				showChatWindow(player, errorDialog);
			else
				showChatWindow(player, nextDialog);
		}
		else if(command.startsWith("cancel_change_leader"))
			VillageMasterPledgeBypasses.cancelLeaderChange(this, player);
		else if(command.startsWith("create_ally"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				if(VillageMasterPledgeBypasses.createAlly(player, val))
					showChatWindow(player, "pledge/al006.htm");
			}
		}
		else if(command.startsWith("dissolve_clan"))
			VillageMasterPledgeBypasses.dissolveClan(this, player);
		else if(command.startsWith("restore_clan"))
			VillageMasterPledgeBypasses.restoreClan(this, player);
		else if(command.startsWith("increase_clan_level"))
			VillageMasterPledgeBypasses.levelUpClan(this, player);
		else if(command.startsWith("learn_clan_skills"))
			VillageMasterPledgeBypasses.showClanSkillList(this, player);
		else if(command.startsWith("ShowCouponExchange"))
		{
			if(ItemFunctions.getItemCount(player, 8869) > 0 || ItemFunctions.getItemCount(player, 8870) > 0)
				command = "Multisell 800";
			else
				command = "Link villagemaster/reflect_weapon_master_noticket.htm";
			super.onBypassFeedback(player, command);
		}
		else if(command.equalsIgnoreCase("CertificationList"))
		{
			CertificationFunctions.showCertificationList(this, player);
		}
		else if(command.equalsIgnoreCase("GetCertification65"))
		{
			CertificationFunctions.getCertification65(this, player);
		}
		else if(command.equalsIgnoreCase("GetCertification70"))
		{
			CertificationFunctions.getCertification70(this, player);
		}
		else if(command.equalsIgnoreCase("GetCertification80"))
		{
			CertificationFunctions.getCertification80(this, player);
		}
		else if(command.equalsIgnoreCase("GetCertification75List"))
		{
			CertificationFunctions.getCertification75List(this, player);
		}
		else if(command.equalsIgnoreCase("GetCertification75C"))
		{
			CertificationFunctions.getCertification75(this, player, true);
		}
		else if(command.equalsIgnoreCase("GetCertification75M"))
		{
			CertificationFunctions.getCertification75(this, player, false);
		}
		else if(command.startsWith("Subclass"))
		{
			if(player.getServitor() != null)
			{
				player.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
				return;
			}

			// Саб класс нельзя получить или поменять, пока используется скилл или персонаж находится в режиме трансформации
			if(player.isActionsDisabled() || player.getTransformation() != 0)
			{
				player.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
				return;
			}

			if(player.getWeightPenalty() >= 3)
			{
				player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
				return;
			}

			if(player.getUsedInventoryPercents() >= 80)
			{
				player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
				return;
			}

			StringBuilder content = new StringBuilder("<html><body>");
			HtmlMessage html = new HtmlMessage(this);

			Map<Integer, SubClass> playerClassList = player.getSubClasses();
			Set<PlayerClass> subsAvailable;

			if(player.getLevel() < 40)
			{
				content.append("You must be level 40 or more to operate with your sub-classes.");
				content.append("</body></html>");
				html.setHtml(content.toString());
				player.sendPacket(html);
				return;
			}

			int classId = 0;
			int newClassId = 0;
			int intVal = 0;

			try
			{
				for(String id : command.substring(9, command.length()).split(" "))
				{
					if(intVal == 0)
					{
						intVal = Integer.parseInt(id);
						continue;
					}
					if(classId > 0)
					{
						newClassId = Integer.parseInt(id);
						continue;
					}
					classId = Integer.parseInt(id);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			switch(intVal)
			{
				case 1: // Возвращает список сабов, которые можно взять (см case 4)
					subsAvailable = getAvailableSubClasses(player, true);

					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

						for(PlayerClass subClass : subsAvailable)
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 4 ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
					}
					else
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime"));
						return;
					}
					break;
				case 2: // Установка уже взятого саба (см case 5)
					content.append("Change Subclass:<br>");

					final int baseClassId = player.getBaseClassId();

					if(playerClassList.size() < 2)
						content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 1\">Add subclass.</a>");
					else
					{
						content.append("Which class would you like to switch to?<br>");

						if(baseClassId == player.getActiveClassId())
							content.append(HtmlUtils.htmlClassName(baseClassId)).append(" <font color=\"LEVEL\">(Base Class)</font><br><br>");
						else
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(baseClassId).append("\">").append(HtmlUtils.htmlClassName(baseClassId)).append("</a> " + "<font color=\"LEVEL\">(Base Class)</font><br><br>");

						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.isBase())
								continue;
							int subClassId = subClass.getClassId();

							if(subClassId == player.getActiveClassId())
								content.append(HtmlUtils.htmlClassName(subClassId)).append("<br>");
							else
								content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(subClassId).append("\">").append(HtmlUtils.htmlClassName(subClassId)).append("</a><br>");
						}
					}
					break;
				case 3: // Отмена сабкласса - список имеющихся (см case 6)
					content.append("Change Subclass:<br>Which of the following sub-classes would you like to change?<br>");

					for(SubClass sub : playerClassList.values())
					{
						content.append("<br>");
						if(!sub.isBase())
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 6 ").append(sub.getClassId()).append("\">").append(HtmlUtils.htmlClassName(sub.getClassId())).append("</a><br>");
					}

					content.append("<br>If you change a sub-class, you'll start at level 40 after the 2nd class transfer.");
					break;
				case 4: // Добавление сабкласса - обработка выбора из case 1
					boolean allowAddition = false;

					subsAvailable = getAvailableSubClasses(player, true);
					if (subsAvailable != null && !subsAvailable.isEmpty())
						for (PlayerClass subClass : subsAvailable)
							if (subClass.ordinal() == classId)
							{
								allowAddition = true;
								break;
							}

					// Проверка хватает ли уровня
					if(player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel").addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
						allowAddition = false;
					}

					if(!playerClassList.isEmpty())
						for(SubClass subClass : playerClassList.values())
							if(subClass.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
							{
								player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel").addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
								allowAddition = false;
								break;
							}

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(new SystemMessage(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(player));
						return;
					}

					/*
					 * Если требуется квест - проверка прохождения Mimir's Elixir (Path to Subclass)
					 * Для камаэлей квест 236_SeedsOfChaos
					 * Если саб первый, то проверить начилие предмета, если не первый, то даём сабкласс.
					 * Если сабов нету, то проверяем наличие предмета.
					 */
					if(!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !playerClassList.isEmpty() && playerClassList.size() < 2 + Config.ALT_GAME_SUB_ADD)
						if(player.isQuestCompleted(234))
						{
							if(player.getRace() == Race.kamael)
							{
								allowAddition = player.isQuestCompleted(236);
								if(!allowAddition)
									player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestSeedsOfChaos"));
							}
							else
							{
								allowAddition = player.isQuestCompleted(235);
								if(!allowAddition)
									player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestMimirsElixir"));
							}
						}
						else
						{
							player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestFatesWhisper"));
							allowAddition = false;
						}

					if(allowAddition)
					{
						if(!player.addSubClass(classId, true, 0))
						{
							player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded"));
							return;
						}

						content.append("Add Subclass:<br>The subclass of <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(classId)).append("</font> has been added.");
						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
					}
					else
						html.setFile("villagemaster/SubClass_Fail.htm");
					break;
				case 5: // Смена саба на другой из уже взятых - обработка выбора из case 2
					/*
					 * If the character is less than level 75 on any of their
					 * previously chosen classes then disallow them to change to
					 * their most recently added sub-class choice.
					 */
					/*for(L2SubClass<?> sub : playerClassList.values())
						if(sub.isBase() && sub.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
						{
							player.sendMessage("You may not change to your subclass before you are level " + Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS, "Вы не можете добавить еще сабкласс пока у вас уровень " + Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS + " на Вашем предыдущем сабклассе.");
							return;
						}*/

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(new SystemMessage(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(player));
						return;
					}

					player.setActiveSubClass(classId, true);

					content.append("Change Subclass:<br>Your active subclass is now a <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(player.getActiveClassId())).append("</font>.");

					player.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS);
					// completed.
					break;
				case 6: // Отмена сабкласса - обработка выбора из case 3
					content.append("Please choose a subclass to change to. If the one you are looking for is not here, " + //
							"please seek out the appropriate master for that class.<br>" + //
					"<font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

					subsAvailable = getAvailableSubClasses(player, false);

					if(!subsAvailable.isEmpty())
						for(PlayerClass subClass : subsAvailable)
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 7 ").append(classId).append(" ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
					else
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime"));
						return;
					}
					break;
				case 7: // Отмена сабкласса - обработка выбора из case 6
					// player.sendPacket(SystemMsg.YOUR_PREVIOUS_SUBCLASS_WILL_BE_REMOVED_AND_REPLACED_WITH_THE_NEW_SUBCLASS_AT_LEVEL_40__DO_YOU_WISH_TO_CONTINUE); // Change confirmation.

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(new SystemMessage(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(player));
						return;
					}

					if(player.modifySubClass(classId, newClassId))
					{
						content.append("Change Subclass:<br>Your subclass has been changed to <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(newClassId)).append("</font>.");
						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
					}
					else
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded"));
						return;
					}
					break;
			}
			content.append("</body></html>");

			// If the content is greater than for a basic blank page,
			// then assume no external HTML file was assigned.
			if(content.length() > 26)
				html.setHtml(content.toString());

			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "villagemaster/" + pom + ".htm";
	}

	public void setLeader(Player leader, String newLeader)
	{
		if(!leader.isClanLeader())
		{
			leader.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		if(leader.getEvent(SiegeEvent.class) != null)
		{
			leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow"));
			return;
		}

		Clan clan = leader.getClan();
		SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		UnitMember member = mainUnit.getUnitMember(newLeader);

		if(member == null)
		{
			//FIX ME зачем 2-ве мессаги(VISTALL)
			//leader.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.S1IsNotMemberOfTheClan", leader).addString(newLeader));
			showChatWindow(leader, "villagemaster/clan-20.htm");
			return;
		}

		if(member.isLeaderOf() != Clan.SUBUNIT_NONE)
		{
			leader.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader"));
			return;
		}

		setLeader(leader, clan, mainUnit, member);
	}

	public static void setLeader(Player player, Clan clan, SubUnit unit, UnitMember newLeader)
	{
		player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2").addString(clan.getLeaderName()).addString(newLeader.getName()));
		//TODO: В данной редакции смена лидера производится сразу же.
		// Надо подумать над реализацией смены кланлидера в запланированный день недели.

		if(clan.getLevel() >= SiegeUtils.MIN_CLAN_SIEGE_LEVEL)
		{
			if(clan.getLeader() != null)
			{
				Player oldLeaderPlayer = clan.getLeader().getPlayer();
				if(oldLeaderPlayer != null)
					SiegeUtils.removeSiegeSkills(oldLeaderPlayer);
			}
			Player newLeaderPlayer = newLeader.getPlayer();
			if(newLeaderPlayer != null)
				SiegeUtils.addSiegeSkills(newLeaderPlayer);
		}

		unit.setLeader(newLeader, true);

		clan.broadcastClanStatus(true, true, false);
	}

	private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
	{
		final int charClassId = player.getBaseClassId();
		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		/**
		 * If the race of your main class is Elf or Dark Elf, you may not select
		 * each class as a subclass to the other class, and you may not select
		 * Overlord and Warsmith class as a subclass.
		 *
		 * You may not select a similar class as the subclass. The occupations
		 * classified as similar classes are as follows:
		 *
		 * Treasure Hunter, Plainswalker and Abyss Walker Hawkeye, Silver Ranger
		 * and Phantom Ranger Paladin, Dark Avenger, Temple Knight and Shillien
		 * Knight Warlocks, Elemental Summoner and Phantom Summoner Elder and
		 * Shillien Elder Swordsinger and Bladedancer Sorcerer, Spellsinger and
		 * Spellhowler
		 *
		 * Kamael могут брать только сабы Kamael
		 * Другие классы не могут брать сабы Kamael
		 *
		 */
		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return Collections.emptySet();

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			if(!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf))
			{
				if(!availSub.isOfRace(npcRace))
					availSubs.remove(availSub);
			}
			else if(!availSub.isOfType(npcTeachType))
				availSubs.remove(availSub);

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.kamael))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
				if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
					availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private Race getVillageMasterRace()
	{
		switch(getTemplate().getRace())
		{
			case 14:
				return Race.human;
			case 15:
				return Race.elf;
			case 16:
				return Race.darkelf;
			case 17:
				return Race.orc;
			case 18:
				return Race.dwarf;
			case 25:
				return Race.kamael;
		}
		return null;
	}

	private ClassType getVillageMasterTeachType()
	{
		switch(getNpcId())
		{
			case 30031:
			case 30037:
			case 30070:
			case 30120:
			case 30191:
			case 30289:
			case 30857:
			case 30905:
			case 32095:
			case 30141:
			case 30305:
			case 30358:
			case 30359:
			case 31336:
				return ClassType.Priest;

			case 30115:
			case 30174:
			case 30175:
			case 30176:
			case 30694:
			case 30854:
			case 31331:
			case 31755:
			case 31996:
			case 32098:
			case 32147:
			case 32160:
			case 30154:
			case 31285:
			case 31288:
			case 31326:
			case 31977:
			case 32150:
				return ClassType.Mystic;
			default:
		}

		return ClassType.Fighter;
	}
}