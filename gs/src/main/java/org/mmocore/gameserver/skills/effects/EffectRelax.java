package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.stats.Env;

public class EffectRelax extends Effect
{
	private boolean _isWereSitting;

	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		Player player = _effected.getPlayer();
		if(player == null)
			return false;
		if(player.isMounted())
		{
			player.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_skill.getId(), _skill.getLevel()));
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player.isMoving)
			player.stopMove();
		_isWereSitting = player.isSitting();
		player.sitDown(null);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if (!_isWereSitting)
			_effected.getPlayer().standUp();
	}

	@Override
	public boolean onActionTime()
	{
		Player player = _effected.getPlayer();
		if(player.isAlikeDead())
			return false;

		if(!player.isSitting())
			return false;

		if(player.isCurrentHpFull() && getSkill().getTemplate().isToggle())
		{
			getEffected().sendPacket(SystemMsg.THAT_SKILL_HAS_BEEN_DEACTIVATED_AS_HP_WAS_FULLY_RECOVERED);
			return false;
		}

		double manaDam = calc();
		if(manaDam > _effected.getCurrentMp())
			if(getSkill().getTemplate().isToggle())
			{
				player.sendPacket(SystemMsg.NOT_ENOUGH_MP, new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
				return false;
			}

		_effected.reduceCurrentMp(manaDam, null);

		return true;
	}
}