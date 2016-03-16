package org.mmocore.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.data.xml.holder.HennaHolder;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.templates.Henna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 17:49/13.09.2011
 */
public class CharacterHennaDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterHennaDAO.class);

	private static CharacterHennaDAO _instance = new CharacterHennaDAO();

	public static CharacterHennaDAO getInstance()
	{
		return _instance;
	}

	private CharacterHennaDAO()
	{
	}

	public void select(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getActiveClassId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot < 1 || slot > 3)
					continue;

				final int symbol_id = rset.getInt("symbol_id");

				if(symbol_id != 0)
				{
					final Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
					if(tpl != null)
						player.getHennas()[slot - 1] = tpl;
				}
			}
		}
		catch(final Exception e)
		{
			_log.warn("could not restore henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void insert(Player player, int index, int symbolId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, symbolId);
			statement.setInt(3, index);
			statement.setInt(4, player.getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not save char henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(Player player, int index)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, index);
			statement.setInt(3, player.getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("could not remove char henna: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}