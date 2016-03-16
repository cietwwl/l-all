package org.mmocore.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.instances.PetInstance;
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
 * @date 15:16/19.09.2011
 */
public class PetEffectDAO
{
	private static final Logger _log = LoggerFactory.getLogger(PetEffectDAO.class);

	private static PetEffectDAO _instance = new PetEffectDAO();

	public static PetEffectDAO getInstance()
	{
		return _instance;
	}

	private PetEffectDAO()
	{
	}

	public void select(PetInstance pet)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `pet_effects` WHERE `object_id`=? ORDER BY `order` ASC");
			statement.setInt(1, pet.getControlItemObjId());
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
					Env env = new Env(pet, pet, skill);
					Effect effect = et.getEffect(env);
					if(effect == null || effect.isOneTime())
						continue;

					effect.setCount(effectCount);
					effect.setPeriod(effectCount == 1 ? duration - effectCurTime : duration);

					pet.getEffectList().addEffect(effect);
					effect.fixStartTime(size--);
				}
			}

			delete(pet.getControlItemObjId());
		}
		catch(final Exception e)
		{
			_log.error("ServitorEffectDAO.select(Player): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void delete(int objectId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM pet_effects WHERE object_id = ?");
			statement.setInt(1, objectId);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("ServitorEffectDAO.delete(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void insert(PetInstance pet)
	{
		List<Effect> effects = pet.getEffectList().getAllEffects();
		if(effects.isEmpty())
			return;

		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			int order = 0;
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `pet_effects` (`object_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`) VALUES");

			StringBuilder sb;
			for(Effect effect : effects)
				if(effect.isInUse() && !effect.getSkill().getTemplate().isToggle() && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime)
				{
					if(effect.isSaveable())
					{
						sb = new StringBuilder("(");
						sb.append(pet.getControlItemObjId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(")");
						b.write(sb.toString());
					}
					while((effect = effect.getNext()) != null && effect.isSaveable())
					{
						sb = new StringBuilder("(");
						sb.append(pet.getControlItemObjId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(")");
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