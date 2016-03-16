package org.mmocore.gameserver.network.l2.s2c;

import org.mmocore.gameserver.templates.Henna;
import org.mmocore.gameserver.model.Player;

//ccccccdd[dd]
public class GMHennaInfo extends L2GameServerPacket
{
	private int _count, _str, _con, _dex, _int, _wit, _men;
	private final Henna[] _hennas = new Henna[3];

	public GMHennaInfo(final Player cha)
	{
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();

		int j = 0;
		for(int i = 0; i < 3; i++)
		{
			Henna h = cha.getHenna(i + 1);
			if(h != null)
				_hennas[j++] = h;
		}
		_count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf0);

		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}
}