package org.mmocore.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.gameserver.data.xml.holder.ResidenceHolder;
import org.mmocore.gameserver.instancemanager.CastleManorManager;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.Manor;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Manor.SeedData;
import org.mmocore.gameserver.model.entity.residence.Castle;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.templates.manor.CropProcure;
import org.mmocore.gameserver.utils.NpcUtils;


/**
 * Format: (ch) dd [dddc]
 * d - manor id
 * d - size
 * [
 * d - crop id
 * d - sales
 * d - price
 * c - reward type
 * ]
 */
public class RequestSetCrop extends L2GameClientPacket
{
	private int _count, _manorId;

	private long[] _items; // _size*4

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 21 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new long[_count * 4];
		for(int i = 0; i < _items.length;)
		{
			int id = readD();
			long sales = readQ();
			long price = readQ();
			int type = readC();
			if(id < 1 || sales < 0 || price < 0 || type < 0 || type > 2)
			{
				_count = 0;
				return;
			}
			_items[i++] = id;
			_items[i++] = sales;
			_items[i++] = price;
			_items[i++] = type;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		if(activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
			
		final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if( castle == null || castle.getOwnerId() != activeChar.getClanId() // clan owns castle
				|| (activeChar.getClanPrivileges() & Clan.CP_CS_MANOR_ADMIN) != Clan.CP_CS_MANOR_ADMIN) // has manor rights
		{
			activeChar.sendActionFailed();
			return;
		}

		if (castle.isNextPeriodApproved())
		{
			activeChar.sendPacket(SystemMsg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM);
			activeChar.sendActionFailed();
			return;
		}

		final NpcInstance chamberlain = NpcUtils.canPassPacket(activeChar, this);
		if (chamberlain == null || chamberlain.getCastle() != castle)
		{
			activeChar.sendActionFailed();
			return;
		}

		List<CropProcure> crops = new ArrayList<CropProcure>(_count);
		final List<SeedData> checkList = Manor.getInstance().getCropsForCastle(_manorId);
		for(int i = 0; i < _count; i++)
		{
			int id = (int) _items[i * 4 + 0];
			long sales = _items[i * 4 + 1];
			long price = _items[i * 4 + 2];
			int type = (int) _items[i * 4 + 3];
			if(id > 0)
			{
				for (SeedData check : checkList)
					if (check.getCrop() == id)
					{
						if (sales > check.getCropLimit())
							break;

						long basePrice = Manor.getInstance().getCropBasicPrice(id);
						if (price != 0 && (price < basePrice * 60 / 100 || price > basePrice * 10))
							break;

						CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
						crops.add(s);
						break;
					}
			}
		}

		castle.setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		castle.saveCropData(CastleManorManager.PERIOD_NEXT);
	}
}