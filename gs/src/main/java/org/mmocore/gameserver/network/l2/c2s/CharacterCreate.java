package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.data.client.holder.NpcNameLineHolder;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.instancemanager.QuestManager;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.SkillLearn;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.quest.Quest;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.CharacterCreateFail;
import org.mmocore.gameserver.network.l2.s2c.CharacterCreateSuccess;
import org.mmocore.gameserver.network.l2.s2c.CharacterSelectionInfo;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.PlayerTemplate;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	protected void runImpl()
	{
		for(ClassId cid : ClassId.VALUES)
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		if(CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 7)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE) || NpcNameLineHolder.getInstance().isBlackListContainsName(_name))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		else if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}

		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
			return;

		sendPacket(CharacterCreateSuccess.STATIC);

		initNewChar(getClient(), newChar);
	}

	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();

		Player.restoreCharSubClasses(newChar);

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);

		newChar.setLoc(template.spawnLoc);
		newChar.setTitle("");

		for(ItemTemplate i : template.getItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getItemId());
			newChar.getInventory().addItem(item);

			if(item.getItemId() == 5588) // tutorial book
				newChar.registerShortCut(new ShortCut(11, 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));

			if(item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON))
				newChar.getInventory().equipItem(item);
		}

		// Adventurer's Scroll of Escape
		ItemInstance item = ItemFunctions.createItem(10650);
		item.setCount(5);
		newChar.getInventory().addItem(item);

		// Scroll of Escape: Kamael Village
		item = ItemFunctions.createItem(9716);
		item.setCount(10);
		newChar.getInventory().addItem(item);

		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
			newChar.addSkill(SkillTable.getInstance().getSkillEntry(skill.getId(), skill.getLevel()), true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new ShortCut(2, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		// понял как на панельке отобразить. нц софт 10-11 панели сделали(by VISTALL)
		// fly transform
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 911, 1, 1));
		newChar.registerShortCut(new ShortCut(3, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 884, 1, 1));
		newChar.registerShortCut(new ShortCut(4, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 885, 1, 1));
		// air ship
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_AIRSHIP, ShortCut.TYPE_ACTION, 70, 0, 1));

		startTutorialQuest(newChar);

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail
		newChar.setIsOnline(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();

		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
	}

	public static void startTutorialQuest(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, Quest.CREATED);
	}
}