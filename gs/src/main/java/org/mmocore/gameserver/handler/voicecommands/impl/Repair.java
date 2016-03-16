package org.mmocore.gameserver.handler.voicecommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.dao.ItemsDAO;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.actor.instances.player.AccountPlayerInfo;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.ItemInstance.ItemLocation;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.napile.pair.primitive.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repair implements IVoicedCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(Repair.class);

	private final String[] _commandList = new String[] { "repair" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!target.isEmpty())
		{
			if(activeChar.getName().equalsIgnoreCase(target))
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCantRepairYourself"));
				return false;
			}

			int objId = 0;

			for(IntObjectPair<AccountPlayerInfo> e : activeChar.getAccountChars().entrySet())
			{
				if(e.getValue().getName().equalsIgnoreCase(target))
				{
					objId = e.getKey();
					break;
				}
			}

			if(objId == 0)
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCanRepairOnlyOnSameAccount"));
				return false;
			}
			else if(World.getPlayer(objId) != null)
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsOnline"));
				return false;
			}

			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rs = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT karma FROM characters WHERE obj_Id=?");
				statement.setInt(1, objId);
				statement.execute();
				rs = statement.getResultSet();

				int karma = 0;

				rs.next();

				karma = rs.getInt("karma");

				DbUtils.close(statement, rs);

				if(karma > 0)
				{
					statement = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE obj_Id=?");
					statement.setInt(1, objId);
					statement.execute();
					DbUtils.close(statement);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET x=0, y=0, z=0 WHERE obj_Id=?");
					statement.setInt(1, objId);
					statement.execute();
					DbUtils.close(statement);

					Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId, ItemLocation.PAPERDOLL);
					for(ItemInstance item : items)
					{
						item.setEquipped(false);
						item.setLocData(0);
						item.setLocation(item.getTemplate().isStoreable() ? ItemLocation.WAREHOUSE : ItemLocation.INVENTORY);
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();
					}
				}

				statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=? AND type='user-var' AND name='reflection'");
				statement.setInt(1, objId);
				statement.execute();
				DbUtils.close(statement);

				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.RepairDone"));
				return true;
			}
			catch(Exception e)
			{
				_log.error("", e);
				return false;
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rs);
			}
		}
		else
			activeChar.sendMessage(".repair <name>");

		return false;
	}
}