package org.mmocore.gameserver.model.actor.instances.player.tasks;

import java.util.List;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.dao.CharacterServitorDAO;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 14:48/19.09.2011
 */
public class ServitorSummonTask extends RunnableImpl
{
	private Player _player;

	public ServitorSummonTask(Player player)
	{
		_player = player;
	}

	@Override
	public void runImpl() throws Exception
	{
		List<int[]> saveServitors = _player.getSavedServitors();
		CharacterServitorDAO.getInstance().delete(_player.getObjectId());

		for(int[] ar : saveServitors)
		{
			switch(ar[0])
			{
				case Servitor.PET_TYPE:
					ItemInstance item = _player.getInventory().getItemByObjectId(ar[1]);
					if(item == null)
						continue;

					_player.summonPet(item, Location.findPointToStay(_player, 10, 20));
					break;
				case Servitor.SUMMON_TYPE:
					_player.summonSummon(ar[1]);
					break;
			}
		}
	}
}
