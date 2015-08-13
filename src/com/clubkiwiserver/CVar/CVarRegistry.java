package com.clubkiwiserver.CVar;

import com.clubkiwiserver.Main;

import java.util.HashMap;

/**
 * Created by x201 on 6/08/2015.
 * console variable class which also contains test server functions
 */
public class CVarRegistry
{
    private HashMap<String, CVar> Registry;

    public CVarRegistry()
    {
        Registry = new HashMap<String, CVar>();
        Registry.put("timeframe", new CVar(1.0));
        Registry.put("debuginfo", new CVar(0));
    }

    public void doCommand(String command)
    {
        String[] split = command.split("\\s+");//set timeframe 1.0
        if(split.length > 1)
        {
            CVar temp = Registry.get(split[1]);
            if (temp != null)
            {
                if (split[0].compareToIgnoreCase("set") == 0)
                {
                    try
                    {
                        temp.setValue(split[2]);
                    } catch (NumberFormatException ex)
                    {
                        System.out.println("you tried to set to the wrong type, requires " + temp.getType().toString());
                    }
                }
                else if (split[0].compareToIgnoreCase("get") == 0)
                {
                    System.out.println(temp.getValue());
                }
                else
                {
                    System.out.println("usage: get/set variable value");
                }
            }
            else
            {
                System.out.println("Unknown CVar. usage: get/set variable value");
            }
        }
        else
        {
            //space for 1 word commands like exit
            if(split[0].compareToIgnoreCase("exit") == 0)
            {
                Main.running = false;
                Main.getThread().interrupt();
                Main.gameLogic.getThread().interrupt();
            }
            else
                System.out.println("usage: get/set variable value");
        }
    }

    public Object getCVar(String cvar)
    {
        return Registry.get(cvar).getValue();
    }

}
