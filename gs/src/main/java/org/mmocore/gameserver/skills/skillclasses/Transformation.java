package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;

public class Transformation extends Skill
{
	public final boolean useSummon;
	public final boolean isDisguise;
	public final String transformationName;

	public Transformation(StatsSet set)
	{
		super(set);
		useSummon = set.getBool("useSummon", false);
		isDisguise = set.getBool("isDisguise", false);
		transformationName = set.getString("transformationName", null);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = target.getPlayer();

		if(player == null || player.getActiveWeaponFlagAttachment() != null)
			return false;

		if(player.getTransformation() != 0 && getId() != SKILL_TRANSFORM_DISPEL)
		{
			// Для всех скилов кроме Transform Dispel
			activeChar.sendPacket(SystemMsg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}

		// Нельзя использовать летающую трансформу на территории Aden, или слишком высоко/низко, или при вызванном пете/саммоне, или в инстансе
		if((getId() == SKILL_FINAL_FLYING_FORM || getId() == SKILL_AURA_BIRD_FALCON || getId() == SKILL_AURA_BIRD_OWL) && (player.getX() > -166168 || player.getZ() <= 0 || player.getZ() >= 6000 || player.getServitor() != null || player.getReflection() != ReflectionManager.DEFAULT))
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_id, _level));
			return false;
		}

		// Нельзя отменять летающую трансформу слишком высоко над землей
		if(player.isInFlyingTransform() && getId() == SKILL_TRANSFORM_DISPEL && Math.abs(player.getZ() - player.getLoc().correctGeoZ().z) > 333)
		{
			activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_id, _level));
			return false;
		}

		if(player.isInWater())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			return false;
		}

		if(player.isRiding() || player.getMountType() == 2)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
			return false;
		}

		// Для трансформации у игрока не должно быть активировано умение Mystic Immunity.
		if(player.getEffectList().getEffectsBySkillId(Skill.SKILL_MYSTIC_IMMUNITY) != null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_WHILE_UNDER_THE_EFFECT_OF_A_SPECIAL_SKILL);
			return false;
		}

		if(player.isInBoat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_BOAT);
			return false;
		}

		if(useSummon)
		{
			if(player.getServitor() == null || !player.getServitor().isSummon() || player.getServitor().isDead())
			{
				activeChar.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
				return false;
			}
		}
		else if(player.getServitor() != null && player.getServitor().isPet() && getId() != SKILL_TRANSFORM_DISPEL && !isBaseTransformation())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITORPET);
			return false;
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(useSummon)
		{
			if(activeChar.getServitor() == null || !activeChar.getServitor().isSummon() || activeChar.getServitor().isDead())
			{
				activeChar.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
				return;
			}
			activeChar.getServitor().unSummon(false, false);
		}

		if(isSummonerTransformation() && activeChar.getServitor() != null && activeChar.getServitor().isSummon())
			activeChar.getServitor().unSummon(false, false);

		for(Creature target : targets)
			if(target != null && target.isPlayer())
				getEffects(skillEntry, activeChar, target, false, false);

		if(isSSPossible())
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
	}
}