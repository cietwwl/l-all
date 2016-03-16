package org.mmocore.gameserver.handler.voicecommands.impl;

import java.text.NumberFormat;
import java.util.Locale;

import org.mmocore.gameserver.data.htm.HtmCache;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.network.l2.components.HtmlMessage;
import org.mmocore.gameserver.stats.Formulas;
import org.mmocore.gameserver.stats.Stats;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;

public class WhoAmI implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "whoami", "whoiam" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		final GameObject object = player.getTarget();
		final Creature target = object != null && object.isCreature() ? (Creature) object : null;

		//TODO [G1ta0] добавить рефлекты
		double hpRegen = Formulas.calcHpRegen(player);
		double cpRegen = Formulas.calcCpRegen(player);
		double mpRegen = Formulas.calcMpRegen(player);
		double hpDrain = player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0., target, null);
		double mpDrain = player.calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0., target, null);
		double hpGain = player.calcStat(Stats.HEAL_EFFECTIVNESS, 100., target, null);
		double mpGain = player.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., target, null);
		double critPerc = player.calcStat(Stats.CRITICAL_DAMAGE, target, null);
		double critStatic = player.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, null);
		double mCritRate = player.calcStat(Stats.MCRITICAL_RATE, target, null);
		double blowRate = player.calcStat(Stats.FATALBLOW_RATE, target, null);

		ItemInstance shld = player.getSecondaryWeaponInstance();
		boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;

		double shieldDef = shield ? player.calcStat(Stats.SHIELD_DEFENCE, player.getTemplate().baseShldDef, target, null) : 0.;
		double shieldRate = shield ? player.calcStat(Stats.SHIELD_RATE, target, null) : 0.;

		double xpRate = player.getRateExp();
		double spRate = player.getRateSp();
		double dropRate = player.getRateItems();
		double adenaRate = player.getRateAdena();
		double spoilRate = player.getRateSpoil();

		double fireResist = player.calcStat(Element.FIRE.getDefence(), 0., target, null);
		double windResist = player.calcStat(Element.WIND.getDefence(), 0., target, null);
		double waterResist = player.calcStat(Element.WATER.getDefence(), 0., target, null);
		double earthResist = player.calcStat(Element.EARTH.getDefence(), 0., target, null);
		double holyResist = player.calcStat(Element.HOLY.getDefence(), 0., target, null);
		double unholyResist = player.calcStat(Element.UNHOLY.getDefence(), 0., target, null);

		double bleedPower = player.calcStat(Stats.BLEED_POWER, 100., target, null) - 100.;
		double bleedResist = player.calcStat(Stats.BLEED_RESIST, 100., target, null) - 100.;
		double poisonPower = player.calcStat(Stats.POISON_POWER, 100., target, null) - 100.;
		double poisonResist = player.calcStat(Stats.POISON_RESIST, 100., target, null) - 100.;
		double stunPower = player.calcStat(Stats.STUN_POWER, 100., target, null) - 100.;
		double stunResist = player.calcStat(Stats.STUN_RESIST, 100., target, null) - 100.;
		double rootPower = player.calcStat(Stats.ROOT_POWER, 100., target, null) - 100.;
		double rootResist = player.calcStat(Stats.ROOT_RESIST, 100., target, null) - 100.;
		double sleepPower = player.calcStat(Stats.SLEEP_POWER, 100., target, null) - 100.;
		double sleepResist = player.calcStat(Stats.SLEEP_RESIST, 100., target, null) - 100.;
		double paralyzePower = player.calcStat(Stats.PARALYZE_POWER, 100., target, null) - 100.;
		double paralyzeResist = player.calcStat(Stats.PARALYZE_RESIST, 100., target, null) - 100.;
		double mentalPower = player.calcStat(Stats.MENTAL_POWER, 100., target, null) - 100.;
		double mentalResist = player.calcStat(Stats.MENTAL_RESIST, 100., target, null) - 100.;
		double debuffPower = player.calcStat(Stats.DEBUFF_POWER, 100., target, null) - 100.;
		double debuffResist = player.calcStat(Stats.DEBUFF_RESIST, 100., target, null) - 100.;
		double cancelPower = player.calcStat(Stats.CANCEL_POWER, target, null);
		double cancelResist = player.calcStat(Stats.CANCEL_RESIST, target, null);

		double swordResist = 100. - player.calcStat(Stats.SWORD_WPN_VULNERABILITY, target, null);
		double dualResist = 100. - player.calcStat(Stats.DUAL_WPN_VULNERABILITY, target, null);
		double bluntResist = 100. - player.calcStat(Stats.BLUNT_WPN_VULNERABILITY, target, null);
		double daggerResist = 100. - player.calcStat(Stats.DAGGER_WPN_VULNERABILITY, target, null);
		double bowResist = 100. - player.calcStat(Stats.BOW_WPN_VULNERABILITY, target, null);
		double crossbowResist = 100. - player.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, target, null);
		double poleResist = 100. - player.calcStat(Stats.POLE_WPN_VULNERABILITY, target, null);
		double fistResist = 100. - player.calcStat(Stats.FIST_WPN_VULNERABILITY, target, null);

		double critChanceResist = 100. - player.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, target, null);
		double critDamResistStatic = player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, target, null);
		double critDamResist = 100. - 100 * (player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, 1., target, null) - critDamResistStatic);

		NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(1);
		df.setMinimumFractionDigits(1);

		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(HtmCache.getInstance().getHtml("command/whoami.htm", player));
		msg.replace("%hpRegen%", df.format(hpRegen));
		msg.replace("%cpRegen%", df.format(cpRegen));
		msg.replace("%mpRegen%", df.format(mpRegen));
		msg.replace("%hpDrain%", df.format(hpDrain));
		msg.replace("%mpDrain%", df.format(mpDrain));
		msg.replace("%hpGain%", df.format(hpGain));
		msg.replace("%mpGain%", df.format(mpGain));
		msg.replace("%critPerc%", df.format(critPerc));
		msg.replace("%critStatic%", df.format(critStatic));
		msg.replace("%mCritRate%", df.format(mCritRate));
		msg.replace("%blowRate%", df.format(blowRate));
		msg.replace("%shieldDef%", df.format(shieldDef));
		msg.replace("%shieldRate%", df.format(shieldRate));
		msg.replace("%xpRate%", df.format(xpRate));
		msg.replace("%spRate%", df.format(spRate));
		msg.replace("%dropRate%", df.format(dropRate));
		msg.replace("%adenaRate%", df.format(adenaRate));
		msg.replace("%spoilRate%", df.format(spoilRate));
		msg.replace("%fireResist%", df.format(fireResist));
		msg.replace("%windResist%", df.format(windResist));
		msg.replace("%waterResist%", df.format(waterResist));
		msg.replace("%earthResist%", df.format(earthResist));
		msg.replace("%holyResist%", df.format(holyResist));
		msg.replace("%darkResist%", df.format(unholyResist));
		msg.replace("%bleedPower%", df.format(bleedPower));
		msg.replace("%bleedResist%", df.format(bleedResist));
		msg.replace("%poisonPower%", df.format(poisonPower));
		msg.replace("%poisonResist%", df.format(poisonResist));
		msg.replace("%stunPower%", df.format(stunPower));
		msg.replace("%stunResist%", df.format(stunResist));
		msg.replace("%rootPower%", df.format(rootPower));
		msg.replace("%rootResist%", df.format(rootResist));
		msg.replace("%sleepPower%", df.format(sleepPower));
		msg.replace("%sleepResist%", df.format(sleepResist));
		msg.replace("%paralyzePower%", df.format(paralyzePower));
		msg.replace("%paralyzeResist%", df.format(paralyzeResist));
		msg.replace("%mentalPower%", df.format(mentalPower));
		msg.replace("%mentalResist%", df.format(mentalResist));
		msg.replace("%debuffPower%", df.format(debuffPower));
		msg.replace("%debuffResist%", df.format(debuffResist));
		msg.replace("%cancelPower%", df.format(cancelPower));
		msg.replace("%cancelResist%", df.format(cancelResist));
		msg.replace("%swordResist%", df.format(swordResist));
		msg.replace("%dualResist%", df.format(dualResist));
		msg.replace("%bluntResist%", df.format(bluntResist));
		msg.replace("%daggerResist%", df.format(daggerResist));
		msg.replace("%bowResist%", df.format(bowResist));
		msg.replace("%crossbowResist%", df.format(crossbowResist));
		msg.replace("%fistResist%", df.format(fistResist));
		msg.replace("%poleResist%", df.format(poleResist));
		msg.replace("%critChanceResist%", df.format(critChanceResist));
		msg.replace("%critDamResist%", df.format(critDamResist));
		player.sendPacket(msg);

		return true;
	}
}
