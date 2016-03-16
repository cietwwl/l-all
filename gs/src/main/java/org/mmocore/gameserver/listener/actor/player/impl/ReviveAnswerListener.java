package org.mmocore.gameserver.listener.actor.player.impl;

import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.PetInstance;

/**
 * @author VISTALL
 * @date 11:35/15.04.2011
 */
public class ReviveAnswerListener implements OnAnswerListener
{

	private final double _power;
	private final boolean _forPet;
	private final long _timeStamp;

	public ReviveAnswerListener(double power, boolean forPet, int expiration)
	{
		_forPet = forPet;
		_power = power;
		_timeStamp = expiration > 0 ? System.currentTimeMillis() + expiration : Long.MAX_VALUE;
	}

	@Override
	public void sayYes(Player player)
	{
		if (System.currentTimeMillis() > _timeStamp)
			return;

		if(!player.isDead() && !_forPet || _forPet && player.getServitor() != null && !player.getServitor().isDead())
			return;

		if(!_forPet)
			player.doRevive(_power);
		else if(player.getServitor() != null)
			((PetInstance) player.getServitor()).doRevive(_power);
	}

	@Override
	public void sayNo(Player player)
	{

	}

	public double getPower()
	{
		return _power;
	}

	public boolean isForPet()
	{
		return _forPet;
	}
}
