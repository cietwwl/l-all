package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.petition.PetitionMainGroup;
import org.mmocore.gameserver.model.petition.PetitionSubGroup;
import org.mmocore.gameserver.network.l2.s2c.ExResponseShowContents;

/**
 * @author VISTALL
 */
public class RequestExShowStepThree extends L2GameClientPacket
{
	private int _subId;

	@Override
	protected void readImpl()
	{
		_subId = readC();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_NEW_PETITION_SYSTEM)
			return;

		PetitionMainGroup group = player.getPetitionGroup();
		if(group == null)
			return;

		PetitionSubGroup subGroup = group.getSubGroup(_subId);
		if(subGroup == null)
			return;

		player.sendPacket(new ExResponseShowContents(subGroup.getDescription(player.getLanguage())));
	}
}