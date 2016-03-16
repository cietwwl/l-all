package org.mmocore.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.skills.EffectType;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.effects.EffectTemplate;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 13:01/02.02.2011
 */
public class CharacterEffectDAO
{
	private static final int SUMMON_SKILL_OFFSET = 100000;
	private static final Logger _log = LoggerFactory.getLogger(CharacterEffectDAO.class);
	private static final CharacterEffectDAO _instance = new CharacterEffectDAO();

	CharacterEffectDAO()
	{
		//
	}

	public static CharacterEffectDAO getInstance()
	{
		return _instance;
	}

	public void select(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `character_effects` WHERE `object_id`=? AND `class_index`=? ORDER BY `order` ASC");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getActiveClassId());
			rset = statement.executeQuery();
			int size = rset.getFetchSize();
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				long effectCurTime = rset.getLong("effect_cur_time");
				long duration = rset.getLong("duration");

				SkillEntry skill = SkillTable.getInstance().getSkillEntry(skillId, skillLvl);
				if(skill == null)
					continue;

				for(EffectTemplate et : skill.getTemplate().getEffectTemplates())
				{
					if(et == null)
						continue;
					Env env = new Env(player, player, skill);
					Effect effect = et.getEffect(env);
					if(effect == null || effect.isOneTime())
						continue;

					effect.setCount(effectCount);
					effect.setPeriod(effectCount == 1 ? duration - effectCurTime : duration);

					player.getEffectList().addEffect(effect);
					effect.fixStartTime(size--);
				}
			}

			DbUtils.closeQuietly(statement, rset);

			statement = con.prepareStatement("DELETE FROM character_effects WHERE object_id = ? AND class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getActiveClassId());
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.error("CharacterEffectDAO.select(Player): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public void delete(int objectId, int classIndex)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_effects WHERE object_id = ? AND class_index=?");
			statement.setInt(1, objectId);
			statement.setInt(2, classIndex);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("Could not delete effects active effects data!" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void insert(Player player)
	{
		List<Effect> effects = player.getEffectList().getAllEffects();
		if(effects.isEmpty())
			return;

		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			int order = 0;
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects` (`object_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`class_index`) VALUES");

			StringBuilder sb;
			for(Effect effect : effects)
				if(effect.isInUse() && !effect.getSkill().getTemplate().isToggle() && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime)
				{
					if(effect.isSaveable())
					{
						sb = new StringBuilder("(");
						sb.append(player.getObjectId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(",");
						sb.append(player.getActiveClassId()).append(")");
						b.write(sb.toString());
					}
					while((effect = effect.getNext()) != null && effect.isSaveable())
					{
						sb = new StringBuilder("(");
						sb.append(player.getObjectId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(",");
						sb.append(player.getActiveClassId()).append(")");
						b.write(sb.toString());
					}
					order++;
				}

			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.error("Could not store active effects data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
