package org.mmocore.gameserver.ai;

import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.skills.SkillEntry;

public class Ranger extends DefaultAI
{
	public Ranger(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		return super.thinkActive() || defaultThinkBuff(2);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, SkillEntry skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);
		NpcInstance actor = getActor();
		if(actor.isDead() || attacker == null || actor.getDistance(attacker) > 200)
			return;

		if(actor.isMoving)
			return;

		int posX = actor.getX();
		int posY = actor.getY();
		int posZ = actor.getZ();

		int old_posX = posX;
		int old_posY = posY;
		int old_posZ = posZ;

		int signx = posX < attacker.getX() ? -1 : 1;
		int signy = posY < attacker.getY() ? -1 : 1;

		// int range = (int) ((actor.calculateAttackSpeed()  /1000 * actor.getWalkSpeed() )* 0.71); // was "actor.getPhysicalAttackRange()"    0.71 = sqrt(2) / 2

		int range = (int) (0.71 * actor.calculateAttackDelay() / 1000 * actor.getMoveSpeed());

		posX += signx * range;
		posY += signy * range;
		posZ = GeoEngine.getHeight(posX, posY, posZ, actor.getGeoIndex());

		if(GeoEngine.canMoveToCoord(old_posX, old_posY, old_posZ, posX, posY, posZ, actor.getGeoIndex()))
		{
			addTaskMove(posX, posY, posZ, false);
			addTaskAttack(attacker);
		}
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultFightTask();
	}

	@Override
	public int getRatePHYS()
	{
		return 10;
	}

	@Override
	public int getRateDOT()
	{
		return 15;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 8;
	}

	@Override
	public int getRateDAM()
	{
		return 20;
	}

	@Override
	public int getRateSTUN()
	{
		return 15;
	}

	@Override
	public int getRateBUFF()
	{
		return 3;
	}

	@Override
	public int getRateHEAL()
	{
		return 20;
	}
}