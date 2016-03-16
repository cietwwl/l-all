package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.items.attachment.FlagItemAttachment;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		SkillEntry skill = activeChar.getKnownSkill(_magicId);
		if(skill != null)
		{
			if(skill.isDisabled())
				return;

			if(!(skill.getTemplate().isActive() || skill.getTemplate().isToggle()))
				return;

			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.sendActionFailed();
				return;
			}

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill))
				return;

			if(skill.getTemplate().isToggle())
				// TODO: DS: что это здесь вообще делает ?
				if (activeChar.isSitting() || activeChar.isFakeDeath())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
					activeChar.sendActionFailed();
					return;					
				}
				else if(activeChar.getEffectList().getEffectsBySkill(skill) != null)
				{
					activeChar.getListeners().onMagicUse(skill, activeChar, false);
					activeChar.getEffectList().stopEffect(skill.getId());
					activeChar.sendActionFailed();
					return;
				}

			Creature target = skill.getTemplate().getAimingTarget(activeChar, activeChar.getTarget());

			activeChar.setGroundSkillLoc(null);
			activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
			activeChar.sendActionFailed();
	}
}