package org.mmocore.gameserver.ai;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.RaceManagerInstance;
import org.mmocore.gameserver.network.l2.s2c.RaceMonsterStatusUpdate;


public class RaceManager extends DefaultAI
{
	private boolean thinking = false; // to prevent recursive thinking
	private List<Player> _knownPlayers = new ArrayList<Player>();

	public RaceManager(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 5000;
	}

	@Override
	public void runImpl()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtThink()
	{
		RaceManagerInstance actor = getActor();
		if(actor == null)
			return;

		RaceMonsterStatusUpdate packet = actor.getPacket();
		if(packet == null)
			return;

		synchronized (this)
		{
			if(thinking)
				return;
			thinking = true;
		}

		try
		{
			List<Player> newPlayers = new ArrayList<Player>();
			for(Player player : World.getAroundObservers(actor))
			{
				newPlayers.add(player);
				if(!_knownPlayers.contains(player))
					player.sendPacket(packet);
				_knownPlayers.remove(player);
			}

			for(Player player : _knownPlayers)
				actor.removeKnownPlayer(player);

			_knownPlayers = newPlayers;
		}
		finally
		{
			// Stop thinking action
			thinking = false;
		}
	}

	@Override
	public RaceManagerInstance getActor()
	{
		return (RaceManagerInstance) super.getActor();
	}
}