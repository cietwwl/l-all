package org.mmocore.gameserver.skills;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.skills.effects.EffectAddSkills;
import org.mmocore.gameserver.skills.effects.EffectAgathionRes;
import org.mmocore.gameserver.skills.effects.EffectAggression;
import org.mmocore.gameserver.skills.effects.EffectBetray;
import org.mmocore.gameserver.skills.effects.EffectBlessNoblesse;
import org.mmocore.gameserver.skills.effects.EffectBlockStat;
import org.mmocore.gameserver.skills.effects.EffectBluff;
import org.mmocore.gameserver.skills.effects.EffectBuff;
import org.mmocore.gameserver.skills.effects.EffectBuffImmunity;
import org.mmocore.gameserver.skills.effects.EffectCPDamPercent;
import org.mmocore.gameserver.skills.effects.EffectCallSkills;
import org.mmocore.gameserver.skills.effects.EffectCancel;
import org.mmocore.gameserver.skills.effects.EffectCharge;
import org.mmocore.gameserver.skills.effects.EffectCharmOfCourage;
import org.mmocore.gameserver.skills.effects.EffectCombatPointHealOverTime;
import org.mmocore.gameserver.skills.effects.EffectConsumeSoulsOverTime;
import org.mmocore.gameserver.skills.effects.EffectCubic;
import org.mmocore.gameserver.skills.effects.EffectDamOverTime;
import org.mmocore.gameserver.skills.effects.EffectDamOverTimeLethal;
import org.mmocore.gameserver.skills.effects.EffectDebuffImmunity;
import org.mmocore.gameserver.skills.effects.EffectDestroySummon;
import org.mmocore.gameserver.skills.effects.EffectDisarm;
import org.mmocore.gameserver.skills.effects.EffectDiscord;
import org.mmocore.gameserver.skills.effects.EffectDispelEffects;
import org.mmocore.gameserver.skills.effects.EffectEnervation;
import org.mmocore.gameserver.skills.effects.EffectFakeDeath;
import org.mmocore.gameserver.skills.effects.EffectFear;
import org.mmocore.gameserver.skills.effects.EffectGrow;
import org.mmocore.gameserver.skills.effects.EffectHPDamPercent;
import org.mmocore.gameserver.skills.effects.EffectHate;
import org.mmocore.gameserver.skills.effects.EffectHeal;
import org.mmocore.gameserver.skills.effects.EffectHealBlock;
import org.mmocore.gameserver.skills.effects.EffectHealCPPercent;
import org.mmocore.gameserver.skills.effects.EffectHealOverTime;
import org.mmocore.gameserver.skills.effects.EffectHealPercent;
import org.mmocore.gameserver.skills.effects.EffectHourglass;
import org.mmocore.gameserver.skills.effects.EffectImmobilize;
import org.mmocore.gameserver.skills.effects.EffectInterrupt;
import org.mmocore.gameserver.skills.effects.EffectInvisible;
import org.mmocore.gameserver.skills.effects.EffectInvulnerable;
import org.mmocore.gameserver.skills.effects.EffectLDManaDamOverTime;
import org.mmocore.gameserver.skills.effects.EffectLockInventory;
import org.mmocore.gameserver.skills.effects.EffectMPDamPercent;
import org.mmocore.gameserver.skills.effects.EffectManaDamOverTime;
import org.mmocore.gameserver.skills.effects.EffectManaHeal;
import org.mmocore.gameserver.skills.effects.EffectManaHealOverTime;
import org.mmocore.gameserver.skills.effects.EffectManaHealPercent;
import org.mmocore.gameserver.skills.effects.EffectMeditation;
import org.mmocore.gameserver.skills.effects.EffectMute;
import org.mmocore.gameserver.skills.effects.EffectMuteAll;
import org.mmocore.gameserver.skills.effects.EffectMuteAttack;
import org.mmocore.gameserver.skills.effects.EffectMutePhisycal;
import org.mmocore.gameserver.skills.effects.EffectNegateEffects;
import org.mmocore.gameserver.skills.effects.EffectNegateMusic;
import org.mmocore.gameserver.skills.effects.EffectParalyze;
import org.mmocore.gameserver.skills.effects.EffectPetrification;
import org.mmocore.gameserver.skills.effects.EffectRandomHate;
import org.mmocore.gameserver.skills.effects.EffectRelax;
import org.mmocore.gameserver.skills.effects.EffectRemoveTarget;
import org.mmocore.gameserver.skills.effects.EffectRoot;
import org.mmocore.gameserver.skills.effects.EffectSalvation;
import org.mmocore.gameserver.skills.effects.EffectServitorShare;
import org.mmocore.gameserver.skills.effects.EffectSilentMove;
import org.mmocore.gameserver.skills.effects.EffectSleep;
import org.mmocore.gameserver.skills.effects.EffectStun;
import org.mmocore.gameserver.skills.effects.EffectSymbol;
import org.mmocore.gameserver.skills.effects.EffectTemplate;
import org.mmocore.gameserver.skills.effects.EffectTransformation;
import org.mmocore.gameserver.skills.effects.EffectUnAggro;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.stats.Stats;

