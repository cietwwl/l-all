package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.model.CharSelectInfo;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.Ex2ndPasswordVerify;
import org.mmocore.gameserver.skills.TimeStamp;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author VISTALL
 */
public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
	protected static IntObjectMap<TimeStamp> _banInfo = new HashIntObjectMap<TimeStamp>();

	private String _password;

	@Override
	protected void readImpl()
	{
		_password = readS();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.EX_2ND_PASSWORD_CHECK)
			return;

		GameClient client = getClient();

		CharSelectInfo csi = ArrayUtils.valid(client.getCharacters(), client.getSelectedIndex());
		if(csi == null)
			return;

		TimeStamp info = _banInfo.get(csi.getObjectId());
		if(info != null && info.getEndTime() > 0 && info.hasNotPassed())
		{
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN, info.getLevel()));
			return;
		}

		if(csi.getPassword().equals(_password))
		{
			_banInfo.remove(csi.getObjectId());

			csi.setPasswordChecked(true);
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_OK, 0));
			client.playerSelected(client.getSelectedIndex());
		}
		else
		{
			info = info == null ? new TimeStamp(csi.getObjectId(), 0, 0) : info;
			if(info.getLevel() == 0)
				_banInfo.put(csi.getObjectId(), info);

			info.setLevel(info.getLevel() + 1);

			if(info.getLevel() >= 5)
			{
				info.setEndTime(System.currentTimeMillis() + 691200000L);
				client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN, info.getLevel()));
			}
			else
				client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_WRONG, info.getLevel()));
		}
	}
}
