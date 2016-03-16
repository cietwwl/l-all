package org.mmocore.gameserver.model.instances;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.reference.L2Reference;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;

/**
 * @author VISTALL
 * @date 20:20/03.01.2011
 */
public class ControlKeyInstance extends GameObject
{
	protected HardReference<ControlKeyInstance> reference;

	public ControlKeyInstance()
	{
		super(IdFactory.getInstance().getNextId());
		reference = new L2Reference<ControlKeyInstance>(this);
	}

	@Override
	public HardReference<ControlKeyInstance> getRef()
	{
		return reference;
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}

		player.sendActionFailed();
	}
}
