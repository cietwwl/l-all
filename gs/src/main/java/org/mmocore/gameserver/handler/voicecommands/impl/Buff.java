/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mmocore.gameserver.handler.voicecommands.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mmocore.gameserver.handler.voicecommands.IVoicedCommandHandler;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.Skill;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.database.mysql;
/**
 *
 * @author forz
 */

public class Buff implements IVoicedCommandHandler
{
    private String[] _commandList = new String[] { "magebuff", "warbuff", "mybuff" };    
    
    int[] mage = {10354,10786,10853,10622,13893,13521,15421,12594,13973,13032,13541,13531,13571,
            12843,14991,15001,15011,15031,15041,14611,14161,8301,47033,2731,2761,3651,2671,2681,2641,
            3041,3491,3631,13231};   
    int[] warrior = {10354,12403,10362,10622,13883,13521,15421,12594,13541,13531,12843,15191,
               14991,15011,15021,15041,70641,14611,13641,14161,46993,2741,2711,2751,3101,9151,
               2691,2671,2681,2641,3041,3491,3641,13231};
    
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{                   
            command = command.intern();
            Date date = new Date();
            
            if(activeChar.time == 0 || activeChar.time <= date.getTime())
            {
                if(activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying())
                {
                    activeChar.sendMessage("В данных условиях использовать 'Баффер' запрещено.");                       
                    return false;
                } 
                
                if("mybuff".equals(command))
                {                                                                                                            
                    if(args == null)
                        return false; 
                    int id = 0;
                    id = mysql.get_int("SELECT id FROM buff_groups WHERE user_id = " + String.valueOf(activeChar.getObjectId()) + " AND droup_name = '" + args + "';");
                    if(id == 0)
                        return false;                   
                    List<Object> buffs = mysql.get_array("SELECT * FROM buff_groups_buffs WHERE group_id = " + String.valueOf(id));
                    List<Integer> buffIds = new ArrayList<Integer>();                    
                    for (Object buff : buffs) 
                    {
                        Map<String, Object> tmp = (HashMap<String, Object>)buff;
                        buffIds.add((Integer)tmp.get("buff_id"));                       
                    }
                    for (Integer buff : buffIds)
                    {
                        SkillEntry skillentry = SkillTable.getInstance().getSkillEntry(((int)buff / 10), ((int)buff % 10));
                        Skill skill = skillentry.getTemplate();                                               
                        activeChar.callSkill(skillentry, skill.getTargets(activeChar, activeChar, true,true), false);                                                
                    } 
                }
                else
                {
                    for(int i=0; i< ("magebuff".equals(command) ? mage.length : warrior.length); i++)
                    {
                        int id = "magebuff".equals(command) ? mage[i]/10 : warrior[i]/10; 
                        int lvl = "magebuff".equals(command) ? mage[i]%10 : warrior[i]%10;
                        SkillEntry skillentry = SkillTable.getInstance().getSkillEntry(id, lvl);
                        Skill skill = skillentry.getTemplate();                                                              
                        activeChar.callSkill(skillentry, skill.getTargets(activeChar, activeChar, true,true), true);                                                                                                                                                           
                    }  
                }
                activeChar.time = date.getTime() + activeChar.cooldown;
            }
            else
                activeChar.sendMessage("До повторного использования осталось " + String.valueOf(activeChar.cooldwonMessage = (activeChar.time - date.getTime()) / 1000) + " сек.");                              
            
            return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
