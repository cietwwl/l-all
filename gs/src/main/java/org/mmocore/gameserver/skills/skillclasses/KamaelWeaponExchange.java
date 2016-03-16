package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExAutoSoulShot;
import org.mmocore.gameserver.network.l2.s2c.InventoryUpdate;
import org.mmocore.gameserver.network.l2.s2c.ShortCutInit;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.item.WeaponTemplate;


public class KamaelWeaponExchange extends Skill
{
	public KamaelWeaponExchange(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player p = (Player) activeChar;
		if(p.isInStoreMode() || p.isProcessingRequest())
			return false;

		ItemInstance item = activeChar.getActiveWeaponInstance();
		if(item != null && ((WeaponTemplate) item.getTemplate()).getKamaelConvert() == 0)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_CONVERT_THIS_ITEM);
			return false;
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		final Player player = (Player) activeChar;
		final ItemInstance item = activeChar.getActiveWeaponInstance();
		if(item == null)
			return;

		int itemId = ((WeaponTemplate) item.getTemplate()).getKamaelConvert();

		if(itemId == 0)
			return;

		player.getInventory().unEquipItem(item);
		player.sendPacket(new InventoryUpdate().addRemovedItem(item));
		item.setItemId(itemId);

		player.sendPacket(new ShortCutInit(player));
		for(int shotId : player.getAutoSoulShot())
			player.sendPacket(new ExAutoSoulShot(shotId, true));

		player.sendPacket(new InventoryUpdate().addNewItem(item));
		player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_EQUIPPED_YOUR_S1).addItemNameWithAugmentation(item));
		player.getInventory().equipItem(item);
	}
}