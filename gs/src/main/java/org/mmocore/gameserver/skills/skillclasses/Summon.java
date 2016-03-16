package org.mmocore.gameserver.skills.skillclasses;

import java.util.List;

import org.mmocore.gameserver.ThreadPoolManager;
import org.mmocore.gameserver.dao.SummonEffectDAO;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.GameObjectTasks;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.World;
import org.mmocore.gameserver.model.base.Experience;
import org.mmocore.gameserver.model.entity.events.impl.SiegeEvent;
import org.mmocore.gameserver.model.instances.MerchantInstance;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.instances.SummonInstance;
import org.mmocore.gameserver.model.instances.TrapInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.templates.StatsSet;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;
import org.mmocore.gameserver.utils.NpcUtils;


public class Summon extends Skill
{
	private final SummonType _summonType;

	private final double _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;
	private final int _minRadius;

	private static enum SummonType
	{
		PET,
		SIEGE_SUMMON,
		AGATHION,
		TRAP,
		MERCHANT,
		NPC
	}

	public Summon(StatsSet set)
	{
		super(set);

		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getDouble("expPenalty", 0.f);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
		_minRadius = set.getInteger("minRadius", 0);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(player == null)
			return false;

		if((_summonType == SummonType.PET || _summonType == SummonType.SIEGE_SUMMON || _summonType == SummonType.MERCHANT) && player.isProcessingRequest())
		{
			player.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		switch(_summonType)
		{
			case TRAP:
				if(player.isInZonePeace())
				{
					activeChar.sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
					return false;
				}
				break;
			case PET:
			case SIEGE_SUMMON:
				if(player.getServitor() != null || player.isMounted())
				{
					player.sendPacket(SystemMsg.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
				break;
			case AGATHION:
				if(player.getAgathionId() > 0 && _npcId != 0)
				{
					player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
					return false;
				}
			case NPC:
				if (_minRadius > 0)
					for (NpcInstance npc : World.getAroundNpc(player, _minRadius, 200))
						if (npc != null && npc.getNpcId() == getNpcId())
						{
							player.sendPacket(new SystemMessage(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
							return false;
						}
		}

		return super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(SkillEntry skillEntry, Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();

		switch(_summonType)
		{
			case AGATHION:
				activeChar.setAgathion(getNpcId());
				break;
			case TRAP:
				SkillEntry trapSkill = getFirstAddedSkill();

				if(activeChar.getTraps().size() >= 5)
				{
					TrapInstance trap = activeChar.getTraps().get(0);
					trap.deleteMe();
				}
				TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkill);
				activeChar.addTrap(trap);
				trap.spawnMe();
				break;
			case PET:
			case SIEGE_SUMMON:
				// Удаление трупа, если идет суммон из трупа.
				Location loc = null;
				if(_targetType == SkillTargetType.TARGET_CORPSE)
					for(Creature target : targets)
						if(target != null && target.isDead())
						{
							activeChar.getAI().setAttackTarget(null);
							loc = target.getLoc();
							if (target.isNpc())
								((NpcInstance) target).endDecayTask();
							else if (target.isSummon())
								((SummonInstance) target).endDecayTask();
							else
								return; // кто труп ?
						}

				if(activeChar.getServitor() != null || activeChar.isMounted())
					return;

				NpcTemplate summonTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, getId());
				activeChar.setServitor(summon);

				summon.setExpPenalty(_expPenalty);
				summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
				summon.setHeading(activeChar.getHeading());
				summon.setReflection(activeChar.getReflection());
				summon.spawnMe(loc == null ? Location.findAroundPosition(activeChar, 50, 70) : loc);
				summon.setRunning();
				summon.setFollowMode(true);

				if(summon.getSkillLevel(4140) > 0)
					summon.altUseSkill(SkillTable.getInstance().getSkillEntry(4140, summon.getSkillLevel(4140)), activeChar);

				SummonEffectDAO.getInstance().select(summon);
				if(activeChar.isInOlympiadMode())
					summon.getEffectList().stopAllEffects();

				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);

				if(_summonType == SummonType.SIEGE_SUMMON)
				{
					summon.setSiegeSummon(true);

					SiegeEvent<?,?> siegeEvent = activeChar.getEvent(SiegeEvent.class);

					siegeEvent.addSiegeSummon(activeChar, summon);
				}

				activeChar.getListeners().onSummonServitor(summon);
				break;
			case MERCHANT:
				if(activeChar.getServitor() != null || activeChar.isMounted())
					return;

				NpcTemplate merchantTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				MerchantInstance merchant = new MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);

				merchant.setCurrentHp(merchant.getMaxHp(), false);
				merchant.setCurrentMp(merchant.getMaxMp());
				merchant.setHeading(activeChar.getHeading());
				merchant.setReflection(activeChar.getReflection());
				merchant.spawnMe(activeChar.getLoc());

				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(merchant), _lifeTime);
				break;
			case NPC:
				NpcUtils.spawnSingle(getNpcId(), activeChar.getLoc(), activeChar.getReflection(), _lifeTime, activeChar.getName());
				break;
		}

		if(isSSPossible())
			caster.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return _targetType == SkillTargetType.TARGET_CORPSE;
	}
}