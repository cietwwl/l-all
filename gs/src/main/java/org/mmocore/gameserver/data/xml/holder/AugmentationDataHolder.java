package org.mmocore.gameserver.data.xml.holder;

import java.util.HashSet;
import java.util.Set;

import org.mmocore.commons.data.xml.AbstractHolder;
import org.mmocore.gameserver.templates.augmentation.AugmentationInfo;

/**
 * @author VISTALL
 * @date 15:10/14.03.2012
 */
public class AugmentationDataHolder extends AbstractHolder
{
	private static AugmentationDataHolder _instance = new AugmentationDataHolder();
	private Set<AugmentationInfo> _augmentationInfos = new HashSet<AugmentationInfo>();

	public static AugmentationDataHolder getInstance()
	{
		return _instance;
	}

	private AugmentationDataHolder()
	{
	}

	@Override
	public int size()
	{
		return _augmentationInfos.size();
	}

	@Override
	public void clear()
	{
		_augmentationInfos.clear();
	}

	public void addAugmentationInfo(AugmentationInfo augmentationInfo)
	{
		_augmentationInfos.add(augmentationInfo);
	}
}