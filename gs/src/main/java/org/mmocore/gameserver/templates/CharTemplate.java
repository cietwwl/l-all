package org.mmocore.gameserver.templates;


import org.mmocore.gameserver.templates.item.WeaponTemplate;

public class CharTemplate
{
	public final static int[] EMPTY_ATTRIBUTES = new int[6];

	public final int baseSTR;
	public final int baseCON;
	public final int baseDEX;
	public final int baseINT;
	public final int baseWIT;
	public final int baseMEN;
	public final double baseHpMax;
	public final double baseMpMax;
	public final double baseCpMax;

	/** HP Regen base */
	public final double baseHpReg;

	/** MP Regen base */
	public final double baseMpReg;

	/** CP Regen base */
	public final double baseCpReg;

	public final double basePAtk;
	public final double baseMAtk;
	public final double basePDef;
	public final double baseMDef;
	public final int basePAtkSpd;
	public final int baseMAtkSpd;
	public final double baseShldDef;
	public final int baseAtkRange;
	public final WeaponTemplate.WeaponType baseAtkType;
	public final int baseShldRate;
	public final int baseCritRate;
	public final int baseRunSpd;
	public final int baseWalkSpd;

	public final int[] baseAttributeAttack;
	public final int[] baseAttributeDefence;

	public final double collisionRadius;
	public final double collisionHeight;

	public CharTemplate(StatsSet set)
	{
		baseSTR = set.getInteger("baseSTR");
		baseCON = set.getInteger("baseCON");
		baseDEX = set.getInteger("baseDEX");
		baseINT = set.getInteger("baseINT");
		baseWIT = set.getInteger("baseWIT");
		baseMEN = set.getInteger("baseMEN");
		baseHpMax = set.getDouble("baseHpMax");
		baseCpMax = set.getDouble("baseCpMax");
		baseMpMax = set.getDouble("baseMpMax");
		baseHpReg = set.getDouble("baseHpReg");
		baseCpReg = set.getDouble("baseCpReg");
		baseMpReg = set.getDouble("baseMpReg");
		basePAtk = set.getDouble("basePAtk");
		baseMAtk = set.getDouble("baseMAtk");
		basePDef = set.getDouble("basePDef");
		baseMDef = set.getDouble("baseMDef");
		basePAtkSpd = set.getInteger("basePAtkSpd");
		baseMAtkSpd = set.getInteger("baseMAtkSpd");
		baseShldDef = set.getDouble("baseShldDef");
		baseAtkRange = set.getInteger("baseAtkRange");
		baseAtkType = set.getEnum("baseAtkType", WeaponTemplate.WeaponType.class);
		baseShldRate = set.getInteger("baseShldRate");
		baseCritRate = set.getInteger("baseCritRate");
		baseRunSpd = set.getInteger("baseRunSpd");
		baseWalkSpd = set.getInteger("baseWalkSpd");
		baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", EMPTY_ATTRIBUTES);
		baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", EMPTY_ATTRIBUTES);
		// Geometry
		collisionRadius = set.getDouble("collision_radius", 5);
		collisionHeight = set.getDouble("collision_height", 5);
	}

	public int getNpcId()
	{
		return 0;
	}

	public static StatsSet getEmptyStatsSet()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseHpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseCpReg", 0);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseAtkType", WeaponTemplate.WeaponType.SWORD);
		return npcDat;
	}
}