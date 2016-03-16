package org.mmocore.gameserver.model.instances;

import java.util.Collections;
import java.util.List;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.handler.onshiftaction.OnShiftActionHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.reference.L2Reference;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;
import org.mmocore.gameserver.network.l2.s2c.ShowTownMap;
import org.mmocore.gameserver.network.l2.s2c.StaticObject;
import org.mmocore.gameserver.templates.StaticObjectTemplate;
import org.mmocore.gameserver.utils.Location;


public class StaticObjectInstance extends GameObject
{
	private final HardReference<StaticObjectInstance> reference;
	private final StaticObjectTemplate _template;
	private int _meshIndex;

	public StaticObjectInstance(int objectId, StaticObjectTemplate template)
	{
		super(objectId);

		_template = template;
		reference = new L2Reference<StaticObjectInstance>(this);
	}

	@Override
	public HardReference<StaticObjectInstance> getRef()
	{
		return reference;
	}

	public int getUId()
	{
		return _template.getUId();
	}

	public int getType()
	{
		return _template.getType();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, StaticObjectInstance.class, this, true))
			return;

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}

		MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
		player.sendPacket(my);

		if(!isInRange(player, 150))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(_template.getType() == 0) // Arena Board
			player.sendPacket(new HtmlMessage(getUId()).setFile("newspaper/arena.htm"));
		else if(_template.getType() == 2) // Village map
		{
			player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
			player.sendActionFailed();
		}
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.<L2GameServerPacket>singletonList(new StaticObject(this));
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	public void broadcastInfo(boolean force)
	{
		StaticObject p = new StaticObject(this);
		for(Player player : World.getAroundObservers(this))
			player.sendPacket(p);
	}

	@Override
	public int getGeoZ(Location loc)   //FIXME [VISTALL] нужно ли?
	{
		return loc.z;
	}

	public int getMeshIndex()
	{
		return _meshIndex;
	}

	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
	}
}