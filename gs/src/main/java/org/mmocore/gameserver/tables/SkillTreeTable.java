package org.mmocore.gameserver.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.SkillLearn;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.base.EnchantSkillLearn;
import org.mmocore.gameserver.network.l2.s2c.ShortCutRegister;
import org.mmocore.gameserver.network.l2.s2c.SkillList;
import org.mmocore.gameserver.skills.LockedSkillEntry;
import org.mmocore.gameserver.skills.SkillEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillTreeTable
{
	public static final int NORMAL_ENCHANT_BOOK = 6622;
	public static final int SAFE_ENCHANT_BOOK = 9627;
	public static final int CHANGE_ENCHANT_BOOK = 9626;
	public static final int UNTRAIN_ENCHANT_BOOK = 9625;

	private static final Logger _log = LoggerFactory.getLogger(SkillTreeTable.class);

	private static SkillTreeTable _instance;

	public static Map<Integer, List<EnchantSkillLearn>> _enchant = new ConcurrentHashMap<Integer, List<EnchantSkillLearn>>();

	public static SkillTreeTable getInstance()
	{
		if(_instance == null)
			_instance = new SkillTreeTable();
		return _instance;
	}

	private SkillTreeTable()
	{
		_log.info("SkillTreeTable: Loaded " + _enchant.size() + " enchanted skills.");
	}

	public static void checkSkill(Player player, SkillEntry skill)
	{
		SkillLearn learn = SkillAcquireHolder.getInstance().getSkillLearn(player, skill.getId(), levelWithoutEnchant(skill), AcquireType.NORMAL);
		if(learn == null)
			return;
		if (learn.isClicked() && !Config.ALT_REMOVE_FORGOTTEN_SCROLLS_ON_DELEVEL)
			return;
		if(learn.getMinLevel() > player.getLevel() + 10)
		{
			player.removeSkill(skill, true);

			// если у нас низкий лвл для скила, то заточка обнуляется 100%
			// и ищем от большего к меньшему подходящий лвл для скила
			for(int i = skill.getTemplate().getBaseLevel(); i != 0; i--)
			{
				SkillLearn learn2 = SkillAcquireHolder.getInstance().getSkillLearn(player, skill.getId(), i, AcquireType.NORMAL);
				if(learn2 == null)
					continue;
				if(learn2.getMinLevel() > player.getLevel() + 10)
					continue;

				SkillEntry newSkill = SkillTable.getInstance().getSkillEntry(skill.getId(), i);
				if(newSkill != null)
				{
					player.addSkill(newSkill, true);
					break;
				}
			}
		}
	}

	public static void lockMaxEnchant(Player player, boolean sendInfo)
	{
		for (SkillEntry skill : player.getAllSkillsArray())
		{
			final int skillDisplayLvl = skill.getDisplayLevel();
			if (skillDisplayLvl < 100) // не заточен
				continue;

			final int skillId = skill.getId();
			final List<EnchantSkillLearn> enchants = _enchant.get(skillId);
			if (enchants == null) // не найдены варианты заточки
				continue;

			for(EnchantSkillLearn e : enchants)
				if (e.getLevel() == skillDisplayLvl)
				{
					final int maxLevel = e.getBaseLevel() + e.getMaxLevel() * (skillDisplayLvl / 100); // уровень скилла для максимума заточки этой ветки
					if (skill.getLevel() < maxLevel) // если скилл уже заточен на максимум то не трогаем
					{
						final SkillEntry newSkill = SkillTable.getInstance().getSkillEntry(skillId, maxLevel);
						if (newSkill != null)
						{
							player.addSkill(new LockedSkillEntry(skill.getEntryType(), newSkill.getTemplate(), skill.getTemplate()), false);
							for (ShortCut sc : player.getAllShortCuts())
								if (sc.getId() == skillId && sc.getType() == ShortCut.TYPE_SKILL)
								{
									sc.setDisplayLevel(newSkill.getDisplayLevel());
									player.sendPacket(new ShortCutRegister(player, sc));
								}
						}
					}
					break;
				}
		}

		if (sendInfo)
			player.sendPacket(new SkillList(player));
	}

	public static void unlockMaxEnchant(Player player, boolean sendInfo)
	{
		for (SkillEntry skill : player.getAllSkillsArray())
		{
			if (skill.getDisplayLevel() < 100) // не заточен
				continue;
			if (skill.getLockedSkill() == null) // не изменен
				continue;

			player.addSkill(new SkillEntry(skill.getEntryType(), skill.getLockedSkill()), false);
			for (ShortCut sc : player.getAllShortCuts())
				if (sc.getId() == skill.getId() && sc.getType() == ShortCut.TYPE_SKILL)
				{
					sc.setDisplayLevel(sc.getLevel());
					player.sendPacket(new ShortCutRegister(player, sc));
				}
		}

		if (sendInfo)
			player.sendPacket(new SkillList(player));
	}

	private static int levelWithoutEnchant(SkillEntry skill)
	{
		return skill.getDisplayLevel() > 100 ? skill.getTemplate().getBaseLevel() : skill.getLevel();
	}

	public static List<EnchantSkillLearn> getFirstEnchantsForSkill(int skillid)
	{
		List<EnchantSkillLearn> result = new ArrayList<EnchantSkillLearn>();

		List<EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return result;

		for(EnchantSkillLearn e : enchants)
			if(e.getLevel() % 100 == 1)
				result.add(e);

		return result;
	}

	public static int isEnchantable(SkillEntry skill)
	{
		List<EnchantSkillLearn> enchants = _enchant.get(skill.getId());
		if(enchants == null)
			return 0;

		for(EnchantSkillLearn e : enchants)
			if(e.getBaseLevel() <= skill.getLevel())
				return 1;

		return 0;
	}

	public static List<EnchantSkillLearn> getEnchantsForChange(int skillid, int level)
	{
		List<EnchantSkillLearn> result = new ArrayList<EnchantSkillLearn>();

		List<EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return result;

		for(EnchantSkillLearn e : enchants)
			if(e.getLevel() % 100 == level % 100)
				result.add(e);

		return result;
	}

	public static EnchantSkillLearn getSkillEnchant(int skillid, int level)
	{
		List<EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return null;

		for(EnchantSkillLearn e : enchants)
			if(e.getLevel() == level)
				return e;
		return null;
	}

	/**
	 * Преобразует уровень скила из клиентского представления в серверное
	 * @param baseLevel базовый уровень скила - максимально возможный без заточки
	 * @param level - текущий уровень скила
	 * @param enchantlevels
	 * @return уровень скила
	 */
	public static int convertEnchantLevel(int baseLevel, int level, int enchantlevels)
	{
		if(level < 100)
			return level;
		return baseLevel + ((level - level % 100) / 100 - 1) * enchantlevels + level % 100;
	}

	public static void unload()
	{
		if(_instance != null)
			_instance = null;

		_enchant.clear();
	}
}