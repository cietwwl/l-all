package org.mmocore.gameserver.model.base;

public class PlayerAccess
{
	public int PlayerID;

	public boolean IsGM = false;
	public boolean CanUseGMCommand = false;
	public boolean CanAnnounce = false;

	// Право банить чат
	public boolean CanBanChat = false;
	// Право снимать бан чата
	public boolean CanUnBanChat = false;
	// Право выдавать невидимый бан чата
	public boolean CanDisableChat = false;
	// Право видеть шауты и трейды всех регионов
	public boolean CanSeeAllShouts = false;

	public boolean CanCharBan = false;
	public boolean CanCharUnBan = false;
	public boolean CanBan = false;
	public boolean CanUnBan = false;
	public boolean CanTradeBanUnban = false;
	public boolean CanUseBanPanel = false;
	public boolean CanKick = false;
	public boolean CanJail = false;

	public boolean UseGMShop = false;
	public boolean CanDelete = false;

	public boolean Menu = false;
	public boolean GodMode = false;

	public boolean CanEditChar = false;
	public boolean CanChangeClass = false;
	public boolean CanRename = false;
	public boolean CanEditCharAll = false;
	public boolean CanEditPledge = false;
	public boolean CanViewChar = false;

	public boolean CanEditNPC = false;

	public boolean Door = false;

	public boolean CanTeleport = false;
	public boolean FastUnstuck = false;

	public boolean CanSnoop = false;
	public boolean MonsterRace = false;
	public boolean Rider = false;
	public boolean CanPolymorph = false;

	public boolean ResurectFixed = false;
	public boolean Res = false;
	public boolean Heal = false;

	public boolean IsEventGm = false;

	public boolean CanReload = false;
	public boolean CanRestart = false;

	public boolean CanUseMovingObservationPoint = false;

	public PlayerAccess()
	{}
}