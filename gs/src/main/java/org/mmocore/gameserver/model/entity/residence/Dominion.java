package org.mmocore.gameserver.model.entity.residence;

import org.mmocore.commons.dao.JdbcEntityState;
import org.mmocore.gameserver.dao.DominionDAO;
import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.templates.StatsSet;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

/**
 * @author VISTALL
 * @date 15:15/14.02.2011
 */
public class Dominion extends Residence
{
	private IntSet _flags = new TreeIntSet();
	private Castle _castle;
	private int _lordObjectId;

	public Dominion(StatsSet set)
	{
		super(set);
	}

	@Override
	public void init()
	{
		initEvent();

		_castle = ResidenceHolder.getInstance().getResidence(Castle.class, getId() - 80);
		_castle.setDominion(this);

		loadData();

		_siegeDate.setTimeInMillis(0);
	}

	@Override
	public void rewardSkills()
	{
		Clan owner = getOwner();
		if(owner != null)
		{
			if(!_flags.contains(getId()))
				return;

			for(int dominionId : _flags.toArray())
			{
				Dominion dominion = ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
				for(SkillEntry skill : dominion.getSkills())
				{
					owner.addSkill(skill, false);
					owner.broadcastToOnlineMembers(new SystemMessage(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));
				}
			}
		}
	}

	@Override
	public void removeSkills()
	{
		Clan owner = getOwner();
		if(owner != null)
		{
			for(int dominionId : _flags.toArray())
			{
				Dominion dominion = ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
				for(SkillEntry skill : dominion.getSkills())
					owner.removeSkill(skill.getId());
			}
		}
	}

	public void addFlag(int dominionId)
	{
		_flags.add(dominionId);
	}

	public void removeFlag(int dominionId)
	{
		_flags.remove(dominionId);
	}

	public int[] getFlags()
	{
		return _flags.toArray();
	}

	@Override
	protected void loadData()
	{
		DominionDAO.getInstance().select(this);
	}

	@Override
	public void changeOwner(Clan clan)
	{
		int newLordObjectId;
		if(clan == null)
		{
			if(_lordObjectId > 0)
				newLordObjectId = 0;
			else
				return;
		}
		else
		{
			newLordObjectId = clan.getLeaderId();

			// разсылаем мессагу
			SystemMessage message = new SystemMessage(SystemMsg.CLAN_LORD_C2_WHO_LEADS_CLAN_S1_HAS_BEEN_DECLARED_THE_LORD_OF_THE_S3_TERRITORY).addName(clan.getLeader().getPlayer()).addString(clan.getName()).addResidenceName(getCastle());
			for(Player player : GameObjectsStorage.getPlayers())
				player.sendPacket(message);
		}

		_lordObjectId = newLordObjectId;

		setJdbcState(JdbcEntityState.UPDATED);
		update();

		// обновляем значки в нпц которые принадлежат територии
		for(NpcInstance npc : GameObjectsStorage.getNpcs())
			if(npc.getDominion() == this)
				npc.broadcastCharInfoImpl();
	}

	public int getLordObjectId()
	{
		return _lordObjectId;
	}

	@Override
	public Clan getOwner()
	{
		return _castle.getOwner();
	}

	@Override
	public int getOwnerId()
	{
		return _castle.getOwnerId();
	}

	public Castle getCastle()
	{
		return _castle;
	}

	@Override
	public void update()
	{
		DominionDAO.getInstance().update(this);
	}

	public void setLordObjectId(int lordObjectId)
	{
		_lordObjectId = lordObjectId;
	}
}
