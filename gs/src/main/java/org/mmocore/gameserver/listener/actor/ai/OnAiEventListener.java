package org.mmocore.gameserver.listener.actor.ai;

import org.mmocore.gameserver.ai.CtrlEvent;
import org.mmocore.gameserver.listener.AiListener;
import org.mmocore.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener
{
	public void onAiEvent(Creature actor, CtrlEvent evt, Object[] args);
}
