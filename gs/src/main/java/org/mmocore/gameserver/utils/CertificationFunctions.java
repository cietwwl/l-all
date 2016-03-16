package org.mmocore.gameserver.utils;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.SubClass;
import org.mmocore.gameserver.model.base.ClassId;
import org.mmocore.gameserver.model.base.ClassType2;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SkillList;
import org.mmocore.gameserver.scripts.Functions;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.skills.SkillEntryType;


/**
 * @author VISTALL
 * @date  17:49/08.12.2010
 */
public class CertificationFunctions
{
	public static final String PATH = "villagemaster/certification/";

	public static void showCertificationList(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, true))
		{
			return;
		}

		Functions.show(PATH + "certificatelist.htm", player, npc);
	}

	public static void getCertification65(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, false))
		{
			return;
		}

		SubClass clzz = player.getActiveClass();
		if (clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ItemFunctions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_65);
		player.store(true);
	}

	public static void getCertification70(NpcInstance npc, Player player)
	{
		if (!checkConditions(70, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ItemFunctions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_70);
		player.store(true);
	}

	public static void getCertification75List(NpcInstance npc, Player player)
	{
		if (!checkConditions(75, npc, player, false))
		{
			return;
		}

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.show(PATH + "certificate-choose.htm", player, npc);
	}

	public static void getCertification75(NpcInstance npc, Player player, boolean classCertifi)
	{
		if (!checkConditions(75, npc, player, false))
		{
			return;
		}

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		if (classCertifi)
		{
			ClassId cl = ClassId.VALUES[clzz.getClassId()];
			if(cl.getType2() == null)
				return;


			ItemFunctions.addItem(player, cl.getType2().getCertificateId(), 1);
		}
		else
		{
			ItemFunctions.addItem(player, 10612, 1); // master ability
		}

		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
	}

	public static void getCertification80(NpcInstance npc, Player player)
	{
		if (!checkConditions(80, npc, player, false))
		{
			return;
		}

		SubClass clzz = player.getActiveClass();

		// если не взят(ы) преведущий сертификат(ы)
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70) || !clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_80))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if(cl.getType2() == null)
			return;

		ItemFunctions.addItem(player, cl.getType2().getTransformationId(), 1);
		clzz.addCertification(SubClass.CERTIFICATION_80);
		player.store(true);
	}

	public static void cancelCertification(NpcInstance npc, Player player)
	{
		if(!player.getActiveClass().isBase())
			return;

		if(!player.reduceAdena(10000000L, true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		for (ClassType2 classType2 : ClassType2.VALUES)
		{
			player.getInventory().destroyItemByItemId(classType2.getCertificateId(), player.getInventory().getCountOf(classType2.getCertificateId()));
			player.getInventory().destroyItemByItemId(classType2.getTransformationId(), player.getInventory().getCountOf(classType2.getTransformationId()));
		}

		for(SkillEntry skill : player.getAllSkills())
			if(skill.getEntryType() == SkillEntryType.CERTIFICATION)
				player.removeSkill(skill, true);

		for(SubClass subClass : player.getSubClasses().values())
		{
			if(!subClass.isBase())
				subClass.setCertification(0);
		}

		player.sendPacket(new SkillList(player));
		Functions.showFromStringHolder("scripts.services.SubclassSkills.SkillsDeleted", player);
	}

	public static boolean checkConditions(int level, NpcInstance npc, Player player, boolean first)
	{
		if (player.getLevel() < level)
		{
			Functions.show(PATH + "certificate-nolevel.htm", player, npc, "%level%", level);
			return false;
		}

		if (player.getActiveClass().isBase())
		{
			Functions.show(PATH + "certificate-nosub.htm", player, npc);
			return false;
		}

		if (first)
		{
			return true;
		}

//		for (ClassType2 type : ClassType2.VALUES)
//		{
//			if (player.getInventory().getCountOf(type.getCertificateId()) > 0 || player.getInventory().getCountOf(type.getTransformationId()) > 0)
//			{
//				Functions.show(PATH + "certificate-already.htm", player, npc);
//				return false;
//			}
//		}

		return true;
	}
}
