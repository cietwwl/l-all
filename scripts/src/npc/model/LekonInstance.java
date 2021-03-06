package npc.model;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.templates.npc.NpcTemplate;
import org.mmocore.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 10:10/24.06.2011
 */
public class LekonInstance extends NpcInstance
{
	private static final int ENERGY_STAR_STONE = 13277;
	private static final int AIRSHIP_SUMMON_LICENSE = 13559;

	public LekonInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("get_license"))
		{
			if(player.getClan() == null || !player.isClanLeader() || player.getClan().getLevel() < 5)
			{
				showChatWindow(player, 2);
				return;
			}

			if(player.getClan().isHaveAirshipLicense() || ItemFunctions.getItemCount(player, AIRSHIP_SUMMON_LICENSE) > 0)
			{
				showChatWindow(player, 4);
				return;
			}

			if(!ItemFunctions.deleteItem(player, ENERGY_STAR_STONE, 10))
			{
				showChatWindow(player, 3);
				return;
			}

			ItemFunctions.addItem(player, AIRSHIP_SUMMON_LICENSE, 1);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
