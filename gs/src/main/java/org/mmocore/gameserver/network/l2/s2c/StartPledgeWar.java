package org.mmocore.gameserver.network.l2.s2c;

public class StartPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _char;

	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x63);
		writeS(_char);
		writeS(_pledgeName);
	}
}