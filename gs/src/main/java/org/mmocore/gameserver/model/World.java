package org.mmocore.gameserver.model;

import java.util.Collections;
import java.util.List;

import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.Zone.ZoneType;
import org.mmocore.gameserver.model.entity.Reflection;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Переработанный класс мира
 *
 * @author G1ta0
 */
public class World
{
	private static final Logger _log = LoggerFactory.getLogger(World.class);

	/** Map dimensions */
	public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
	public static final int MAP_MAX_X = (Config.GEO_X_LAST - 20 + 1 << 15) - 1;
	public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 18 + 1 << 15) - 1;
	public static final int MAP_MIN_Z = Config.MAP_MIN_Z;
	public static final int MAP_MAX_Z = Config.MAP_MAX_Z;

	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;

	public static final int SHIFT_BY = Config.SHIFT_BY;
	public static final int SHIFT_BY_Z = Config.SHIFT_BY_Z;

	/** calculated offset used so top left region is 0,0 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);

	/** Размерность массива регионов */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;

	private static volatile WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];

	public static void init()
	{
		_log.info("L2World: Creating regions: [" + (REGIONS_X + 1) + "][" + (REGIONS_Y + 1) + "][" + (REGIONS_Z + 1) + "].");
	}

	private static WorldRegion[][][] getRegions()
	{
		return _worldRegions;
	}

	private static int validX(int x)
	{
		if(x < 0)
			x = 0;
		else if(x > REGIONS_X)
			x = REGIONS_X;
		return x;
	}

	private static int validY(int y)
	{
		if(y < 0)
			y = 0;
		else if(y > REGIONS_Y)
			y = REGIONS_Y;
		return y;
	}

	private static int validZ(int z)
	{
		if(z < 0)
			z = 0;
		else if(z > REGIONS_Z)
			z = REGIONS_Z;
		return z;
	}

	public static int validCoordX(int x)
	{
		if(x < MAP_MIN_X)
			x = MAP_MIN_X + 1;
		else if(x > MAP_MAX_X)
			x = MAP_MAX_X - 1;
		return x;
	}

	public static int validCoordY(int y)
	{
		if(y < MAP_MIN_Y)
			y = MAP_MIN_Y + 1;
		else if(y > MAP_MAX_Y)
			y = MAP_MAX_Y - 1;
		return y;
	}

	public static int validCoordZ(int z)
	{
		if(z < MAP_MIN_Z)
			z = MAP_MIN_Z + 1;
		else if(z > MAP_MAX_Z)
			z = MAP_MAX_Z - 1;
		return z;
	}

	private static int regionX(int x)
	{
		return (x >> SHIFT_BY) + OFFSET_X;
	}

	private static int regionY(int y)
	{
		return (y >> SHIFT_BY) + OFFSET_Y;
	}

	private static int regionZ(int z)
	{
		return (z >> SHIFT_BY_Z) + OFFSET_Z;
	}

	static boolean isNeighbour(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return x1 <= x2 + 1 && x1 >= x2 - 1 && y1 <= y2 + 1 && y1 >= y2 - 1 && z1 <= z2 + 1 && z1 >= z2 - 1;
	}

	/**
	 * @param loc локация для поиска региона
	 * @return Регион, соответствующий локации
	 */
	public static WorldRegion getRegion(Location loc)
	{
		return getRegion(validX(regionX(loc.x)), validY(regionY(loc.y)), validZ(regionZ(loc.z)));
	}

	/**
	 * @param obj обьект для поиска региона
	 * @return Регион, соответствующий координатам обьекта
	 */
	public static WorldRegion getRegion(GameObject obj)
	{
		return getRegion(validX(regionX(obj.getX())), validY(regionY(obj.getY())), validZ(regionZ(obj.getZ())));
	}

	/**
	 * @param x координата на карте регионов
	 * @param y координата на карте регионов
	 * @param z координата на карте регионов
	 * @return Регион, соответствующий координатам
	 */
	private static WorldRegion getRegion(int x, int y, int z)
	{
		WorldRegion[][][] regions = getRegions();
		WorldRegion region = null;
		region = regions[x][y][z];
		if(region == null)
			synchronized (regions)
			{
				region = regions[x][y][z];
				if(region == null)
					region = regions[x][y][z] = new WorldRegion(x, y, z);
			}
		return region;
	}

	/**
	 * Находит игрока по имени
	 * Регистр символов любой.
	 * @param name имя
	 * @return найденый игрок или null если игрока нет
	 */
	public static Player getPlayer(String name)
	{
		return GameObjectsStorage.getPlayer(name);
	}

	/**
	 * Находит игрока по objectId
	 *
	 * @param objId
	 * @return найденый игрок или null если игрока нет
	 */
	public static Player getPlayer(int objId)
	{
		return GameObjectsStorage.getPlayer(objId);
	}

	/**
	 * Проверяет, сменился ли регион в котором находится обьект
	 * Если сменился - удаляет обьект из старого региона и добавляет в новый.
	 * @param object обьект для проверки
	 * @param dropper - если это L2ItemInstance, то будет анимация дропа с перса
	 */
	public static void addVisibleObject(GameObject object, Creature dropper)
	{
		if(object == null || !object.isVisible())
			return;

		final WorldRegion region = getRegion(object);
		final WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == region)
			return;

		int x1, y0, y1, z0, z1;
		if(currentRegion == null) // Новый обьект (пример - игрок вошел в мир, заспаунился моб, дропнули вещь)
		{
			// Добавляем обьект в список видимых
			object.setCurrentRegion(region);
			region.addObject(object);

			// Показываем обьект в текущем и соседних регионах
			// Если обьект игрок, показываем ему все обьекты в текущем и соседних регионах
			x1 = validX(region.getX() + 1);
			y0 = validY(region.getY() - 1);
			y1 = validY(region.getY() + 1);
			z0 = validZ(region.getZ() - 1);
			z1 = validZ(region.getZ() + 1);
			for(int x = validX(region.getX() - 1); x <= x1; x++)
				for(int y = y0; y <= y1; y++)
					for(int z = z0; z <= z1; z++)
						getRegion(x, y, z).addToPlayers(object, dropper);
		}
		else// Обьект уже существует, перешел из одного региона в другой
		{
			currentRegion.removeObject(object); // Удаляем обьект из старого региона
			object.setCurrentRegion(region);
			region.addObject(object); // Добавляем обьект в список видимых

			// Убираем обьект из старых соседей.
			int rx = region.getX();
			int ry = region.getY();
			int rz = region.getZ();
			x1 = validX(currentRegion.getX() + 1);
			y0 = validY(currentRegion.getY() - 1);
			y1 = validY(currentRegion.getY() + 1);
			z0 = validZ(currentRegion.getZ() - 1);
			z1 = validZ(currentRegion.getZ() + 1);
			for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
				for(int y = y0; y <= y1; y++)
					for(int z = z0; z <= z1; z++)
						if(!isNeighbour(rx, ry, rz, x, y, z))
							getRegion(x, y, z).removeFromPlayers(object);

			// Показываем обьект, но в отличие от первого случая - только для новых соседей.
			x1 = validX(region.getX() + 1);
			y0 = validY(region.getY() - 1);
			y1 = validY(region.getY() + 1);
			z0 = validZ(region.getZ() - 1);
			z1 = validZ(region.getZ() + 1);
			rx = currentRegion.getX();
			ry = currentRegion.getY();
			rz = currentRegion.getZ();
			for(int x = validX(region.getX() - 1); x <= x1; x++)
				for(int y = y0; y <= y1; y++)
					for(int z = z0; z <= z1; z++)
						if(!isNeighbour(rx, ry, rz, x, y, z))
							getRegion(x, y, z).addToPlayers(object, dropper);
		}
	}

	/**
	 * Удаляет обьект из текущего региона
	 * @param object обьект для удаления
	 */
	public static void removeVisibleObject(GameObject object)
	{
		if(object == null || object.isVisible())
			return;

		final WorldRegion currentRegion;
		if((currentRegion = object.getCurrentRegion()) == null)
			return;

		object.setCurrentRegion(null);
		currentRegion.removeObject(object);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					getRegion(x, y, z).removeFromPlayers(object);
	}

	public static GameObject getAroundObjectById(GameObject object, int objId)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return null;

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.getObjectId() == objId)
							return obj;

		return null;
	}

	public static List<GameObject> getAroundObjects(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<GameObject> result = new LazyArrayList<GameObject>(128);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						result.add(obj);
					}			
		return result;
	}

	public static List<GameObject> getAroundObjects(GameObject object, int radius, int height)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		final int numOfRegions = (radius >> SHIFT_BY) > 0 ? 2 : 1;
		final int numOfRegionsZ = (height >> SHIFT_BY_Z) > 0 ? 2 : 1;

		final List<GameObject> result = new LazyArrayList<GameObject>(128);

		final int x1 = validX(currentRegion.getX() + numOfRegions);
		final int y0 = validY(currentRegion.getY() - numOfRegions);
		final int y1 = validY(currentRegion.getY() + numOfRegions);
		final int z0 = validZ(currentRegion.getZ() - numOfRegionsZ);
		final int z1 = validZ(currentRegion.getZ() + numOfRegionsZ);
		for(int x = validX(currentRegion.getX() - numOfRegions); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if(Math.abs(obj.getZ() - oz) > height)
							continue;
						int dx = Math.abs(obj.getX() - ox);
						if(dx > radius)
							continue;
						int dy = Math.abs(obj.getY() - oy);
						if(dy > radius)
							continue;
						if(dx * dx + dy * dy > sqrad)
							continue;

						result.add(obj);
					}

		return result;
	}

	public static List<Creature> getAroundCharacters(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<Creature> result = new LazyArrayList<Creature>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						result.add((Creature)obj);
					}

		return result;
	}

	public static List<Creature> getAroundCharacters(GameObject object, int radius, int height)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		final int numOfRegions = (radius >> SHIFT_BY) > 0 ? 2 : 1;
		final int numOfRegionsZ = (height >> SHIFT_BY_Z) > 0 ? 2 : 1;

		final List<Creature> result = new LazyArrayList<Creature>(64);

		final int x1 = validX(currentRegion.getX() + numOfRegions);
		final int y0 = validY(currentRegion.getY() - numOfRegions);
		final int y1 = validY(currentRegion.getY() + numOfRegions);
		final int z0 = validZ(currentRegion.getZ() - numOfRegionsZ);
		final int z1 = validZ(currentRegion.getZ() + numOfRegionsZ);
		for(int x = validX(currentRegion.getX() - numOfRegions); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if(Math.abs(obj.getZ() - oz) > height)
							continue;
						int dx = Math.abs(obj.getX() - ox);
						if(dx > radius)
							continue;
						int dy = Math.abs(obj.getY() - oy);
						if(dy > radius)
							continue;
						if(dx * dx + dy * dy > sqrad)
							continue;

						result.add((Creature)obj);
					}
		return result;
	}

	public static List<NpcInstance> getAroundNpc(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<NpcInstance> result = new LazyArrayList<NpcInstance>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						result.add((NpcInstance)obj);
					}
		return result;
	}

	public static List<NpcInstance> getAroundNpc(GameObject object, int radius, int height)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		final int numOfRegions = (radius >> SHIFT_BY) > 0 ? 2 : 1;
		final int numOfRegionsZ = (height >> SHIFT_BY_Z) > 0 ? 2 : 1;

		final List<NpcInstance> result = new LazyArrayList<NpcInstance>(64);

		final int x1 = validX(currentRegion.getX() + numOfRegions);
		final int y0 = validY(currentRegion.getY() - numOfRegions);
		final int y1 = validY(currentRegion.getY() + numOfRegions);
		final int z0 = validZ(currentRegion.getZ() - numOfRegionsZ);
		final int z1 = validZ(currentRegion.getZ() + numOfRegionsZ);
		for(int x = validX(currentRegion.getX() - numOfRegions); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if(Math.abs(obj.getZ() - oz) > height)
							continue;
						int dx = Math.abs(obj.getX() - ox);
						if(dx > radius)
							continue;
						int dy = Math.abs(obj.getY() - oy);
						if(dy > radius)
							continue;
						if(dx * dx + dy * dy > sqrad)
							continue;

						result.add((NpcInstance)obj);
					}
		return result;
	}

	public static List<Playable> getAroundPlayables(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<Playable> result = new LazyArrayList<Playable>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayable() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						result.add((Playable)obj);
					}
		return result;
	}

	public static List<Playable> getAroundPlayables(GameObject object, int radius, int height)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		final int numOfRegions = (radius >> SHIFT_BY) > 0 ? 2 : 1;
		final int numOfRegionsZ = (height >> SHIFT_BY_Z) > 0 ? 2 : 1;

		final List<Playable> result = new LazyArrayList<Playable>(64);

		final int x1 = validX(currentRegion.getX() + numOfRegions);
		final int y0 = validY(currentRegion.getY() - numOfRegions);
		final int y1 = validY(currentRegion.getY() + numOfRegions);
		final int z0 = validZ(currentRegion.getZ() - numOfRegionsZ);
		final int z1 = validZ(currentRegion.getZ() + numOfRegionsZ);
		for(int x = validX(currentRegion.getX() - numOfRegions); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayable() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if(Math.abs(obj.getZ() - oz) > height)
							continue;
						int dx = Math.abs(obj.getX() - ox);
						if(dx > radius)
							continue;
						int dy = Math.abs(obj.getY() - oy);
						if(dy > radius)
							continue;
						if(dx * dx + dy * dy > sqrad)
							continue;

						result.add((Playable)obj);
					}
		return result;
	}

	public static List<Player> getAroundPlayers(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<Player> result = new LazyArrayList<Player>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						result.add((Player)obj);
					}
		return result;
	}

	public static List<Player> getAroundPlayers(GameObject object, int radius, int height)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		final int numOfRegions = (radius >> SHIFT_BY) > 0 ? 2 : 1;
		final int numOfRegionsZ = (height >> SHIFT_BY_Z) > 0 ? 2 : 1;

		final List<Player> result = new LazyArrayList<Player>(64);

		final int x1 = validX(currentRegion.getX() + numOfRegions);
		final int y0 = validY(currentRegion.getY() - numOfRegions);
		final int y1 = validY(currentRegion.getY() + numOfRegions);
		final int z0 = validZ(currentRegion.getZ() - numOfRegionsZ);
		final int z1 = validZ(currentRegion.getZ() + numOfRegionsZ);
		for(int x = validX(currentRegion.getX() - numOfRegions); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if(Math.abs(obj.getZ() - oz) > height)
							continue;
						int dx = Math.abs(obj.getX() - ox);
						if(dx > radius)
							continue;
						int dy = Math.abs(obj.getY() - oy);
						if(dy > radius)
							continue;
						if(dx * dx + dy * dy > sqrad)
							continue;

						result.add((Player)obj);
					}
		return result;
	}

	/**
	 * Получить список игроков для координат, а не объекта
	 * Используется для рассылки броадкаста кораблей
	 * @param loc
	 * @return
	 */
	public static List<Player> getAroundObservers(Location loc)
	{
		final WorldRegion currentRegion = getRegion(loc);
		if(currentRegion == null)
			return Collections.emptyList();

		final List<Player> result = new LazyArrayList<Player>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isObservePoint() || obj.isPlayer())
						{
							if(obj.isPlayer() && ((Player)obj).isInObserverMode())
								continue;
							result.add(obj.getPlayer());
						}
		return result;
	}

	/**
	 * Получить список игроков для отправки или обновлении информации (пакетный уровень)
	 * 
	 * @param object
	 * @return
	 */
	public static List<Player> getAroundObservers(GameObject object)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();

		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();

		final List<Player> result = new LazyArrayList<Player>(64);

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						
						if(obj.isObservePoint() || obj.isPlayer())
						{
							if(obj.isPlayer() && ((Player)obj).isInObserverMode())
								continue;
							result.add(obj.getPlayer());
						}
					}
		return result;
	}
	
	/**
	 * Проверить, пустые ли соседние регионы от игроков, включая текущий
	 * @return
	 */
	public static boolean isNeighborsEmpty(WorldRegion region)
	{
		final int x1 = validX(region.getX() + 1);
		final int y0 = validY(region.getY() - 1);
		final int y1 = validY(region.getY() + 1);
		final int z0 = validZ(region.getZ() - 1);
		final int z1 = validZ(region.getZ() + 1);
		for(int x = validX(region.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					if(!getRegion(x, y, z).isEmpty())
						return false;
		return true;
	}

	public static void activate(WorldRegion currentRegion)
	{
		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					getRegion(x, y, z).setActive(true);
	}

	public static void deactivate(WorldRegion currentRegion)
	{
		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					if(isNeighborsEmpty(getRegion(x, y, z)))
						getRegion(x, y, z).setActive(false);
	}


	/**
	 * Показывает игроку все видимые обьекты в текущем регионе и соседних
	 * 
	 * @param player
	 * @param isObserver - показать объекты в точке наблюдения
	 */
	public static void showObjectsToPlayer(Player player, boolean isObserver) // TODO: DS: убрать isObserver
	{
		final WorldRegion currentRegion = isObserver ? player.getObservePoint().getCurrentRegion() : player.getCurrentRegion();
		if(currentRegion == null)
			return;

		final int oid = player.getObjectId();
		final int rid = isObserver ? player.getObservePoint().getReflectionId() : player.getReflectionId();

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						player.sendPacket(player.addVisibleObject(obj, null));
					}
	}

	/**
	 * Убирает у игрока все видимые обьекты в текущем регионе и соседних
	 * 
	 * @param player
	 * @param isObserver - убрать объекты в точке наблюдения	 
	 */
	public static void removeObjectsFromPlayer(Player player, boolean isObserver) // TODO: DS: убрать isObserver
	{
		final WorldRegion currentRegion = isObserver ? player.getObservePoint().getCurrentRegion() : player.getCurrentRegion();
		if(currentRegion == null)
			return;

		final int oid = player.getObjectId();
		final int rid = isObserver ? player.getObservePoint().getReflectionId() : player.getReflectionId();

		final int x1 = validX(currentRegion.getX() + 1);
		final int y0 = validY(currentRegion.getY() - 1);
		final int y1 = validY(currentRegion.getY() + 1);
		final int z0 = validZ(currentRegion.getZ() - 1);
		final int z1 = validZ(currentRegion.getZ() + 1);
		for(int x = validX(currentRegion.getX() - 1); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;

						player.sendPacket(player.removeVisibleObject(obj, null));
					}
	}

	/**
	 * Убирает обьект у всех игроков в регионе
	 */
	public static void removeObjectFromPlayers(GameObject object)
	{
		List<L2GameServerPacket> d = null;
		for(Player p : World.getAroundObservers(object))
			p.sendPacket(p.removeVisibleObject(object, d == null ? d = object.deletePacketList(p) : d));
	}
	
	static void addZone(Zone zone)
	{
		final Reflection reflection = zone.getReflection();

		final Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}

		final int x1 = validX(regionX(territory.getXmax()));
		final int y0 = validY(regionY(territory.getYmin()));
		final int y1 = validY(regionY(territory.getYmax()));
		final int z0 = validZ(regionZ(territory.getZmin()));
		final int z1 = validZ(regionZ(territory.getZmax()));
		for(int x = validX(regionX(territory.getXmin())); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
				{
					WorldRegion region = getRegion(x, y, z);
					region.addZone(zone);
					for(GameObject obj : region)
					{
						if(!obj.isCreature() || obj.getReflection() != reflection)
							continue;

						((Creature)obj).updateZones();
					}
				}
	}

	static void removeZone(Zone zone)
	{
		final Reflection reflection = zone.getReflection();

		final Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}

		final int x1 = validX(regionX(territory.getXmax()));
		final int y0 = validY(regionY(territory.getYmin()));
		final int y1 = validY(regionY(territory.getYmax()));
		final int z0 = validZ(regionZ(territory.getZmin()));
		final int z1 = validZ(regionZ(territory.getZmax()));
		for(int x = validX(regionX(territory.getXmin())); x <= x1; x++)
			for(int y = y0; y <= y1; y++)
				for(int z = z0; z <= z1; z++)
				{
					WorldRegion region = getRegion(x, y, z);
					region.removeZone(zone);
					for(GameObject obj : region)
					{
						if(!obj.isCreature() || obj.getReflection() != reflection)
							continue;

						((Creature)obj).updateZones();
					}
				}
	}

	/**
	 * Создает и возвращает список территорий для точек x, y, z
	 */
	public static void getZones(List<Zone> inside, Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
			return;
		for(Zone zone : zones)
			if(zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				inside.add(zone);
	}

	public static boolean isWater(Location loc, Reflection reflection)
	{
		return getWater(loc, reflection) != null;
	}

	public static Zone getWater(Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
			return null;
		for(Zone zone : zones)
			if(zone != null && zone.getType() == ZoneType.water && zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				return zone;
		return null;
	}

	/**
	 * Возвращает статистку по регионам
	 * @return int[], где<br>
	 * [0] количество активных регионов<br>
	 * [1] количество неактивных регионов<br>
	 * [2] количество неинициализированных регионов<br>
	 *
	 * [10] количество объектов<br>
	 * [11] количество персонажей<br>
	 * [12] количество игроков<br>
	 * [13] количество игроков в оффлайн-режиме<br>
	 * [14] количество NPC<br>
	 * [15] количество активных NPC<br>
	 * [16] количество монстров<br>
	 * [17] количество минионов<br>
	 * [18] количество саммонов/петов<br>
	 * [19] количество дверей<br>
	 * [20] количество вещей<br>
	 */
	public static int[] getStats()
	{
		WorldRegion region;
		int[] ret = new int[32];

		for(int x = 0; x <= REGIONS_X; x++)
			for(int y = 0; y <= REGIONS_Y; y++)
				for(int z = 0; z <= REGIONS_Z; z++)
				{
					ret[0]++;

					region = _worldRegions[x][y][z];

					if (region != null)
					{
						if(region.isActive())
							ret[1]++;
						else
							ret[2]++;

						for(GameObject obj : region)
						{
							ret[10]++;

							if(obj.isCreature())
							{
								ret[11]++;

								if(obj.isPlayer())
								{
									ret[12]++;
									Player p = (Player)obj;

									if(p.isInOfflineMode())
										ret[13]++;
								}
								else if(obj.isNpc())
								{
									ret[14]++;

									if(obj.isMonster())
									{
										ret[16]++;
										if(obj.isMinion())
											ret[17]++;
									}

									NpcInstance npc = (NpcInstance) obj;
									if(npc.hasAI())
									{
										if(npc.getAI().isActive())
											ret[15]++;
									}
								}
								else if(obj.isPlayable())
								{
									ret[18]++;
								}
								else if(obj.isDoor())
								{
									ret[19]++;
								}
							}
							else if(obj.isItem())
							{
								ret[20]++;
							}
						}
					}
					else
						ret[3]++;

				}
		return ret;
	}
}