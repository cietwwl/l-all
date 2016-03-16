package org.mmocore.gameserver.model.items.attachment;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.skills.SkillEntry;

/**
 * @author VISTALL
 * @date 15:49/26.03.2011
 */
public interface FlagItemAttachment extends PickableAttachment
{
	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onLogout(Player player);
	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onDeath(Player owner, Creature killer);

	boolean canAttack(Player player);

	boolean canCast(Player player, SkillEntry skillEntry);
}
