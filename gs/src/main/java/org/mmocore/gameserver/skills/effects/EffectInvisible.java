package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;

public final class EffectInvisible extends Effect
{
	private SpecialEffectState _invisible = SpecialEffectState.FALSE;

	public EffectInvisible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	protected void checkAndDispelEffect(SkillEntry skill)
	{
		exit(); // Хайд слетает при любом действии
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		Player player = (Player) _effected;
		if(player.isInvisible())
			return false;
		if(player.getActiveWeaponFlagAttachment() != null)
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) _effected;

		_invisible = player.getInvisible();

		player.setInvisible(SpecialEffectState.TRUE);

		World.removeObjectFromPlayers(player);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = (Player) _effected;
		if(!player.isInvisible())
			return;

		player.setInvisible(_invisible);

		player.broadcastUserInfo(true);
		if(player.getServitor() != null)
			player.getServitor().broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}