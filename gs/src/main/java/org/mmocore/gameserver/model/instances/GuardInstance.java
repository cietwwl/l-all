package org.mmocore.gameserver.model.instances;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.SpecialEffectState;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.npc.NpcTemplate;

public class GuardInstance extends NpcInstance
{
	public GuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setUndying(SpecialEffectState.FALSE);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() && ((MonsterInstance)attacker).isAggressive() || attacker.isPlayable() && attacker.getKarma() > 0;
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "guard/" + pom + ".htm";
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, SkillEntry skill, boolean awake, boolean standUp, boolean directHp)
	{
		getAggroList().addDamageHate(attacker, (int)damage, 0);

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
}