package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.utils.Location;

/**
 * @author SYS
 * @date 08/9/2007
 * Format: chdddddc
 *
 * Пример пакета:
 * D0
 * 2F 00
 * E4 35 00 00 x
 * 62 D1 02 00 y
 * 22 F2 FF FF z
 * 90 05 00 00 skill id
 * 00 00 00 00 ctrlPressed
 * 00 shiftPressed
 */
public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0xd0
	 */
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		SkillEntry skillEntry = activeChar.getKnownSkill(_skillId);
		if(skillEntry != null)
		{
			if(skillEntry.isDisabled())
				return;

			Skill skill = skillEntry.getTemplate();
			if(skill.getAddedSkills().length == 0)
				return;

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skillEntry))
				return;

			if(!activeChar.isInRange(_loc, skill.getCastRange()))
			{
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
				activeChar.sendActionFailed();
				return;
			}

			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			if(skillEntry.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
			{
				activeChar.setGroundSkillLoc(_loc);
				activeChar.getAI().Cast(skillEntry, target, _ctrlPressed, _shiftPressed);
			}
			else
				activeChar.sendActionFailed();
		}
		else
			activeChar.sendActionFailed();
	}
}