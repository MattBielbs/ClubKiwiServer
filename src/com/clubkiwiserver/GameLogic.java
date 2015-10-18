package com.clubkiwiserver;

import com.clubkiwi.Helper;
import com.clubkiwiserver.DataStructs.DispenserData;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by x201 on 3/08/2015.
 */
public class GameLogic implements Runnable
{

    private final Thread thread;
    private Random rand = new Random();

    public GameLogic()
    {
        //Start this class in a new thread
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        while (Main.running)
        {
            try
            {
                //Sleep for however long the tick is set to.
                Thread.sleep(Math.round(10000 * (Double) Main.cVarRegistry.getCVar("timeframe")));

                //some debug info
                if((int)Main.cVarRegistry.getCVar("debuginfo") > 4)
                    Helper.println("Tick");

                Main.dbHelper.DecreaseAllHunger(); //this should also decrease health if hunger is 0

                //update the server version of the clients kiwi then send it over as an update packet.
                for (Client c : Main.Clients)
                {
                    c.setkInstance(Main.dbHelper.Login(c.getUsername(), c.getPassword()));
                }

                //Regenerate the dispensers (50% chance to regenerate a used dispenser)
                for(ArrayList<DispenserData> a :Main.worldItems.values())
                {
                    for(DispenserData d : a)
                    {
                        if(!d.isbVisible())
                        {
                            if(rand.nextBoolean())
                                d.setbVisible(true);
                        }
                    }
                }

                for (Client c : Main.Clients)
                {
                    c.sendDespensersFromRoom();
                }

            }
            catch (Exception ex)
            {
                System.out.println("Gamelogic interrupted");
            }
        }
    }

    public Thread getThread()
    {
        return thread;
    }
}
