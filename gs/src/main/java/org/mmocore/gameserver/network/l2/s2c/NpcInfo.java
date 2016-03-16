package org.mmocore.gameserver.network.l2.s2c;


import org.apache.commons.lang3.StringUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.data.xml.holder.NpcHolder;
import org.mmocore.gameserver.model.Creature;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Servitor;
import org.mmocore.gameserver.model.base.TeamType;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.pledge.Alliance;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.network.l2.components.NpcString;
import org.mmocore.gameserver.skills.AbnormalEffectType;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.Location;

/**
 * @reworked VISTALL
 */
public class NpcInfo extends L2GameServerPacket
{
	private boolean can_writeImpl = false;
	private int _npcObjId, _npcId, running, incombat, dead, _showSpawnAnimation;
	private int _runSpd, _walkSpd, _mAtkSpd, _pAtkSpd, _rhand, _lhand, _enchantEffect;
	private int karma, pvp_flag, _abnormalEffect, _abnormalEffect2, clan_id, clan_crest_id, ally_id, ally_crest_id, _formId;
	private double colHeight, colRadius, currentColHeight, currentColRadius;
	private boolean _isAttackable, _isNameAbove, isFlying;
	private Location _loc;
	private String _name = StringUtils.EMPTY;
	private String _title = StringUtils.EMPTY;
	private boolean _showName, _targetable;
	private int _state;
	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;
	private TeamType _team;
	private boolean _isPet;

	public NpcInfo(NpcInstance cha, Creature attacker)
	{
		_npcId = cha.getDisplayId() != 0 ? cha.getDisplayId() : cha.getTemplate().npcId;
		_isAttackable = attacker != null && cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_enchantEffect = 0;
		if(Config.SERVER_SIDE_NPC_NAME || cha.getTemplate().displayId != 0 || cha.getName() != cha.getTemplate().name)
			_name = cha.getName();
		if(Config.SERVER_SIDE_NPC_TITLE || cha.getTemplate().displayId != 0 || cha.getTitle() != cha.getTemplate().title)
			_title = cha.getTitle();

		_showSpawnAnimation = cha.getSpawnAnimation();
		_showName = cha.isShowName();
		_state = cha.getNpcState();
		_nameNpcString = cha.getNameNpcString();
		_titleNpcString = cha.getTitleNpcString();
		_isPet = false;

		common(cha);
	}

	public NpcInfo(Servitor cha, Creature attacker)
	{
		if(cha.getPlayer() != null && cha.getPlayer().isInvisible())
			return;

		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = 0;
		_lhand = 0;
		_enchantEffect = 0;
		_showName = true;
		_name = cha.getName();
		_title = cha.getTitle();
		_showSpawnAnimation = cha.getSpawnAnimation();
		_isPet = true;

		common(cha);
	}

	public NpcInfo(Player player)
	{
		if(player.isInvisible())
			return;

		_npcId = player.getPolyId();
		NpcTemplate template = NpcHolder.getInstance().getTemplate(_npcId);

		_isAttackable = false;
		_enchantEffect = 0;
		_showName = true;
		_name = player.getName();
		_title = player.getTitle();
		_showSpawnAnimation = 0;

		//
		Clan clan = player.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

		colHeight = template.collisionHeight;
		colRadius = template.collisionRadius;
		currentColHeight = colHeight;
		currentColRadius = colRadius;

		_rhand = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
		_lhand = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND);

		_npcObjId = player.getObjectId();
		_loc = player.getLoc();
		_mAtkSpd = player.getMAtkSpd();

		_runSpd = player.getRunSpeed();
		_walkSpd = player.getWalkSpeed();
		karma = player.getKarma();
		pvp_flag = player.getPvpFlag();
		_pAtkSpd = player.getPAtkSpd(true);
		running = player.isRunning() ? 1 : 0;
		incombat = player.isInCombat() ? 1 : 0;
		dead = player.isAlikeDead() ? 1 : 0;
		_abnormalEffect = player.getAbnormalEffect(AbnormalEffectType.FIRST);
		_abnormalEffect2 = player.getAbnormalEffect(AbnormalEffectType.SECOND);
		isFlying = player.isFlying();
		_team = player.getTeam();
		_formId = player.getFormId();
		_isNameAbove = player.isNameAbove();
		_isPet = false;

		can_writeImpl = true;
	}

	private void common(Creature cha)
	{
		colHeight = cha.getTemplate().collisionHeight;
		colRadius = cha.getTemplate().collisionRadius;
		currentColHeight = cha.getColHeight();
		currentColRadius = cha.getColRadius();
		_npcObjId = cha.getObjectId();
		_loc = cha.getLoc();
		_mAtkSpd = cha.getMAtkSpd();
		//
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

		_runSpd = cha.getRunSpeed();
		_walkSpd = cha.getWalkSpeed();
		karma = cha.getKarma();
		pvp_flag = cha.getPvpFlag();
		_pAtkSpd = cha.getPAtkSpd(true);
		running = cha.isRunning() ? 1 : 0;
		incombat = cha.isInCombat() ? 1 : 0;
		dead = cha.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha.getAbnormalEffect(AbnormalEffectType.FIRST);
		_abnormalEffect2 = cha.getAbnormalEffect(AbnormalEffectType.SECOND);
		isFlying = cha.isFlying();
		_team = cha.getTeam();
		_formId = cha.getFormId();
		_isNameAbove = cha.isNameAbove();
		_targetable = cha.isTargetable();

		can_writeImpl = true;
	}

	public NpcInfo update()
	{
		_showSpawnAnimation = 1;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;

		writeC(0x0c);
		//ddddddddddddddddddffffdddcccccSSddddddddccffddddccd
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd /*_swimRunSpd*//*0x32*/); // swimspeed
		writeD(_walkSpd/*_swimWalkSpd*//*0x32*/); // swimspeed
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(1.100000023841858); // взято из клиента
		writeF(_pAtkSpd / 277.478340719);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0); //TODO chest
		writeD(_lhand); // left hand weapon
		writeC(_isNameAbove ? 1 : 0); // 2.2: name above char 1=true ... ??; 2.3: 1 - normal, 2 - dead
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(_nameNpcString.getId());
		writeS(_name);
		writeD(_titleNpcString.getId());
		writeS(_title);
		writeD(_isPet); // 0- светло зеленый титул(моб), 1 - светло синий(пет)/отображение текущего МП
		writeD(pvp_flag);
		writeD(karma);
		writeD(_abnormalEffect); // C2
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(isFlying ? 2 : 0); // C2
		writeC(_team.ordinal()); // team aura 1-blue, 2-red
		writeF(currentColRadius); // тут что-то связанное с colRadius
		writeF(currentColHeight); // тут что-то связанное с colHeight
		writeD(_enchantEffect); // C4
		writeD(0x00); // writeD(_npc.isFlying() ? 1 : 0); // C6
		writeD(0x00);
		writeD(_formId);// great wolf type
		writeC(_targetable ? 0x01 : 0x00); // targetable
		writeC(_showName ? 0x01 : 0x00); // show name
		writeD(_abnormalEffect2);
		writeD(_state);
	}
}