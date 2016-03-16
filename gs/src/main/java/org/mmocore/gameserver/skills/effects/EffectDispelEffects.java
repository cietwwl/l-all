package org.mmocore.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import org.mmocore.commons.collections.CollectionUtils;
import org.mmocore.commons.collections.LazyArrayList;
import org.mmocore.commons.util.Rnd;
import org.mmocore.gameserver.model.Effect;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.stats.Env;
import org.mmocore.gameserver.utils.EffectsComparator;

/**
 * @author pchayka
 */

public class EffectDispelEffects extends Effect
{
	private final int _cancelRate;
	private final int _negateCount;

	public EffectDispelEffects(Env env, EffectTemplate template)
	{
		super(env, template);
		_cancelRate = template.getParam().getInteger("cancelRate", 0);
		_negateCount = template.getParam().getInteger("negateCount", 5);
	}

	@Override
	public void onStart()
	{
		if(_effected.getEffectList().isEmpty())
			return;
		
		List<Effect> effectList = new ArrayList<Effect>(_effected.getEffectList().getAllEffects());
		CollectionUtils.shellSort(effectList, EffectsComparator.getReverseInstance());

		LazyArrayList<Effect> buffList = LazyArrayList.newInstance();
		for(Effect e : effectList)
			if(e.isOffensive() && e.isCancelable())
				buffList.add(e);

		if(buffList.isEmpty())
		{
			LazyArrayList.recycle(buffList);
			return;
		}

		int negated = 0;
		for(Effect e : buffList)
		{
			if(negated >= _negateCount)
				break;

			if(Rnd.chance(_cancelRate))
			{
				negated++;
				_effected.sendPacket(new SystemMessage(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		}

		buffList.clear();
		LazyArrayList.recycle(buffList);
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}