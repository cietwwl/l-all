package org.mmocore.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.mmocore.commons.dbutils.DbUtils;
import org.mmocore.gameserver.database.DatabaseFactory;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.templates.PlayerTemplate;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.item.WeaponTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class CharTemplateTable
{
	private static final Logger _log = LoggerFactory.getLogger(CharTemplateTable.class);

	private static CharTemplateTable _instance;

	private Map<Integer, PlayerTemplate> _templates;

	public static CharTemplateTable getInstance()
	{
		if(_instance == null)
			_instance = new CharTemplateTable();
		return _instance;
	}

	private CharTemplateTable()
	{
		_templates = new HashMap<Integer, PlayerTemplate>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM class_list, char_templates, lvlupgain WHERE class_list.id = char_templates.classId AND class_list.id = lvlupgain.classId ORDER BY class_list.id");
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet set = new StatsSet();
				ClassId classId = ClassId.VALUES[rset.getInt("class_list.id")];
				set.set("classId", rset.getInt("class_list.id"));
				set.set("className", rset.getString("char_templates.className"));
				set.set("raceId", rset.getInt("char_templates.RaceId"));
				set.set("baseSTR", rset.getInt("char_templates.STR"));
				set.set("baseCON", rset.getInt("char_templates.CON"));
				set.set("baseDEX", rset.getInt("char_templates.DEX"));
				set.set("baseINT", rset.getInt("char_templates._INT"));
				set.set("baseWIT", rset.getInt("char_templates.WIT"));
				set.set("baseMEN", rset.getInt("char_templates.MEN"));
				set.set("baseHpMax", rset.getDouble("lvlupgain.defaultHpBase"));
				set.set("lvlHpAdd", rset.getDouble("lvlupgain.defaultHpAdd"));
				set.set("lvlHpMod", rset.getDouble("lvlupgain.defaultHpMod"));
				set.set("baseMpMax", rset.getDouble("lvlupgain.defaultMpBase"));
				set.set("baseCpMax", rset.getDouble("lvlupgain.defaultCpBase"));
				set.set("lvlCpAdd", rset.getDouble("lvlupgain.defaultCpAdd"));
				set.set("lvlCpMod", rset.getDouble("lvlupgain.defaultCpMod"));
				set.set("lvlMpAdd", rset.getDouble("lvlupgain.defaultMpAdd"));
				set.set("lvlMpMod", rset.getDouble("lvlupgain.defaultMpMod"));
				set.set("baseHpReg", 0.01);
				set.set("baseCpReg", 0.01);
				set.set("baseMpReg", 0.01);
				set.set("basePAtk", rset.getInt("char_templates.p_atk"));
				set.set("basePDef", /* classId.isMage()? 77 : 129 */rset.getInt("char_templates.p_def"));
				set.set("baseMAtk", rset.getInt("char_templates.m_atk"));
				set.set("baseMDef", 41 /* rset.getInt("char_templates.m_def") */);
				set.set("classBaseLevel", rset.getInt("lvlupgain.class_lvl"));
				set.set("basePAtkSpd", rset.getInt("char_templates.p_spd"));
				set.set("baseMAtkSpd", classId.isMage() ? 166 : 333 /* rset.getInt("char_templates.m_spd") */);
				set.set("baseCritRate", rset.getInt("char_templates.critical"));
				set.set("baseWalkSpd", rset.getInt("char_templates.walk_spd"));
				set.set("baseRunSpd", rset.getInt("char_templates.run_spd"));
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				set.set("baseAtkRange", 40);
				set.set("baseAtkType", WeaponTemplate.WeaponType.FIST);

				set.set("spawnX", rset.getInt("char_templates.x"));
				set.set("spawnY", rset.getInt("char_templates.y"));
				set.set("spawnZ", rset.getInt("char_templates.z"));

				PlayerTemplate ct;

				//
				// Male class
				//
				set.set("isMale", true);
				// set.setMUnk1(rset.getDouble(27));
				// set.setMUnk2(rset.getDouble(28));
				set.set("collision_radius", rset.getDouble("char_templates.m_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.m_col_h"));
				ct = new PlayerTemplate(set);
				// 5items must go here
				for(int x = 1; x < 6; x++)
					if(rset.getInt("char_templates.items" + x) != 0)
						ct.addItem(rset.getInt("char_templates.items" + x));
				_templates.put(ct.classId.getId(), ct);

				//
				// Female class
				//
				set.set("isMale", false);
				// set.setFUnk1(rset.getDouble(31));
				// set.setFUnk2(rset.getDouble(32));
				set.set("collision_radius", rset.getDouble("char_templates.f_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.f_col_h"));
				ct = new PlayerTemplate(set);
				// 5items must go here
				for(int x = 1; x < 6; x++)
				{
					int itemId = rset.getInt("char_templates.items" + x);
					if(itemId != 0)
						ct.addItem(itemId);
				}
				_templates.put(ct.classId.getId() | 0x100, ct);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		_log.info("CharTemplateTable: Loaded " + _templates.size() + " Character Templates.");
	}

	public PlayerTemplate getTemplate(ClassId classId, boolean female)
	{
		return getTemplate(classId.getId(), female);
	}

	public PlayerTemplate getTemplate(int classId, boolean female)
	{
		int key = classId;
		if(female)
			key |= 0x100;
		return _templates.get(key);
	}
}
