package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;

/**
 * Format:   dddddddddh [h] h [ddd]
 * Пример пакета:
 * 48
 * 86 99 00 4F  86 99 00 4F
 * EF 08 00 00  01 00 00 00
 * 00 00 00 00  00 00 00 00
 * F9 B5 FF FF  7D E0 01 00  68 F3 FF FF
 * 00 00 00 00
 */
public class MagicSkillUse extends L2GameServerPacket
{
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _casterId;
	private final int _casterX;
	private final int _casterY;
	private final int _casterZ;
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;

	public MagicSkillUse(Creature caster, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_casterId = caster.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int)reuseDelay;
		_casterX = caster.getX();
		_casterY = caster.getY();
		_casterZ = caster.getZ();
		_targetX = target.getX();
		_targetY = target.getY();
		_targetZ = target.getZ();
	}

	public MagicSkillUse(Creature caster, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_casterId = caster.getObjectId();
		_targetId = caster.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int)reuseDelay;
		_casterX = caster.getX();
		_casterY = caster.getY();
		_casterZ = caster.getZ();
		_targetX = caster.getX();
		_targetY = caster.getY();
		_targetZ = caster.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_casterId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_casterX);
		writeD(_casterY);
		writeD(_casterZ);
		writeD(0x00); // unknown
		writeD(_targetX);
		writeD(_targetY);
		writeD(_targetZ);
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		if (player != null)
		{
			if (player.buffAnimRange() < 0)
				return null;
			if (player.buffAnimRange() == 0)
				return _casterId == player.getObjectId() ? super.packet(player) : null;

			Creature observer = player.getObservePoint();
			if (observer == null)
				observer = player;

			return observer.getDistance(_casterX, _casterY) < player.buffAnimRange() ? super.packet(player) : null;
		}

		return super.packet(player); 
	}
}