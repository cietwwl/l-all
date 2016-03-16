package org.mmocore.gameserver.network.l2.s2c;

public class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private String _requestor;
	private int _mode;

	public ExAskModifyPartyLooting(String name, int mode)
	{
		_requestor = name;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xBF);
		writeS(_requestor);
		writeD(_mode);
	}
}
