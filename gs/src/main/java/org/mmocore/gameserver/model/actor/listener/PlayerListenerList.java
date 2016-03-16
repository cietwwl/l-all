package org.mmocore.gameserver.model.actor.listener;

import org.mmocore.commons.listener.Listener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerEnterListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerExitListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerSayListener;
import org.mmocore.gameserver.listener.actor.player.OnPlayerSummonServitorListener;
import org.mmocore.gameserver.listener.actor.player.OnTeleportListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * @author G1ta0
 */
public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}

	public void onExit()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
	}

	public void onPartyInvite()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onPartyLeave()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}

	public void onSummonServitor(Servitor servitor)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);
	}

	public void onSay(ChatType type, String target, String text)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerSayListener.class.isInstance(listener))
					((OnPlayerSayListener) listener).onSay(getActor(), type, target, text);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerSayListener.class.isInstance(listener))
					((OnPlayerSayListener) listener).onSay(getActor(), type, target, text);
	}
}
