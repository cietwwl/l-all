package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill.SkillType;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.EffectType;

public class RequestDispel extends L2GameClientPacket
{
	private int _objectId, _id, _level;

	@Override
	protected void readImpl() throws Exception
	{
		_objectId = readD();
		_id = readD();
		_level = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		boolean showMsg = true;
		Creature target = activeChar;
		if (target.getObjectId() != _objectId)
		{
			showMsg = false;
			target = activeChar.getServitor();
			if (target == null || target.getObjectId() != _objectId)
				return;
		}

		for(Effect e : target.getEffectList().getAllEffects())
			if(e.getDisplayId() == _id && e.getDisplayLevel() == _level)
				if(!e.isOffensive() && !e.getSkill().getTemplate().isMusic() && e.getSkill().getTemplate().isSelfDispellable() && e.getSkill().getSkillType() != SkillType.TRANSFORMATION && e.getTemplate().getEffectType() != EffectType.Hourglass)
				{
					e.exit(false);
					if (showMsg)
						activeChar.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
				}
				else
					return;
	}
}