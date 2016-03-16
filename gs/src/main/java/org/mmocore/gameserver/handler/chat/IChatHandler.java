package org.mmocore.gameserver.handler.chat;

import org.mmocore.gameserver.network.l2.components.ChatType;

/**
 * @author VISTALL
 * @date 18:16/12.03.2011
 */
public interface IChatHandler
{
	void say();

	ChatType getType();
}
