package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;

	/**
	 * packet type id 0x48
	 * format:		ch
	 */
	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isLockedTarget())
		{
			if(activeChar.isClanAirShipDriver())
				activeChar.sendPacket(SystemMsg.THIS_ACTION_IS_PROHIBITED_WHILE_STEERING);

			activeChar.sendActionFailed();
			return;
		}

		if(_unselect == 0)
		{
			if(activeChar.isCastingNow())
			{
				SkillEntry skill = activeChar.getCastingSkill();
				activeChar.abortCast(skill != null && (skill.getTemplate().isHandler() || skill.getTemplate().getHitTime() > 1000), false);
			}
			else if(activeChar.getTarget() != null)
				activeChar.setTarget(null);
		}
		else if(activeChar.getTarget() != null)
			activeChar.setTarget(null);
	}
}