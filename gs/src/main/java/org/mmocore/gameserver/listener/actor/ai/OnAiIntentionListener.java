package org.mmocore.gameserver.listener.actor.ai;

import org.mmocore.gameserver.ai.CtrlIntention;
import org.mmocore.gameserver.listener.AiListener;
import org.mmocore.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	public void onAiIntention(Creature actor, CtrlIntention intention, Object arg0, Object arg1);
}
