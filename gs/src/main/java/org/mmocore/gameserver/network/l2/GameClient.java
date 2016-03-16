package org.mmocore.gameserver.network.l2;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;

import org.mmocore.commons.net.nio.impl.MMOClient;
import org.mmocore.commons.net.nio.impl.MMOConnection;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.GameServer;
import org.mmocore.gameserver.dao.CharacterDAO;
import org.mmocore.gameserver.data.client.holder.NpcNameLineHolder;
import org.mmocore.gameserver.model.CharSelectInfo;
import org.mmocore.gameserver.model.GameObjectsStorage;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.authcomm.SessionKey;
import org.mmocore.gameserver.network.authcomm.gs2as.PlayerLogout;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.ActionFail;
import org.mmocore.gameserver.network.l2.s2c.CharSelected;
import org.mmocore.gameserver.network.l2.s2c.Ex2ndPasswordCheck;
import org.mmocore.gameserver.network.l2.s2c.ExNeedToChangeName;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.NetPing;
import org.mmocore.gameserver.taskmanager.LazyPrecisionTaskManager;
import org.mmocore.gameserver.utils.AutoBan;
import org.mmocore.gameserver.utils.Language;
import org.mmocore.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a client connected on Game Server
 */
public class GameClient extends MMOClient<MMOConnection<GameClient>>
{
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class);

	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		ENTER_GAME,
		IN_GAME,
		DISCONNECTED
	}

	private GameClientState _state;
	private GameCrypt _crypt;
	private String _ip;

	private SessionKey _sessionKey;

	/** Данные аккаунта */
	private String _login;

	private double _bonus = 1.0;
	private int _bonusExpire;

	private int revision = 0;

	private int _selectedIndex = -1;
	private Language _language = Language.RUSSIAN;
	private CharSelectInfo[] _csi = new CharSelectInfo[7];

	private Future<?> _pingTask;
	private int _pingTryCount;
	private int _pingTime;

	private int _failedPackets;
	private int _unknownPackets;

	private Player _activeChar;

	protected GameClient(MMOConnection<GameClient> con)
	{
		super(con);

		_state = GameClientState.CONNECTED;
		_crypt = new GameCrypt();
		_ip = con.getSocket().getInetAddress().getHostAddress();
	}

	@Override
	protected void onDisconnection()
	{
		final Player player;

		setState(GameClientState.DISCONNECTED);

		stopPingTask();

		player = getActiveChar();
		setActiveChar(null);

		if(player != null)
		{
			player.setNetConnection(null);
			player.scheduleDelete();
		}

		if(getSessionKey() != null)
		{
			if(isAuthed())
			{
				AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
				AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
			}
			else
			{
				AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
			}
		}
	}

	@Override
	protected void onForcedDisconnection()
	{
		// TODO Auto-generated method stub

	}

	public void startPingTask()
	{
		_pingTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new Runnable(){
			@Override
			public void run()
			{
				if(++_pingTryCount > 2)
					closeNow(true);
				else
				{
					int time = (int) (System.currentTimeMillis() - GameServer.getInstance().getServerStartTime());
					sendPacket(new NetPing(time));
				}
			}
		}, 30000L, 30000L);
	}

	public void onNetPing(int time)
	{
		_pingTryCount--;
		_pingTime = (int) (System.currentTimeMillis() - GameServer.getInstance().getServerStartTime() - time);
		_pingTime /= 2;
	}

	public int getPing()
	{
		return _pingTime;
	}

	public void stopPingTask()
	{
		if(_pingTask != null)
		{
			_pingTask.cancel(false);
			_pingTask = null;
		}
	}

	public int getObjectIdByIndex(int charslot)
	{
		if(charslot < 0 || charslot >= _csi.length)
		{
			_log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		CharSelectInfo p = _csi[charslot];
		return p == null ? 0 : p.getObjectId();
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLoginName(String loginName)
	{
		_login = loginName;
	}

	public void setActiveChar(Player player)
	{
		_activeChar = player;
		if(player != null)
			player.setNetConnection(this);
	}

	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public void setCharSelection(CharSelectInfo[] chars)
	{
		_csi = chars;
	}

	public int getRevision()
	{
		return revision;
	}

	public void setRevision(int revision)
	{
		this.revision = revision;
	}

	public void playerSelected(int index)
	{
		int objectIdByIndex = getObjectIdByIndex(index);
		if(objectIdByIndex <= 0 || getActiveChar() != null || AutoBan.isBanned(objectIdByIndex))
		{
			sendPacket(ActionFail.STATIC);
			return;
		}

		_selectedIndex = index;
		CharSelectInfo info = _csi[index];
		if(info == null)
			return;

		if(Config.EX_CHANGE_NAME_DIALOG)
		{
			if(!Util.isMatchingRegexp(info.getName(), Config.CNAME_TEMPLATE) || NpcNameLineHolder.getInstance().isBlackListContainsName(info.getName()))
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER_NAME, ExNeedToChangeName.REASON_INVALID, info.getName()));
				return;
			}
			else if(CharacterDAO.getInstance().getPlayersCountByName(info.getName()) > 1)
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER_NAME, ExNeedToChangeName.REASON_EXISTS, info.getName()));
				return;
			}
		}

		if(Config.EX_2ND_PASSWORD_CHECK)
		{
			if(info.isPasswordEnable())
			{
				if(!info.isPasswordChecked())
				{
					sendPacket(info.getPassword() == null ? Ex2ndPasswordCheck.PASSWORD_NEW : Ex2ndPasswordCheck.PASSWORD_PROMPT);
					return;
				}
			}
		}

		Player noCarrierPlayer = null;
		CharSelectInfo[] array = getCharacters();
		for(int i = 0; i < array.length; i++)
		{
			CharSelectInfo p = array[i];
			Player player = p != null ? GameObjectsStorage.getPlayer(p.getObjectId()) : null;
			if(player != null)
			{
				// если у нас чар в оффлайне, или выходит, и этот чар выбран - кикаем
				if(player.isInOfflineMode() || player.isLogoutStarted())
				{
					if(index == i)
						player.kick();
				}
				else
				{
					player.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);

					// если есть чар
					// если это выбраный - обнуляем конект и используем, иначе кикаем)
					if(index == i)
					{
						noCarrierPlayer = player;

						GameClient oldClient = player.getNetConnection();
						if(oldClient != null)
						{
							oldClient.setActiveChar(null);
							oldClient.closeNow(false);
						}
					}
					else
						player.logout();
				}
			}
		}

		Player selectedPlayer = noCarrierPlayer == null ? Player.restore(objectIdByIndex) : noCarrierPlayer;
		if(selectedPlayer == null)
		{
			sendPacket(ActionFail.STATIC);
			return;
		}

		if(selectedPlayer.getAccessLevel() < 0)
			selectedPlayer.setAccessLevel(0);

		setActiveChar(selectedPlayer);
		setState(GameClient.GameClientState.ENTER_GAME);

		sendPacket(new CharSelected(selectedPlayer, getSessionKey().playOkID1));
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		return _crypt.decrypt(buf.array(), buf.position(), size);
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void sendPacket(L2GameServerPacket... gsp)
	{
		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void sendPackets(List<L2GameServerPacket> gsp)
	{
		if(isConnected())
			getConnection().sendPackets(gsp);
	}

	public void close(L2GameServerPacket gsp)
	{
		if(isConnected())
			getConnection().close(gsp);
	}

	public String getIpAddr()
	{
		return _ip;
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	public double getBonus()
	{
		return _bonus;
	}

	public int getBonusExpire()
	{
		return _bonusExpire;
	}

	public void setBonus(double bonus)
	{
		_bonus = bonus;
	}

	public void setBonusExpire(int bonusExpire)
	{
		_bonusExpire = bonusExpire;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState state)
	{
		_state = state;
	}

	public void onPacketReadFail()
	{
		if(_failedPackets++ >= 10)
		{
			_log.warn("Too many client packet fails, connection closed : " + this);
			closeNow(true);
		}
	}

	public void onUnknownPacket()
	{
		if(_unknownPackets++ >= 10)
		{
			_log.warn("Too many client unknown packets, connection closed : " + this);
			closeNow(true);
		}
	}

	public CharSelectInfo[] getCharacters()
	{
		return _csi;
	}

	public int getSelectedIndex()
	{
		return _selectedIndex;
	}

	public Language getLanguage()
	{
		return _language;
	}

	public void setLanguage(Language language)
	{
		_language = language;
	}

	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_login == null ? "" : " Account: " + _login) + (_activeChar == null ? "" : " Player : " + _activeChar);
	}
}