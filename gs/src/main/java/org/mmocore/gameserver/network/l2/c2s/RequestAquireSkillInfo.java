package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.data.xml.holder.SkillAcquireHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.SkillLearn;
import org.mmocore.gameserver.model.base.AcquireType;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.s2c.AcquireSkillInfo;
import org.mmocore.gameserver.tables.SkillTable;

/**
 * Reworked: VISTALL
 */
public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || SkillTable.getInstance().getSkillEntry(_id, _level) == null || _type == null)
			return;

		NpcInstance trainer = player.getLastNpc();
		if((trainer == null || (!player.isInRangeZ(trainer, Creature.INTERACTION_DISTANCE)) && !player.isGM()))
			return;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);
		if(skillLearn == null)
			return;

		sendPacket(new AcquireSkillInfo(_type, skillLearn));
	}
}