package org.mmocore.gameserver.listener.reflection;

import org.mmocore.commons.listener.Listener;
import org.mmocore.gameserver.model.entity.Reflection;

public interface OnReflectionCollapseListener extends Listener<Reflection>
{
	public void onReflectionCollapse(Reflection reflection);
}
