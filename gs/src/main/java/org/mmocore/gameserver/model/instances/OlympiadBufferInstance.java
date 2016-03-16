package org.mmocore.gameserver.model.instances;

import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillUse;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.npc.NpcTemplate;


public class OlympiadBufferInstance extends NpcInstance
{
	private TIntHashSet _buffs = new TIntHashSet();

	public OlympiadBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		_buffs.clear();
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(_buffs.size() < 5 && command.startsWith("Buff"))
		{
			int id = 0;
			int lvl = 0;
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			id = Integer.parseInt(st.nextToken());
			lvl = Integer.parseInt(st.nextToken());
			SkillEntry skill = SkillTable.getInstance().getSkillEntry(id, lvl);
			List<Creature> target = new ArrayList<Creature>();
			target.add(player);
			broadcastPacket(new MagicSkillUse(this, player, id, lvl, 0, 0));
			callSkill(skill, target, true);
			_buffs.add(id);
		}
		showChatWindow(player, 0);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		final int size = _buffs.size();
		if (size == 0)
			return "olympiad/olympiad_master001.htm";
		if ( size < 5)
			return "olympiad/olympiad_master002.htm";

		return "olympiad/olympiad_master003.htm";
	}
}