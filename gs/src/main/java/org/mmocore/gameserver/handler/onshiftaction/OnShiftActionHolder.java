package org.mmocore.gameserver.handler.onshiftaction;

import java.util.HashMap;
import java.util.Map;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.MyTargetSelected;

/**
 * @author VISTALL
 * @date 2:38/19.08.2011
 */
public class OnShiftActionHolder extends AbstractHolder
{
	private static final OnShiftActionHolder _instance = new OnShiftActionHolder();
	private Map<Class<?>, OnShiftActionHandler<?>> _handlers = new HashMap<Class<?>, OnShiftActionHandler<?>>();

	public static OnShiftActionHolder getInstance()
	{
		return _instance;
	}

	public <T> void register(Class<T> clazz, OnShiftActionHandler<T> t)
	{
		_handlers.put(clazz, t);
	}

	@SuppressWarnings("unchecked")
	public <T extends GameObject> boolean callShiftAction(Player player, Class<T> clazz, T obj, boolean select)
	{
		OnShiftActionHandler<T> l = (OnShiftActionHandler<T>) _handlers.get(clazz);
		if(l == null)
			return false;

		if(select && player.getTarget() != obj)
		{
			player.setTarget(obj);
			player.sendPacket(new MyTargetSelected(obj.getObjectId(), 0));
		}

		boolean b = l.call(obj, player);
		player.sendPacket(ActionFail.STATIC);
		return b;
	}

	@Override
	public int size()
	{
		return _handlers.size();
	}

	@Override
	public void clear()
	{
		_handlers.clear();
	}
}
