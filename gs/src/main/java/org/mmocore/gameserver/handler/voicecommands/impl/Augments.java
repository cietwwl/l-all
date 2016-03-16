package org.mmocore.gameserver.handler.voicecommands.impl;

import org.mmocore.gameserver.data.xml.holder.OptionDataHolder;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.triggers.TriggerInfo;
import org.mmocore.gameserver.templates.OptionDataTemplate;

public class Augments implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "augments" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		for (int slot = 0; slot < Inventory.PAPERDOLL_MAX; slot++)
		{
			int[] augmentations = player.getInventory().getPaperdollAugmentationId(slot);
			if(augmentations == ItemInstance.EMPTY_AUGMENTATIONS)
				continue;

			StringBuilder info = new StringBuilder(30);
			info.append(slot);
			info.append(" ");
			info.append(augmentations[0]);
			info.append(":");
			info.append(augmentations[1]);

			getInfo(info, augmentations[0]);
			getInfo(info, augmentations[1]);
			player.sendMessage(info.toString());
		}
		return true;
	}

	private void getInfo(StringBuilder info, int id)
	{
		OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(id);
		if (template != null)
		{
			if (!template.getSkills().isEmpty())
				for (SkillEntry s : template.getSkills())
				{
					info.append(" ");
					info.append(s.getId());
					info.append("/");
					info.append(s.getLevel());
				}
			if (!template.getTriggerList().isEmpty())
				for (TriggerInfo t : template.getTriggerList())
				{
					info.append(" ");
					info.append(t.id);
					info.append("/");
					info.append(t.level);
					info.append(" ");
					info.append(t.getType());
					info.append(":");
					info.append(t.getChance());
				}
		}
	}
}