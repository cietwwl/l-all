/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mmocore.gameserver.handler.voicecommands.impl;

import java.util.Date;
import org.mmocore.gameserver.Config;
import org.mmocore.gameserver.dao.AccountBonusDAO;
import org.mmocore.gameserver.data.StringHolder;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.model.actor.instances.player.Bonus;
import org.mmocore.gameserver.network.authcomm.AuthServerCommunication;
import org.mmocore.gameserver.network.authcomm.gs2as.BonusRequest;
import org.mmocore.gameserver.network.l2.s2c.ExBR_PremiumState;
import org.mmocore.gameserver.network.l2.s2c.ExShowScreenMessage;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;

/**
 *
 * @author forz
 */

public class PremOff implements IVoicedCommandHandler
{
    private String[] _commandList = new String[] { "premoff" };        
    
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{      
            if("premoff".equals(command))
            {
                if(activeChar.isGM())
                {                                  
                    activeChar.getNetConnection().setBonus(0);
                    activeChar.getNetConnection().setBonusExpire(0);
                    activeChar.stopBonusTask();
                    activeChar.getBonus().setBonusExpire(0);                   
                    activeChar.getBonus().setRateXp(1.);
                    activeChar.getBonus().setRateSp(1.);
                    activeChar.getBonus().setDropAdena(1.);
                    activeChar.getBonus().setDropItems(1.);
                    activeChar.getBonus().setDropSpoil(1.);
                    activeChar.getBonus().setQuestDropRate(1.);
                    activeChar.getBonus().setQuestRewardRate(1.);
                    if(activeChar.getParty() != null)
                            activeChar.getParty().recalculatePartyData();                  
                    String msg = StringHolder.getInstance().getString(activeChar, "scripts.services.RateBonus.LuckEnded");
                    activeChar.sendPacket(new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true), new ExBR_PremiumState(activeChar, false));
                    activeChar.sendMessage(msg);                   
                    activeChar.updatePremiumItems();                    
                    if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
					AccountBonusDAO.getInstance().delete(activeChar.getAccountName());                  
                    return true;
                }               
            }                      
            return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
