package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.ShortCut;
import org.mmocore.gameserver.network.l2.s2c.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
	private int _type, _id, _slot, _page, _lvl, _characterType;

	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();

		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_page < 0 || _page > ShortCut.PAGE_MAX)
		{
			player.sendActionFailed();
			return;
		}

		if(_type <= 0 || _type > ShortCut.TYPE_MAX)
		{
			player.sendActionFailed();
			return;
		}

		ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
		player.sendPacket(new ShortCutRegister(player, shortCut));
		player.registerShortCut(shortCut);
	}
}