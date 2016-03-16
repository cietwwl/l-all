package org.mmocore.gameserver.network.l2.c2s;

import org.mmocore.commons.lang.ArrayUtils;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.CharacterAccessDAO;
import org.mmocore.gameserver.model.CharSelectInfo;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.s2c.Ex2ndPasswordAck;

/**
 * @author VISTALL
 */
public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private int _type;
	private String _password, _newPassword;
	
	@Override
	protected void readImpl()
	{
		_type = readC();
		_password = readS();
		if(_type == 2)
			_newPassword = readS();
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

		switch(_type)
		{
			case 0:
				if(csi.getPassword() != null)
					return;

				if(_password.length() < 6 || _password.length() > 8)
				{
					client.sendPacket(new Ex2ndPasswordAck(_type, Ex2ndPasswordAck.WRONG_PATTERN));
					return;
				}

				csi.setPassword(_password);
				client.sendPacket(new Ex2ndPasswordAck(_type, Ex2ndPasswordAck.SUCCESS));

				CharacterAccessDAO.getInstance().update(csi.getObjectId(), _password);
				break;
			case 2:
				if(csi.getPassword() == null)
					return;

				if(!csi.getPassword().equals(_password))
				{
					client.sendPacket(new Ex2ndPasswordAck(_type, Ex2ndPasswordAck.WRONG_PATTERN));
					return;
				}

				if(_newPassword.length() < 6 || _newPassword.length() > 8)
				{
					client.sendPacket(new Ex2ndPasswordAck(_type, Ex2ndPasswordAck.WRONG_PATTERN));
					return;
				}

				csi.setPassword(_newPassword);
				client.sendPacket(new Ex2ndPasswordAck(_type, Ex2ndPasswordAck.SUCCESS));

				CharacterAccessDAO.getInstance().update(csi.getObjectId(), _newPassword);
				break;
		}
	}
}
