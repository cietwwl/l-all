package org.mmocore.gameserver.skills.effects;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.stats.Env;

public final class EffectDisarm extends Effect
{
	public EffectDisarm(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isPlayer())
			// Нельзя снимать/одевать проклятое оружие и флаги
			if(_effected.getPlayer().isCursedWeaponEquipped() || _effected.getPlayer().getActiveWeaponFlagAttachment() != null)
				return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		ItemInstance wpn = _effected.getActiveWeaponInstance();
		if(_effected.isPlayer())
		{
			Player player = (Player) _effected;
			if(wpn != null)
			{
				player.getInventory().unEquipItem(wpn);
				player.sendDisarmMessage(wpn);
			}
		}
		else if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setRHandId(0);
			npc.setLHandId(0);
		}
		_effected.startWeaponEquipBlocked();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopWeaponEquipBlocked();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setRHandId(npc.getTemplate().rhand);
			npc.setLHandId(npc.getTemplate().lhand);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}