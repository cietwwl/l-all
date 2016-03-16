package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.PetData;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.instances.PetInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SetupGauge;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.templates.StatsSet;

public class FeedPet extends Skill
{
	private final int _feedNormal;
	private final int _feedMounted;
	private final int _feedFlyMounted;

	public FeedPet(StatsSet set)
	{
		super(set);
		final int feedValues[] = set.getIntegerArray("feedValues", null);
		if (feedValues == null || feedValues.length != 3)
			throw new Error("Invalid feed value in skill " + this);

		_feedNormal = feedValues[0];
		_feedMounted = feedValues[1];
		_feedFlyMounted = feedValues[2];
	}

	@Override
	public final boolean checkCondition(SkillEntry skillEntry, final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if (!activeChar.isPlayable())
			return false;
		if (activeChar.isPlayer() && !((Player)activeChar).isMounted())
		{
			if (getItemConsumeId().length > 0)
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(getItemConsumeId()[0]));
			return false;
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public final void useSkill(SkillEntry skillEntry, Creature caster, List<Creature> targets)
	{
		if (!caster.isPlayable())
			return;

		for (Creature target : targets)
		{
			if (target.isPet())
			{
				final PetInstance pet = (PetInstance)target;
				final int maxFed = pet.getMaxFed();
				final int currentFed = pet.getCurrentFed();
				int feed = _feedNormal;
				feed = Math.max(feed * maxFed * pet.getAddFed() / 100, 1);
				feed = Math.min(feed + currentFed, maxFed);
				if (feed != currentFed)
				{
					pet.setCurrentFed(feed);
					pet.sendStatusUpdate();
					if (currentFed * 10 < maxFed && feed * 10 > maxFed) // было меньше 10%, стало больше 10%
						if (feed * 100 <= maxFed * 55) // но меньше или равно 55%
							pet.getPlayer().sendPacket(SystemMsg.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
				}
			}
			else if (target.isPlayer())
			{
				final Player player = (Player)target;
				if (player.isMounted() && player.getMountCurrentFed() >= 0)
				{
					final PetData info = PetDataTable.getInstance().getInfo(player.getMountNpcId(), player.getMountLevel());
					if (info == null)
						continue;
					final int maxFed = player.getMountMaxFed();
					int feed = player.isFlying() ? _feedFlyMounted : _feedMounted;
					feed = Math.max(feed * maxFed * info.getAddFed() / 100, 1);
					feed = Math.min(feed + player.getMountCurrentFed(), maxFed);
					if (feed != player.getMountCurrentFed())
					{
						player.setMountCurrentFed(feed);
						player.sendPacket(new SetupGauge(player, SetupGauge.GREEN, feed * 10000, maxFed * 10000));
					}
				}
			}
		}
	}
}
