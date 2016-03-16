package org.mmocore.gameserver.handler.admincommands.impl;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.handler.admincommands.IAdminCommandHandler;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.Earthquake;
import org.mmocore.gameserver.network.l2.s2c.SocialAction;
import org.mmocore.gameserver.skills.AbnormalEffect;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.Util;


public class AdminEffects implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_invis,
		admin_vis,
		admin_earthquake,
		admin_block,
		admin_unblock,
		admin_changename,
		admin_gmspeed,
		admin_invul,
		admin_setinvul,
		admin_getinvul,
		admin_social,
		admin_abnormal,
		admin_transform,
		admin_showmovie
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().GodMode)
			return false;

		int val;
		AbnormalEffect ae = AbnormalEffect.NULL;
		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_invis:
			case admin_vis:
				if(activeChar.getInvisible() == SpecialEffectState.GM)
				{
					activeChar.setInvisible(SpecialEffectState.FALSE);
					activeChar.broadcastCharInfo();
					if(activeChar.getServitor() != null)
						activeChar.getServitor().broadcastCharInfo();
				}
				else if(activeChar.getInvisible() == SpecialEffectState.FALSE)
				{
					activeChar.setInvisible(SpecialEffectState.GM);
					activeChar.sendUserInfo(true);
					World.removeObjectFromPlayers(activeChar);
					if(activeChar.getServitor() != null)
						World.removeObjectFromPlayers(activeChar.getServitor());
				}
				if(activeChar.getInvisible() == SpecialEffectState.GM)
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_hide", "true", -1);
				}
				else
					activeChar.unsetVar("gm_hide");
				break;
			case admin_gmspeed:
				if(wordList.length < 2)
					val = 0;
				else
					try
				{
						val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
					return false;
				}
				List<Effect> superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
				int sh_level = superhaste == null ? 0 : superhaste.isEmpty() ? 0 : superhaste.get(0).getSkill().getLevel();

				if(val == 0)
				{
					if(sh_level != 0)
						activeChar.doCast(SkillTable.getInstance().getSkillEntry(7029, sh_level), activeChar, true); //снимаем еффект
					activeChar.unsetVar("gm_gmspeed");
				}
				else if(val >= 1 && val <= 4)
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_gmspeed", String.valueOf(val), -1);
					if(val != sh_level)
					{
						if(sh_level != 0)
							activeChar.doCast(SkillTable.getInstance().getSkillEntry(7029, sh_level), activeChar, true); //снимаем еффект
						activeChar.doCast(SkillTable.getInstance().getSkillEntry(7029, val), activeChar, true);
					}
				}
				else
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
				break;
			case admin_invul:
				handleInvul(activeChar, activeChar);
				if(activeChar.isInvul())
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_invul", "true", -1);
				}
				else
					activeChar.unsetVar("gm_invul");
				break;
		}

		if(!activeChar.isGM())
			return false;

		switch(command)
		{
			case admin_earthquake:
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new Earthquake(activeChar.getLoc(), intensity, duration));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //earthquake intensity duration");
					return false;
				}
				break;
			case admin_block:
				if(wordList.length < 2)
				{
					if(target == null || !target.isCreature())
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						return false;
					}
					if(((Creature) target).isBlocked())
						return false;
					((Creature) target).abortAttack(true, false);
					((Creature) target).abortCast(true, false);
					((Creature) target).block();
					activeChar.sendMessage("Target blocked.");
					break;
				}
				try
				{
						val = Math.max(Integer.parseInt(wordList[1]), 100);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("USAGE: //block radius");
					return false;
				}
				int c1 = 0;
				for (Creature c : activeChar.getAroundCharacters(val, 300))
					if (!c.isBlocked())
					{
						c.abortAttack(true, false);
						c.abortCast(true, false);
						c.block();
						c1++;
					}
				activeChar.sendMessage("Blocked " + c1 +" characters in " + val + " radius");
				break;
			case admin_unblock:
				if(wordList.length < 2)
				{
					if(target == null || !target.isCreature())
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						return false;
					}
					if(!((Creature) target).isBlocked())
						return false;
					((Creature) target).unblock();
					activeChar.sendMessage("Target unblocked.");
				}
				try
				{
						val = Math.max(Integer.parseInt(wordList[1]), 100);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("USAGE: //unblock radius");
					return false;
				}
				int c2 = 0;
				for (Creature c : activeChar.getAroundCharacters(val, 300))
					if (c.isBlocked())
					{
						c.unblock();
						c2++;
					}
				activeChar.sendMessage("Unblocked " + c2 +" characters in " + val + " radius");
				break;
			case admin_changename:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //changename newName");
					return false;
				}
				if(target == null)
					target = activeChar;
				if(!target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String oldName = ((Creature) target).getName();
				String newName = Util.joinStrings(" ", wordList, 1);

				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();

				activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
				break;
			case admin_setinvul:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			case admin_getinvul:
				if(target != null && target.isCreature())
					activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (!((Creature) target).isInvul() ? "NOT " : "") + "invul");
				break;
			case admin_social:
				if(wordList.length < 2)
					val = Rnd.get(1, 7);
				else
					try
				{
						val = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("USAGE: //social value");
					return false;
				}
				if(target == null || target == activeChar)
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), val));
				else if(target.isCreature())
					((Creature) target).broadcastPacket(new SocialAction(target.getObjectId(), val));
				break;
			case admin_abnormal:
				try
				{
					if(wordList.length > 1)
						ae = AbnormalEffect.getByName(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //abnormal name");
					activeChar.sendMessage("//abnormal - Clears all abnormal effects");
					return false;
				}

				Creature effectTarget = target == null ? activeChar : (Creature) target;

				if(ae == AbnormalEffect.NULL)
				{
					effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
					effectTarget.sendMessage("Abnormal effects clearned by admin.");
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Abnormal effects clearned.");
				}
				else
				{
					effectTarget.startAbnormalEffect(ae);
					effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
				}
				break;
			case admin_transform:
				try
				{
					val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //transform transform_id");
					return false;
				}
				activeChar.setTransformation(val);
				break;
			case admin_showmovie:
        if(wordList.length < 2)
        {
            activeChar.sendMessage("USAGE: //showmovie id");
            return false;
        }
        int id;
        try
        {
            id = Integer.parseInt(wordList[1]);
        }
        catch(NumberFormatException e)
        {
            activeChar.sendMessage("You must specify id");
            return false;
        }
        activeChar.showQuestMovie(id);
        break;
		}

		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.getInvulnerable() == SpecialEffectState.GM)
		{
			target.setInvul(SpecialEffectState.FALSE);
			target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getServitor() != null)
			{
				target.getServitor().setInvul(SpecialEffectState.FALSE);
				target.getServitor().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now mortal.");
		}
		else if(target.getInvulnerable() != SpecialEffectState.TRUE)
		{
			target.setInvul(SpecialEffectState.GM);
			target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getServitor() != null)
			{
				target.getServitor().setInvul(SpecialEffectState.GM);
				target.getServitor().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now immortal.");
		}
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}