public enum EffectType
{
	// Основные эффекты
	AddSkills(EffectAddSkills.class, null, false),
	AgathionResurrect(EffectAgathionRes.class, null, true),
	Aggression(EffectAggression.class, null, true),
	Betray(EffectBetray.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	BlessNoblesse(EffectBlessNoblesse.class, null, true),
	BlockStat(EffectBlockStat.class, null, true),
	Buff(EffectBuff.class, null, false),
	Bluff(EffectBluff.class, AbnormalEffect.NULL, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	BuffImmunity(EffectBuffImmunity.class, null, false),
	DebuffImmunity(EffectDebuffImmunity.class, null, true),
	DispelEffects(EffectDispelEffects.class, null, true),
	CallSkills(EffectCallSkills.class, null, false),
	Cancel(EffectCancel.class, null, Stats.CANCEL_RESIST, Stats.CANCEL_POWER, true),
	CombatPointHealOverTime(EffectCombatPointHealOverTime.class, null, true),
	ConsumeSoulsOverTime(EffectConsumeSoulsOverTime.class, null, true),
	Charge(EffectCharge.class, null, false),
	CharmOfCourage(EffectCharmOfCourage.class, null, true),
	CPDamPercent(EffectCPDamPercent.class, null, true),
	Cubic(EffectCubic.class, null, true),
	DamOverTime(EffectDamOverTime.class, null, false),
	DamOverTimeLethal(EffectDamOverTimeLethal.class, null, false),
	DestroySummon(EffectDestroySummon.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Disarm(EffectDisarm.class, null, true),
	Discord(EffectDiscord.class, AbnormalEffect.CONFUSED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Enervation(EffectEnervation.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, false),
	FakeDeath(EffectFakeDeath.class, null, true),
	Fear(EffectFear.class, AbnormalEffect.SOUL_SHOCK, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Grow(EffectGrow.class, AbnormalEffect.GROW, false),
	Hate(EffectHate.class, null, false),
	Heal(EffectHeal.class, null, false),
	HealBlock(EffectHealBlock.class, null, true),
	HealCPPercent(EffectHealCPPercent.class, null, true),
	HealOverTime(EffectHealOverTime.class, null, false),
	HealPercent(EffectHealPercent.class, null, false),
	HPDamPercent(EffectHPDamPercent.class, null, true),
	IgnoreSkill(EffectBuff.class, null, false),
	Immobilize(EffectImmobilize.class, null, true),
	Interrupt(EffectInterrupt.class, null, true),
	Invulnerable(EffectInvulnerable.class, null, false),
	Invisible(EffectInvisible.class, null, false),
	LockInventory(EffectLockInventory.class, null, false),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, null, true),
	ManaDamOverTime(EffectManaDamOverTime.class, null, true),
	ManaHeal(EffectManaHeal.class, null, false),
	ManaHealOverTime(EffectManaHealOverTime.class, null, false),
	ManaHealPercent(EffectManaHealPercent.class, null, false),
	Meditation(EffectMeditation.class, null, false),
	MPDamPercent(EffectMPDamPercent.class, null, true),
	Mute(EffectMute.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MuteAll(EffectMuteAll.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MuteAttack(EffectMuteAttack.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MutePhisycal(EffectMutePhisycal.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	NegateEffects(EffectNegateEffects.class, null, false),
	NegateMusic(EffectNegateMusic.class, null, false),
	Paralyze(EffectParalyze.class, AbnormalEffect.HOLD_1, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	Petrification(EffectPetrification.class, AbnormalEffect.HOLD_2, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	RandomHate(EffectRandomHate.class, null, true),
	Relax(EffectRelax.class, null, true),
	RemoveTarget(EffectRemoveTarget.class, null, true),
	Root(EffectRoot.class, AbnormalEffect.ROOT, Stats.ROOT_RESIST, Stats.ROOT_POWER, true),
	Hourglass(EffectHourglass.class, null, true),
	Salvation(EffectSalvation.class, null, true),
	ServitorShare(EffectServitorShare.class, null, true),
	SilentMove(EffectSilentMove.class, AbnormalEffect.STEALTH, true),
	Sleep(EffectSleep.class, AbnormalEffect.SLEEP, Stats.SLEEP_RESIST, Stats.SLEEP_POWER, true),
	Stun(EffectStun.class, AbnormalEffect.STUN, Stats.STUN_RESIST, Stats.STUN_POWER, true),
	Symbol(EffectSymbol.class, null, false),
	Transformation(EffectTransformation.class, null, true),
	UnAggro(EffectUnAggro.class, null, true),
	Vitality(EffectBuff.class, AbnormalEffect.VITALITY, true),

	// Производные от основных эффектов
	Poison(EffectDamOverTime.class, null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	PoisonLethal(EffectDamOverTimeLethal.class, null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	Bleed(EffectDamOverTime.class, null, Stats.BLEED_RESIST, Stats.BLEED_POWER, false),
	Debuff(EffectBuff.class, null, false),
	WatcherGaze(EffectBuff.class, null, false),

	AbsorbDamageToEffector(EffectBuff.class, null, false), // абсорбирует часть дамага к еффектора еффекта
	AbsorbDamageToMp(EffectBuff.class, null, false), // абсорбирует часть дамага в мп
	AbsorbDamageToSummon(EffectLDManaDamOverTime.class, null, true); // абсорбирует часть дамага к сумону

	private final Constructor<? extends Effect> _constructor;
	private final AbnormalEffect _abnormal;
	private final Stats _resistType;
	private final Stats _attributeType;
	private final boolean _isRaidImmune;

	private EffectType(Class<? extends Effect> clazz, AbnormalEffect abnormal, boolean isRaidImmune)
	{
		this(clazz, abnormal, null, null, isRaidImmune);
	}

	private EffectType(Class<? extends Effect> clazz, AbnormalEffect abnormal, Stats resistType, Stats attributeType, boolean isRaidImmune)
	{
		try
		{
			_constructor = clazz.getConstructor(Env.class, EffectTemplate.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
		_abnormal = abnormal;
		_resistType = resistType;
		_attributeType = attributeType;
		_isRaidImmune = isRaidImmune;
	}

	public AbnormalEffect getAbnormal()
	{
		return _abnormal;
	}

	public Stats getResistType()
	{
		return _resistType;
	}

	public Stats getAttributeType()
	{
		return _attributeType;
	}

	public boolean isRaidImmune()
	{
		return _isRaidImmune;
	}

	public Effect makeEffect(Env env, EffectTemplate template) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		return _constructor.newInstance(env, template);
	}
}