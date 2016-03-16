package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Manor;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.MonsterInstance;
import org.mmocore.gameserver.network.l2.components.CustomMessage;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.utils.ItemFunctions;


public class Sowing extends Skill
{
	public Sowing(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		Player player = (Player) activeChar;
		int seedId = player.getUseSeed();
		boolean altSeed = ItemHolder.getInstance().getTemplate(seedId).isAltSeed();

		// remove seed from inventory
		if(!ItemFunctions.deleteItem(player, seedId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		for(Creature target : targets)
			if(target != null)
			{
				MonsterInstance monster = (MonsterInstance) target;
				if(monster.isSeeded())
					continue;

				// обработка
				double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;

				double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
				double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());

				// Штраф, на разницу уровней между мобом и игроком
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
					SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;

				// Штраф, на разницу уровней между семечкой и мобом
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET)
					SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;

				if(altSeed)
					SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;

				// Минимальный шанс успеха всегда 1%
				if(SuccessRate < 1)
					SuccessRate = 1;

				if(player.isGM())
					activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Sowing.Chance").addNumber((long) SuccessRate));

				if(Rnd.chance(SuccessRate) && monster.setSeeded(player, seedId, altSeed))
					activeChar.sendPacket(SystemMsg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
				else
					activeChar.sendPacket(SystemMsg.THE_SEED_WAS_NOT_SOWN);
			}
	}
}