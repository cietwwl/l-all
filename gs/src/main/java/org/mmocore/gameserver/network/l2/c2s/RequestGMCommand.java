package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.pledge.SubUnit;
import org.mmocore.gameserver.network.l2.s2c.ExGMViewQuestItemList;
import org.mmocore.gameserver.network.l2.s2c.GMHennaInfo;
import org.mmocore.gameserver.network.l2.s2c.GMViewCharacterInfo;
import org.mmocore.gameserver.network.l2.s2c.GMViewItemList;
import org.mmocore.gameserver.network.l2.s2c.GMViewPledgeInfo;
import org.mmocore.gameserver.network.l2.s2c.GMViewQuestInfo;
import org.mmocore.gameserver.network.l2.s2c.GMViewSkillInfo;
import org.mmocore.gameserver.network.l2.s2c.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		// readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		Player target = World.getPlayer(_targetName);
		if(target == null)
			return;

		if(!player.getPlayerAccess().CanViewChar)
			return;

		switch(_command)
		{
			case 1:
				player.sendPacket(new GMViewCharacterInfo(target));
				player.sendPacket(new GMHennaInfo(target));
				break;
			case 2:
				if(target.getClan() != null)
				{
					for(SubUnit subUnit : target.getClan().getAllSubUnits())
						player.sendPacket(new GMViewPledgeInfo(target.getName(), target.getClan(), subUnit));
				}
				break;
			case 3:
				player.sendPacket(new GMViewSkillInfo(target));
				break;
			case 4:
				player.sendPacket(new GMViewQuestInfo(target));
				break;
			case 5:
				ItemInstance[] items = target.getInventory().getItems();
				int questSize = 0;
				for(ItemInstance item : items)
					if(item.getTemplate().isQuest())
						questSize ++;
				player.sendPacket(new GMViewItemList(target, items, items.length - questSize));
				player.sendPacket(new ExGMViewQuestItemList(target, items, questSize));

				player.sendPacket(new GMHennaInfo(target));
				break;
			case 6:
				player.sendPacket(new GMViewWarehouseWithdrawList(target));
				break;
		}
	}
}