package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.ChestInstance;
import org.mmocore.gameserver.model.instances.DoorInstance;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;


public class Unlock extends Skill
{
	private final int _unlockPower;

	public Unlock(StatsSet set)
	{
		super(set);
		_unlockPower = set.getInteger("unlockPower", 0) + 100;
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target instanceof ChestInstance && target.isDead())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		if(target instanceof ChestInstance && activeChar.isPlayer())
			return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);

		if(!target.isDoor() || _unlockPower == 0)
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		DoorInstance door = (DoorInstance) target;

		if(door.isOpen())
		{
			activeChar.sendPacket(SystemMsg.IT_IS_NOT_LOCKED);
			return false;
		}

		if(!door.isUnlockable())
		{
			activeChar.sendPacket(SystemMsg.THIS_DOOR_CANNOT_BE_UNLOCKED);
			return false;
		}

		if(door.getKey() > 0) // ключ не подходит к двери
		{
			activeChar.sendPacket(SystemMsg.THIS_DOOR_CANNOT_BE_UNLOCKED);
			return false;
		}

		if(_unlockPower - door.getLevel() * 100 < 0) // Дверь слишком высокого уровня
		{
			activeChar.sendPacket(SystemMsg.THIS_DOOR_CANNOT_BE_UNLOCKED);
			return false;
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		for(Creature targ : targets)
			if(targ != null)
			{
				if(targ.isDoor())
				{
					DoorInstance target = (DoorInstance) targ;
					if(!target.isOpen() && (target.getKey() > 0 || Rnd.chance(_unlockPower - target.getLevel() * 100)))
					{
						target.openMe((Player)activeChar, true);
					}
					else
						activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR);
				}
				else if(targ instanceof ChestInstance)
				{
					ChestInstance target = (ChestInstance) targ;
					if(!target.isDead())
						target.tryOpen((Player) activeChar, this);
				}
			}
	}
}