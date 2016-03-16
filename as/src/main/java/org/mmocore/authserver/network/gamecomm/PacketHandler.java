package org.mmocore.authserver.network.gamecomm;

import java.nio.ByteBuffer;

import org.mmocore.authserver.network.gamecomm.gs2as.AuthRequest;
import org.mmocore.authserver.network.gamecomm.gs2as.BonusRequest;
import org.mmocore.authserver.network.gamecomm.gs2as.ChangeAccessLevel;
import org.mmocore.authserver.network.gamecomm.gs2as.OnlineStatus;
import org.mmocore.authserver.network.gamecomm.gs2as.PingResponse;
import org.mmocore.authserver.network.gamecomm.gs2as.PlayerAuthRequest;
import org.mmocore.authserver.network.gamecomm.gs2as.PlayerInGame;
import org.mmocore.authserver.network.gamecomm.gs2as.PlayerLogout;
import org.mmocore.authserver.network.gamecomm.gs2as.SetAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler
{
	private static Logger _log = LoggerFactory.getLogger(PacketHandler.class);

	public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf)
	{
		ReceivablePacket packet = null;

		int id = buf.get() & 0xff;

		if(!gs.isAuthed())
			switch(id)
			{
				case 0x00:
					packet = new AuthRequest();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}
		else
			switch(id)
			{
				case 0x01:
					packet = new OnlineStatus();
					break;
				case 0x02:
					packet = new PlayerAuthRequest();
					break;
				case 0x03:
					packet = new PlayerInGame();
					break;
				case 0x04:
					packet = new PlayerLogout();
					break;
				case 0x05:
					packet = new SetAccountInfo();
					break;
				case 0x10:
					packet = new BonusRequest();
					break;
				case 0x11:
					packet = new ChangeAccessLevel();
					break;
				case 0xff:
					packet = new PingResponse();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}

		return packet;
	}
}