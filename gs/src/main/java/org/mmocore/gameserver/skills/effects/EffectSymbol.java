package org.mmocore.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.SymbolInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillLaunched;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EffectSymbol extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(EffectSymbol.class);

	private NpcInstance _symbol = null;

	public EffectSymbol(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTemplate().getTargetType() != Skill.SkillTargetType.TARGET_SELF)
		{
			_log.error("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}

		SkillEntry skill = getSkill().getTemplate().getFirstAddedSkill();
		if(skill == null)
		{
			_log.error("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		SkillEntry skill = getSkill().getTemplate().getFirstAddedSkill();

		// Затычка, в клиенте они почему-то не совпадают.
		skill.getTemplate().setMagicType(getSkill().getTemplate().getMagicType());

		Location loc = _effected.getLoc();
		if(_effected.isPlayer() && ((Player) _effected).getGroundSkillLoc() != null)
		{
			loc = ((Player) _effected).getGroundSkillLoc();
			((Player) _effected).setGroundSkillLoc(null);
		}

		NpcTemplate template = NpcHolder.getInstance().getTemplate(_skill.getTemplate().getSymbolId());
		if(getTemplate()._count <= 1) // дебафающие купола, процедура каста в SymbolInstance
			_symbol = new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill);
		else // атакующие купола, процедура каста здесь
			_symbol = new NpcInstance(IdFactory.getInstance().getNextId(), template);
		
		_symbol.setLevel(_effected.getLevel());
		_symbol.setReflection(_effected.getReflection());
		_symbol.spawnMe(loc);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		if (_symbol != null && _symbol.isVisible())
			_symbol.deleteMe();

		_symbol = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate()._count <= 1)
			return false;

		Creature effector = getEffector();
		SkillEntry skill = getSkill().getTemplate().getFirstAddedSkill();
		NpcInstance symbol = _symbol;
		double mpConsume = getSkill().getTemplate().getMpConsume();

		if(effector == null || skill == null || symbol == null)
			return false;

		if(mpConsume > effector.getCurrentMp())
		{
			effector.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			return false;
		}

		effector.reduceCurrentMp(mpConsume, effector);

		// Использовать разрешено только скиллы типа TARGET_ONE
		for(Creature cha : World.getAroundCharacters(symbol, getSkill().getTemplate().getSkillRadius(), 200))
			if(!cha.isDoor() && cha.getEffectList().getEffectsBySkill(skill) == null && skill.checkTarget(effector, cha, null, false, false) == null)
			{
				if(skill.getTemplate().isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false))
					continue;
				List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(cha);
				effector.callSkill(skill, targets, true);
				effector.broadcastPacket(new MagicSkillLaunched(symbol, getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha));
			}

		return true;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}
}