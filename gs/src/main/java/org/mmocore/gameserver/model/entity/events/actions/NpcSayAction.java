package org.mmocore.gameserver.model.entity.events.actions;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.entity.events.Event;
import org.mmocore.gameserver.model.entity.events.EventAction;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.ChatType;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.network.l2.s2c.NpcSay;

/**
 * @author VISTALL
 * @date  21:44/10.12.2010
 */
public class NpcSayAction implements EventAction
{
	private int _npcId;
	private int _range;
	private ChatType _chatType;
	private NpcString _text;

	public NpcSayAction(int npcId, int range, ChatType type, NpcString string)
	{
		_npcId = npcId;
		_range = range;
		_chatType = type;
		_text = string;
	}

	@Override
	public void call(Event event)
	{
		NpcInstance npc = event.getNpcByNpcId(_npcId);
		if(npc == null)
			return;

		for(Player player : World.getAroundObservers(npc))
			if(_range <= 0 || player.isInRangeZ(npc, _range))
				packet(npc, player);
	}

	private void packet(NpcInstance npc, Player player)
	{
		player.sendPacket(new NpcSay(npc, _chatType, _text));
	}
}
