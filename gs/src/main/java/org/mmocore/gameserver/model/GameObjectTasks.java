package org.mmocore.gameserver.model;

import java.util.List;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.commons.threading.RunnableImpl;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.geodata.GeoEngine;
import org.mmocore.gameserver.instancemanager.ReflectionManager;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ExVoteSystemInfo;
import org.mmocore.gameserver.network.l2.s2c.MagicSkillLaunched;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;

public class GameObjectTasks
{
	public static class DeleteTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _ref;

		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}

		@Override
		public void runImpl()
		{
			Creature c = _ref.get();

			if(c != null)
				c.deleteMe();
		}
	}

	// ============================ Таски для L2Player ==============================
	public static class SoulConsumeTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public SoulConsumeTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setConsumedSouls(player.getConsumedSouls() + 1, null);
		}
	}

	/** PvPFlagTask */
	public static class PvPFlagTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
			if(diff > Config.PVP_TIME)
				player.stopPvPFlag();
			else if(diff > Config.PVP_TIME - 20000)
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}

	/** HourlyTask */
	public static class HourlyTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			// Каждый час в игре оповещаем персонажу сколько часов он играет.
			int hoursInGame = player.getHoursInGame();
			player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK_S1).addNumber(hoursInGame));
			player.sendPacket(new SystemMessage(SystemMsg.YOU_OBTAINED_S1_RECOMMENDATIONS).addNumber(player.addRecomLeft()));
		}
	}

	/** RecomBonusTask */
	public static class RecomBonusTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public RecomBonusTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setRecomBonusTime(0);
			player.sendPacket(new ExVoteSystemInfo(player));
		}
	}

	/** WaterTask */
	public static class WaterTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}

			double reduceHp = player.getMaxHp() < 100 ? 1 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, 0, false, false, false, true, false, false, false, false);
			player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_TAKEN_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE).addNumber((long) reduceHp));
		}
	}

	/** KickTask */
	public static class KickTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setOfflineMode(false);
			player.kick();
		}
	}

	/** UnJailTask */
	public static class UnJailTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public UnJailTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.unblock();
			player.standUp();
			player.teleToLocation(17817, 170079, -3530, ReflectionManager.DEFAULT);
		}
	}

	/** EndSitDownTask */
	public static class EndSitDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}

	/** EndStandUpTask */
	public static class EndStandUpTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.setSitting(false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	// ============================ Таски для L2Character ==============================

	/** AltMagicUseTask */
	public static class AltMagicUseTask extends RunnableImpl
	{
		public final SkillEntry _skill;
		private final HardReference<? extends Creature> _charRef, _targetRef;

		public AltMagicUseTask(Creature character, Creature target, SkillEntry skill)
		{
			_charRef = character.getRef();
			_targetRef = target.getRef();
			_skill = skill;
		}

		@Override
		public void runImpl()
		{
			Creature cha, target;
			if((cha = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			cha.altOnMagicUseTimer(target, _skill);
		}
	}

	/** CastEndTimeTask */
	public static class CastEndTimeTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		private final SkillEntry _skill;

		public CastEndTimeTask(Creature character, SkillEntry skill)
		{
			_charRef = character.getRef();
			_skill = skill;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			character.onCastEndTime(_skill);
		}
	}

	/** HitTask */
	public static class HitTask extends RunnableImpl
	{
		final boolean _crit, _miss, _shld, _soulshot, _unchargeSS, _notify;
		final int _damage, _poleHitCount;
		private final HardReference<? extends Creature> _charRef, _targetRef;

		public HitTask(Creature cha, Creature target, int damage, int poleHitCount, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_poleHitCount = poleHitCount;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
		}

		@Override
		public void runImpl()
		{
			Creature character, target;
			if((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;

			if(character.isAttackAborted())
				return;

			character.onHitTimer(target, _damage, _poleHitCount, _crit, _miss, _soulshot, _shld, _unchargeSS);

			if(_notify)
				character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
		}
	}

	/** Task launching the function onMagicUseTimer() */
	public static class MagicUseTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicUseTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			SkillEntry castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}

	/** MagicLaunchedTask */
	public static class MagicLaunchedTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicLaunchedTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			SkillEntry castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			List<Creature> targets = castingSkill.getTemplate().getTargets(character, castingTarget, _forceUse);
			character.broadcastPacket(new MagicSkillLaunched(character, castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets));
		}
	}

	public static class MagicGeoCheckTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;

		public MagicGeoCheckTask(Creature cha)
		{
			_charRef = cha.getRef();
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Creature castingTarget = character.getCastingTarget();
			if (castingTarget == null)
				return;
			if (!GeoEngine.canSeeTarget(character, castingTarget, character.isFlying()))
				return;

			character._skillGeoCheckTask = null;
		}
	}

	/** Task of AI notification */
	public static class NotifyAITask extends RunnableImpl
	{
		private final CtrlEvent _evt;
		private final Object _arg0;
		private final Object _arg1;
		private final Object _arg2;
		private final HardReference<? extends Creature> _charRef;

		public NotifyAITask(Creature cha, CtrlEvent evt, Object arg0, Object arg1, Object arg2)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = arg0;
			_arg1 = arg1;
			_arg2 = arg2;
		}

		public NotifyAITask(Creature cha, CtrlEvent evt, Object arg0, Object arg1)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = arg0;
			_arg1 = arg1;
			_arg2 = null;
		}

		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = null;
			_arg1 = null;
			_arg2 = null;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null || !character.hasAI())
				return;

			character.getAI().notifyEvent(_evt, _arg0, _arg1, _arg2);
		}
	}

	public static class MountFeedTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public MountFeedTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			final Player player = _playerRef.get();
			if (player == null)
				return;

			player.updateMountFed();
		}
	}
}