package org.mmocore.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.instances.MonsterInstance;
import org.mmocore.gameserver.model.pledge.Clan;

public class Die extends L2GameServerPacket
{
	private int _objectId;
	private boolean _fake;
	private boolean _sweepable;

	private Map<RestartType, Boolean> _types = new HashMap<RestartType, Boolean>(RestartType.VALUES.length);

	public Die(Creature cha)
	{
		_objectId = cha.getObjectId();
		_fake = !cha.isDead();

		if(cha.isMonster())
			_sweepable = ((MonsterInstance) cha).isSweepActive();
		else if(cha.isPlayer() && !((Player)cha).isInOlympiadMode())
		{
			Player player = (Player) cha;
			put(RestartType.FIXED, player.getPlayerAccess().ResurectFixed || (!(Config.ALT_DISABLE_FEATHER_ON_SIEGES_AND_EPIC && (player.isOnSiegeField() || player.isInZone(ZoneType.epic))) && ((player.getInventory().getCountOf(10649) > 0 && !player.getInventory().isLockedItem(10649)) || (player.getInventory().getCountOf(13300) > 0 && !player.getInventory().isLockedItem(13300)))));
			put(RestartType.AGATHION, player.isAgathionResAvailable());
			put(RestartType.TO_VILLAGE, true);

			Clan clan = player.getClan();
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
				put(RestartType.TO_CASTLE, clan.getCastle() > 0);
				put(RestartType.TO_FORTRESS, clan.getHasFortress() > 0);
			}

			for(Event e : cha.getEvents())
				e.checkRestartLocs(player, _types);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(_fake)
			return;

		writeC(0x00);
		writeD(_objectId);
		writeD(get(RestartType.TO_VILLAGE)); // to nearest village
		writeD(get(RestartType.TO_CLANHALL)); // to hide away
		writeD(get(RestartType.TO_CASTLE)); // to castle
		writeD(get(RestartType.TO_FLAG));// to siege HQ
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(get(RestartType.FIXED));// FIXED
		writeD(get(RestartType.TO_FORTRESS));// fortress
		writeC(0); //show die animation
		writeD(get(RestartType.AGATHION));//agathion ress button
		writeD(0x00); //additional free space
	}

	private void put(RestartType t, boolean b)
	{
		_types.put(t, b);
	}

	private boolean get(RestartType t)
	{
		Boolean b = _types.get(t);
		return b != null && b;
	}
}