package org.mmocore.gameserver.network.authcomm.as2gs;

import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.AccountBonusDAO;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.actor.instances.player.Bonus;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.authcomm.ReceivablePacket;
import org.mmocore.gameserver.network.authcomm.SessionKey;
import org.mmocore.gameserver.network.authcomm.gs2as.PlayerInGame;
import org.mmocore.gameserver.network.l2.GameClient;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.CharacterSelectionInfo;
import org.mmocore.gameserver.network.l2.s2c.LoginFail;
import org.mmocore.gameserver.network.l2.s2c.ServerClose;

public class PlayerAuthResponse extends ReceivablePacket
{
	private String account;
	private boolean authed;
	private int playOkId1;
	private int playOkId2;
	private int loginOkId1;
	private int loginOkId2;
	private double bonus;
	private int bonusExpire;

	@Override
	public void readImpl()
	{
		account = readS();
		authed = readC() == 1;
		if(authed)
		{
			playOkId1 = readD();
			playOkId2 = readD();
			loginOkId1 = readD();
			loginOkId2 = readD();
			bonus = readF();
			bonusExpire = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			return;

		if(authed && client.getSessionKey().equals(skey))
		{
			client.setAuthed(true);
			client.setState(GameClient.GameClientState.AUTHED);
			client.startPingTask();
			switch(Config.SERVICES_RATE_TYPE)
			{
				case Bonus.NO_BONUS:
					bonus = 1.;
					bonusExpire = 0;
					break;
				case Bonus.BONUS_GLOBAL_ON_GAMESERVER:
					double[] bonuses = AccountBonusDAO.getInstance().select(account);
					bonus = bonuses[0];
					bonusExpire = (int)bonuses[1];
					break;
			}
			client.setBonus(bonus);
			client.setBonusExpire(bonusExpire);

			GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
			if(oldClient != null)
			{
				oldClient.setAuthed(false);
				Player activeChar = oldClient.getActiveChar();
				if(activeChar != null)
				{
					//FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
					activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);

					oldClient.setActiveChar(null);
					activeChar.setNetConnection(null);
					activeChar.scheduleDelete();
				}
				oldClient.close(ServerClose.STATIC);
			}

			sendPacket(new PlayerInGame(client.getLogin()));

			CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
			client.setCharSelection(csi.getCharInfo());
			client.sendPacket(csi);
		}
		else
		{
			client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
		}
	}
}