package npc.model;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author B0nux
 */
public class RafortyInstance extends NpcInstance
{
	private static final int FREYA_NECKLACE = 16025;
	private static final int BLESSED_FREYA_NECKLACE = 16026;
	private static final int BOTTLE_OF_FREYAS_SOUL = 16027;

	public RafortyInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("exchange_necklace_1"))
		{
			if(ItemFunctions.getItemCount(player, FREYA_NECKLACE) > 0)
				showChatWindow(player, "default/" + getNpcId() + "-ex4.htm");
			else
				showChatWindow(player, "default/" + getNpcId() + "-ex6.htm");
		}
		else if(command.equalsIgnoreCase("exchange_necklace_2"))
		{
			if(ItemFunctions.getItemCount(player, BOTTLE_OF_FREYAS_SOUL) > 0)
				showChatWindow(player, "default/" + getNpcId() + "-ex8.htm");
			else
				showChatWindow(player, "default/" + getNpcId() + "-ex7.htm");
		}
		else if(command.equalsIgnoreCase("exchange_necklace_3"))
		{
			if(ItemFunctions.getItemCount(player, FREYA_NECKLACE) > 0 && ItemFunctions.getItemCount(player, BOTTLE_OF_FREYAS_SOUL) > 0)
			{
				ItemFunctions.deleteItem(player, FREYA_NECKLACE, 1);
				ItemFunctions.deleteItem(player, BOTTLE_OF_FREYAS_SOUL, 1);
				ItemFunctions.addItem(player, BLESSED_FREYA_NECKLACE, 1);
				showChatWindow(player, "default/" + getNpcId() + "-ex9.htm");
			}
			else
				showChatWindow(player, "default/" + getNpcId() + "-ex11.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}