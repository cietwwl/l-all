package org.mmocore.gameserver.listener.actor.player.impl;

import org.mmocore.commons.lang.reference.HardReference;
import org.mmocore.gameserver.listener.actor.player.OnAnswerListener;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.skills.skillclasses.Call;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 11:28/15.04.2011
 */
public class SummonAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _summonerRef;
	private final int _itemId;
	private final int _count;
	private final long _timeStamp;

	public SummonAnswerListener(Player summoner, Player player, int itemConsumeId, int itemConsumeCount, int expiration)
	{
		_summonerRef = summoner.getRef();
		_itemId = itemConsumeId;
		_count = itemConsumeCount;
		_timeStamp = expiration > 0 ? System.currentTimeMillis() + expiration : Long.MAX_VALUE;
	}

	@Override
	public void sayYes(Player player)
	{
		if (System.currentTimeMillis() > _timeStamp)
			return;

		Player summoner = _summonerRef.get();
		if(summoner == null)
			return;

		if (Call.canSummonHere(summoner) != null)
			return;

		if (Call.canBeSummoned(player) != null)
			return;

		player.abortAttack(true, true);
		player.abortCast(true, true);
		player.stopMove();

		if (_itemId == 0 || _count == 0)
			player.teleToLocation(summoner.getLoc());
		else if (ItemFunctions.deleteItem(player, _itemId, _count))
			player.teleToLocation(summoner.getLoc());
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	@Override
	public void sayNo(Player player)
	{
		//
	}
}
