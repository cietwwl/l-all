package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.EnchantSkillLearn;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExEnchantSkillInfo;
import org.mmocore.gameserver.network.l2.s2c.ExEnchantSkillResult;
import org.mmocore.gameserver.network.l2.s2c.SkillList;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.TimeStamp;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.tables.SkillTreeTable;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Log;

/**
 * Format (ch) dd
 */
public final class RequestExEnchantSkillSafe extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (activeChar.isSitting() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}

		if(activeChar.getLevel() < 76)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL);
			return;
		}

		if(activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS);
			return;
		}

		if(activeChar.getTransformation() != 0 || activeChar.isInCombat() || activeChar.isInBoat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_);
			return;
		}

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);

		if(sl == null)
			return;

		final SkillEntry se = activeChar.getKnownSkill(_skillId);
		if (se == null)
			return;
		if (se.getLockedSkill() != null)
			return;
		int slevel = se.getLevel();

		int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel());

		// already knows the skill with this level
		if(slevel >= enchantLevel)
			return;

		// Можем ли мы перейти с текущего уровня скилла на данную заточку
		if(slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1)
		{
			activeChar.sendMessage("Incorrect enchant level.");
			return;
		}

		SkillEntry skill = SkillTable.getInstance().getSkillEntry(_skillId, enchantLevel);
		if(skill == null)
			return;

		int[] cost = sl.getCost();
		int requiredSp = (int)(cost[1] * sl.getCostMult() * Config.ALT_SKILL_SAFE_ENCHANT_SP_MODIFIER);
		int requiredAdena = (int)(cost[0] * sl.getCostMult() * Config.ALT_SKILL_SAFE_ENCHANT_ADENA_MODIFIER);

		int rate = sl.getRate(activeChar);

		if(activeChar.getSp() < requiredSp)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(ItemFunctions.getItemCount(activeChar, SkillTreeTable.SAFE_ENCHANT_BOOK) == 0)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}

		ItemFunctions.deleteItem(activeChar, SkillTreeTable.SAFE_ENCHANT_BOOK, 1);
		ItemFunctions.deleteItem(activeChar, ItemTemplate.ITEM_ID_ADENA, requiredAdena);
		activeChar.addExpAndSp(0, -requiredSp);

		if(Rnd.chance(rate))
		{
			TimeStamp ts = null;
			if (Config.ALT_SKILL_ENCHANT_UPDATE_REUSE)
				ts = activeChar.getSkillReuse(activeChar.getKnownSkill(_skillId));

			activeChar.addSkill(skill, true);
			if (ts != null && ts.hasNotPassed())
				activeChar.disableSkill(skill, ts.getReuseCurrent());

			activeChar.sendPacket(new SystemMessage(SystemMsg.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(1));
			activeChar.sendPacket(new SkillList(activeChar));
			RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
			Log.add(activeChar.getName() + "|Successfully safe enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.SKILL_ENCHANT_FAILED_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0));
			Log.add(activeChar.getName() + "|Failed to safe enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
		}

		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
	}
}