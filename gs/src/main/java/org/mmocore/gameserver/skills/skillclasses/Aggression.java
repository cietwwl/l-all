package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;

public class Aggression extends Skill
{
	private final boolean _unaggring;
	private final boolean _silent;

	public Aggression(StatsSet set)
	{
		super(set);
		_unaggring = set.getBool("unaggroing", false);
		_silent = set.getBool("silent", false);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		int effect = _effectPoint;

		if(isSSPossible() && (activeChar.getChargedSoulShot() || activeChar.getChargedSpiritShot(false) > 0))
			effect *= 2;

		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isAutoAttackable(activeChar))
					continue;
				if(target.isNpc())
					if(_unaggring)
					{
						if(target.isNpc() && activeChar.isPlayable())
							((NpcInstance) target).getAggroList().addDamageHate(activeChar, 0, -effect);
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);
						if(!_silent)
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, skillEntry, 0);
					}
				else if(target.isPlayable() && !target.isDebuffImmune())
					target.setTarget(activeChar);
				getEffects(skillEntry, activeChar, target, true, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}