package org.mmocore.gameserver.skills;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.tables.SkillTable;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillsEngine
{
	private static final Logger _log = LoggerFactory.getLogger(SkillsEngine.class);

	private static final SkillsEngine _instance = new SkillsEngine();

	public static SkillsEngine getInstance()
	{
		return _instance;
	}

	private SkillsEngine()
	{
		//
	}

	public List<Skill> loadSkills(File file)
	{
		if(file == null)
		{
			_log.warn("SkillsEngine: File not found!");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}

	public IntObjectMap<SkillEntry> loadAllSkills()
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/stats/skills");
		if(!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return Containers.emptyIntObjectMap();
		}

		Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
		IntObjectMap<SkillEntry> result = new HashIntObjectMap<SkillEntry> ();
		int maxId = 0, maxLvl = 0;

		for(File file : files)
		{
			List<Skill> s = loadSkills(file);
			if(s == null)
				continue;
			for(Skill skill : s)
			{
				result.put(SkillTable.getSkillHashCode(skill), new SkillEntry(SkillEntryType.NONE, skill));
				if(skill.getId() > maxId)
					maxId = skill.getId();
				if(skill.getLevel() > maxLvl)
					maxLvl = skill.getLevel();
			}
		}

		_log.info("SkillsEngine: Loaded " + result.size() + " skill templates from XML files. Max id: " + maxId + ", max level: " + maxLvl);
		return result;
	}
}