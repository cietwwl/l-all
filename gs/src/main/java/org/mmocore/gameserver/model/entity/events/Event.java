package org.mmocore.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.mmocore.commons.collections.MultiValueSet;
import org.mmocore.commons.listener.Listener;
import org.mmocore.commons.listener.ListenerList;
import org.mmocore.commons.logging.LoggerObject;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.dao.ItemsDAO;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.listener.event.OnStartStopListener;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.base.RestartType;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.entity.events.objects.DoorObject;
import org.mmocore.gameserver.model.entity.events.objects.InitableObject;
import org.mmocore.gameserver.model.entity.events.objects.SpawnableObject;
import org.mmocore.gameserver.model.entity.events.objects.ZoneObject;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.TimeUtils;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;


/**
 * @author VISTALL
 * @date 12:54/10.12.2010
 */
public abstract class Event extends LoggerObject
{
	private class ListenerListImpl extends ListenerList<Event>
	{
		public void onStart()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStart(Event.this);
		}

		public void onStop()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStop(Event.this);
		}
	}

	public static final String EVENT = "event";

	// actions
	protected final IntObjectMap<List<EventAction>> _onTimeActions = new TreeIntObjectMap<List<EventAction>>();
	protected final List<EventAction> _onStartActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onStopActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onInitActions = new ArrayList<EventAction>(0);
	// objects
	protected final Map<Object, List<Object>> _objects = new HashMap<Object, List<Object>>(0);

	protected final int _id;
	protected final String _name;

	protected final ListenerListImpl _listenerList = new ListenerListImpl();

	protected IntObjectMap<ItemInstance> _banishedItems = Containers.emptyIntObjectMap();

	private List<Future<?>> _tasks = null;

	protected Event(MultiValueSet<String> set)
	{
		this(set.getInteger("id"), set.getString("name"));
	}

	protected Event(int id, String name)
	{
		_id = id;
		_name = name;
	}

	public void initEvent()
	{
		callActions(_onInitActions);

		reCalcNextTime(true);

		printInfo();
	}

	public void startEvent()
	{
		callActions(_onStartActions);

		_listenerList.onStart();
	}

	public void stopEvent()
	{
		callActions(_onStopActions);

		_listenerList.onStop();
	}

	public void printInfo()
	{
		final long startSiegeMillis = startTimeMillis();

		if(startSiegeMillis == 0)
			info(getName() + " time - undefined");
		else
			info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
	}
	//===============================================================================================================
	//												Actions
	//===============================================================================================================

	protected void callActions(List<EventAction> actions)
	{
		for(EventAction action : actions)
			action.call(this);
	}

	public void addOnStartActions(List<EventAction> start)
	{
		_onStartActions.addAll(start);
	}

	public void addOnStopActions(List<EventAction> start)
	{
		_onStopActions.addAll(start);
	}

	public void addOnInitActions(List<EventAction> start)
	{
		_onInitActions.addAll(start);
	}

	public void addOnTimeAction(int time, EventAction action)
	{
		List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.add(action);
		else
		{
			List<EventAction> actions = new ArrayList<EventAction>(1);
			actions.add(action);
			_onTimeActions.put(time, actions);
		}
	}

	public void addOnTimeActions(int time, List<EventAction> actions)
	{
		if(actions.isEmpty())
			return;

		List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.addAll(actions);
		else
			_onTimeActions.put(time, new ArrayList<EventAction>(actions));
	}

	public void timeActions(int time)
	{
		List<EventAction> actions = _onTimeActions.get(time);
		if(actions == null)
		{
			info("Undefined time : " + time);
			return;
		}

		callActions(actions);
	}

	public int[] timeActions()
	{
		return _onTimeActions.keySet().toArray();
	}

	//===============================================================================================================
	//												Tasks
	//===============================================================================================================

	public synchronized void registerActions()
	{
		final long t = startTimeMillis();
		if(t == 0)
			return;

		if(_tasks == null)
			_tasks = new ArrayList<Future<?>>(_onTimeActions.size());

		final long c = System.currentTimeMillis();
		for(int key : _onTimeActions.keySet().toArray())
		{
			long time = t + key * 1000L;
			EventTimeTask wrapper = new EventTimeTask(this, key);
			if(time <= c)
				ThreadPoolManager.getInstance().execute(wrapper);
			else
				_tasks.add(ThreadPoolManager.getInstance().schedule(wrapper, time - c));
		}
	}

	public synchronized void clearActions()
	{
		if(_tasks == null)
			return;

		for(Future<?> f : _tasks)
			f.cancel(false);

		_tasks.clear();
	}

	//===============================================================================================================
	//												Objects
	//===============================================================================================================

	@SuppressWarnings("unchecked")
	public <O> List<O> getObjects(Object name)
	{
		List<Object> objects = _objects.get(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	@SuppressWarnings("unchecked")
	public <O> O getFirstObject(Object name)
	{
		List<Object> objects = getObjects(name);
		return objects.size() > 0 ? (O) objects.get(0) : null;
	}

	public void addObject(Object name, Object object)
	{
		if(object == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.add(object);
		else
		{
			list = new CopyOnWriteArrayList<Object>();
			list.add(object);
			_objects.put(name, list);
		}
	}

	public void removeObject(Object name, Object o)
	{
		if(o == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.remove(o);
	}

	@SuppressWarnings("unchecked")
	public <O> List<O> removeObjects(Object name)
	{
		List<Object> objects = _objects.remove(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	public void addObjects(Object name, List<?> objects)
	{
		if(objects.isEmpty())
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.addAll(objects);
		else
			_objects.put(name, new CopyOnWriteArrayList<Object>(objects));
	}


	public Map<Object, List<Object>> getObjects()
	{
		return _objects;
	}

	public void spawnAction(Object name, boolean spawn)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
			{
				if(spawn)
					((SpawnableObject) object).spawnObject(this);
				else
					((SpawnableObject) object).despawnObject(this);
			}
	}

	public void respawnAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).respawnObject(this);
	}

	public void doorAction(Object name, boolean open)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof DoorObject)
			{
				if(open)
					((DoorObject) object).open(this);
				else
					((DoorObject) object).close(this);
			}
	}

	public void zoneAction(Object name, boolean active)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof ZoneObject)
				((ZoneObject) object).setActive(active, this);
	}

	public void initAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof InitableObject)
				((InitableObject) object).initObject(this);
	}

	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(EVENT))
		{
			if(start)
				startEvent();
			else
				stopEvent();
		}
	}

	public void refreshAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).refreshObject(this);
	}
	//===============================================================================================================
	//												Abstracts
	//===============================================================================================================

	public abstract void reCalcNextTime(boolean onInit);

	public abstract EventType getType();

	protected abstract long startTimeMillis();

	//===============================================================================================================
	//												Broadcast
	//===============================================================================================================
	public void broadcastToWorld(IBroadcastPacket packet)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			if(player != null)
				player.sendPacket(packet);
	}

	public void broadcastToWorld(L2GameServerPacket packet)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			if(player != null)
				player.sendPacket(packet);
	}
	//===============================================================================================================
	//												Getters & Setters
	//===============================================================================================================
	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public GameObject getCenterObject()
	{
		return null;
	}

	public Reflection getReflection()
	{
		return ReflectionManager.DEFAULT;
	}

	public int getRelation(Player thisPlayer, Player target, int oldRelation)
	{
		return oldRelation;
	}

	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		return oldRelation;
	}

	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		//
	}

	public Location getRestartLoc(Player player, RestartType type)
	{
		return null;
	}

	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
	{
		return false;
	}

	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return null;
	}

	public SystemMsg canUseItem(Player player, ItemInstance item)
	{
		return null;
	}

	public boolean isInProgress()
	{
		return false;
	}

	public void findEvent(Player player)
	{
		//
	}

	public void announce(int a)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented announce");
	}

	public void teleportPlayers(String teleportWho)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented teleportPlayers");
	}

	public boolean ifVar(String name)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented ifVar");
	}

	public List<Player> itemObtainPlayers()
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented itemObtainPlayers");
	}

	public void giveItem(Player player, int itemId, long count)
	{
		switch(itemId)
		{
			case ItemTemplate.ITEM_ID_FAME:
				player.setFame(player.getFame() + (int)count, toString());
				break;
			default:
				ItemFunctions.addItem(player, itemId, count);
				break;
		}
	}

	public List<Player> broadcastPlayers(int range)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented broadcastPlayers");
	}

	public NpcInstance getNpcByNpcId(int npcId)
	{
		return GameObjectsStorage.getByNpcId(npcId);
	}
	/**
	 * @param active   кто ресает
	 * @param target   кого ресает
	 * @param force    ктрл зажат ли
	 * @param quiet  если тру, мессаги об ошибке непосылаются
	 * @return  возращает можно ли реснуть цень
	 */
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented canResurrect");
	}

	//===============================================================================================================
	//											setEvent helper
	//===============================================================================================================
	public void onAddEvent(GameObject o)
	{
		//
	}

	public void onRemoveEvent(GameObject o)
	{
		//
	}
	//===============================================================================================================
	//											Banish items
	//===============================================================================================================
	public void addBanishItem(ItemInstance item)
	{
		if(_banishedItems == Containers.<ItemInstance>emptyIntObjectMap())
			_banishedItems = new CHashIntObjectMap<ItemInstance>();

		_banishedItems.put(item.getObjectId(), item);
	}

	public void removeBanishItems()
	{
		Iterator<IntObjectPair<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
		while(iterator.hasNext())
		{
			IntObjectPair<ItemInstance> entry = iterator.next();
			iterator.remove();

			ItemInstance item = ItemsDAO.getInstance().load(entry.getKey());
			if(item != null)
			{
				if(item.getOwnerId() > 0)
				{
					GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
					if(object != null && object.isPlayable())
					{
						((Playable)object).getInventory().destroyItem(item);
						object.getPlayer().sendPacket(SystemMessage.removeItems(item));
					}
				}
				item.delete();
			}
			else
				item = entry.getValue();

			item.deleteMe();
		}
	}
	//===============================================================================================================
	//											 Listeners
	//===============================================================================================================
	public void addListener(Listener<Event> l)
	{
		_listenerList.add(l);
	}

	public void removeListener(Listener<Event> l)
	{
		_listenerList.remove(l);
	}
	//===============================================================================================================
	//											Object
	//===============================================================================================================
	public void cloneTo(Event e)
	{
		for(EventAction a : _onInitActions)
			e._onInitActions.add(a);

		for(EventAction a : _onStartActions)
			e._onStartActions.add(a);

		for(EventAction a : _onStopActions)
			e._onStopActions.add(a);

		for(IntObjectPair<List<EventAction>> entry : _onTimeActions.entrySet())
			e.addOnTimeActions(entry.getKey(), entry.getValue());
	}
}
