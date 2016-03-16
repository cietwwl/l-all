package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Recipe;
import org.mmocore.gameserver.model.Player;

/**
 * format ddddd
 */
public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private int _id;
	private boolean _isDwarvenRecipe;
	private int _status;
	private int _curMP;
	private int _maxMP;

	public RecipeItemMakeInfo(Player player, Recipe recipeList, int status)
	{
		_id = recipeList.getId();
		_isDwarvenRecipe = recipeList.isDwarvenRecipe();
		_status = status;
		_curMP = (int) player.getCurrentMp();
		_maxMP = player.getMaxMp();
		//
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xdd);
		writeD(_id); //ID рецепта
		writeD(_isDwarvenRecipe ? 0x00 : 0x01);
		writeD(_curMP);
		writeD(_maxMP);
		writeD(_status); //итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
	}
}