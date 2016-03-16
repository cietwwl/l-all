package org.mmocore.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 23:41/20.08.2011
 */
public class AccountLectureMarkDAO
{
	private static final Logger _log = LoggerFactory.getLogger(AccountLectureMarkDAO.class);
	private static final AccountLectureMarkDAO _instance = new AccountLectureMarkDAO();

	public static final String SELECT_SQL_QUERY = "SELECT lecture_mark FROM account_lecture_mark WHERE account=?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO account_lecture_mark(account, lecture_mark) VALUES (?,?)";

	public static AccountLectureMarkDAO getInstance()
	{
		return _instance;
	}

	public int select(String account)
	{
		int val = -1;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
				val = rset.getInt("lecture_mark");
		}
		catch(Exception e)
		{
			_log.error("AccountLectureMarkDAO.select(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return val;
	}

	public void replace(String account, int mark)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setString(1, account);
			statement.setInt(2, mark);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("AccountLectureMarkDAO.replace(String, int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
