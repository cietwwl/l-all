package org.mmocore.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.data.xml.holder.CubicHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.model.GameObject;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillLaunched;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillUse;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.templates.CubicTemplate;

/**
 * @author VISTALL && pchayka
 * @date  13:17/22.12.2010
 * Каждую секунду задача кубика вызывает выбор скила из списка доступных. Если умение на откате (delay), выбирает следующее и отправляет в исполнение.
 * В обработке умения считается шанс запуска умения (info.getChance()), если скилл не удался - отмена обработки.
 * При успешной активации проходит обработка умения и оно запускается на откат (delay).
 */
public class EffectCubic extends Effect
{
	private class ActionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
				return;

			Player player = _effected != null && _effected.isPlayer() ? (Player) _effected : null;
			if(player == null)
				return;

			doAction(player);
		}
	}

	private final CubicTemplate _template;
	private Future<?> _task = null;
	private long _reuse = 0;

	public EffectCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		_template = CubicHolder.getInstance().getTemplate(getTemplate().getParam().getInteger("cubicId"), getTemplate().getParam().getInteger("cubicLevel"));
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player == null)
			return;

		player.addCubic(this);
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(), 1000L, 1000L);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = _effected.getPlayer();
		if(player == null)
			return;

		player.removeCubic(getId());
		_task.cancel(true);
		_task = null;
	}

	public void doAction(Player player)
	{
		if (_reuse > System.currentTimeMillis())
			return;

		boolean result = false;
		int chance = Rnd.get(100);
		for(Map.Entry<Integer, List<CubicTemplate.SkillInfo>> entry : _template.getSkills())
			if((chance -= entry.getKey()) < 0)
			{
				for(CubicTemplate.SkillInfo skillInfo : entry.getValue())
				{
					switch(skillInfo.getActionType())
					{
						case ATTACK:
							result = doAttack(player, skillInfo);
							break;
						case DEBUFF:
							result = doDebuff(player, skillInfo);
							break;
						case HEAL:
							result = doHeal(player, skillInfo);
							break;
						case CANCEL:
							result = doCancel(player, skillInfo);
							break;
					}
				}
				break;
			}
		if (result)
			_reuse = System.currentTimeMillis() + _template.getDelay() * 1000L;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean isCancelable()
	{
		return false;
	}

	public int getId()
	{
		return _template.getId();
	}

	private static boolean doHeal(final Player player, CubicTemplate.SkillInfo info)
	{
		final SkillEntry skill = info.getSkill();
		Creature target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentHpFull() && !player.isDead())
				target = player;
		}
		else
		{
			double currentHp = Integer.MAX_VALUE;
			for(Player member : player.getParty().getPartyMembers())
			{
				if(member == null)
					continue;

				if(player.isInRange(member, skill.getTemplate().getCastRange()) && !member.isCurrentHpFull() && !member.isDead() && member.getCurrentHp() < currentHp)
				{
					currentHp = member.getCurrentHp();
					target = member;
				}
			}
		}

		if(target == null)
			return false;

		int chance = info.getChance((int) target.getCurrentHpPercents());

		if(!Rnd.chance(chance))
			return false;

		final Creature aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getTemplate().getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getTemplate().getHitTime());
		return true;
	}

	private static boolean doAttack(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;

		Creature target = getTarget(player, info);
		if (target == null)
			return false;

		final Creature aimTarget = target;
		final SkillEntry skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getTemplate().getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);

				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);

				if(aimTarget.isNpc())
					if(aimTarget.paralizeOnAttack(player))
					{
						if(Config.PARALIZE_ON_RAID_DIFF)
							player.paralizeMe(aimTarget, Skill.SKILL_RAID_CURSE_MUTE);
					}
					else
					{
						int damage = skill.getTemplate().getEffectPoint() != 0 ? skill.getTemplate().getEffectPoint() : (int) skill.getTemplate().getPower();
						aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, skill, damage);
					}
			}
		}, skill.getTemplate().getHitTime());
		return true;
	}

	private static boolean doDebuff(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;

		Creature target = getTarget(player, info);
		if (target == null)
			return false;

		final Creature aimTarget = target;
		final SkillEntry skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getTemplate().getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getTemplate().getHitTime());
		return true;
	}

	private static boolean doCancel(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;

		boolean hasDebuff = false;
		for(Effect e : player.getEffectList().getAllEffects())
			if(e.isOffensive() && e.isCancelable() && !e.getTemplate()._applyOnCaster)
			{
				hasDebuff = true;
				break;
			}

		if(!hasDebuff)
			return false;

		final SkillEntry skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getTemplate().getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(player);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getTemplate().getHitTime());
		return true;
	}

	private static final Creature getTarget(final Player owner, final CubicTemplate.SkillInfo info)
	{
		if (!owner.isInCombat())
			return null;

		final GameObject object = owner.getTarget();
		if (object == null || !object.isCreature())
			return null;

		final Creature target = (Creature)object;
		if (target.isDead())
			return null;

		if (target.getCurrentHp() < info.getMinHp() && target.getCurrentHpPercents() < info.getMinHpPercent())
			return null;

		if ((target.isDoor() && !info.isCanAttackDoor()))
			return null;

		if (!owner.isInRangeZ(target, info.getSkill().getTemplate().getCastRange()))
			return null;

		Player targetPlayer = target.getPlayer();
		if (targetPlayer != null && !targetPlayer.isInCombat())
			return null;

		if (!target.isAutoAttackable(owner))
			return null;

		return target;
	}
}