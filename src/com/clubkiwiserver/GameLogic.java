package com.clubkiwiserver;

import com.clubkiwi.Character.Kiwi;
import com.clubkiwi.Helper;

/**
 * Created by x201 on 3/08/2015.
 */
public class GameLogic implements Runnable
{

    private Thread thread;

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
                Thread.sleep(Math.round(86400 * (Double) Main.cVarRegistry.getCVar("timeframe")));

                //some debug info
                if((int)Main.cVarRegistry.getCVar("debuginfo") > 0)
                    Helper.println("Tick");

                Main.dbHelper.DecreaseAllHunger(); //this should also decrease health if hunger is 0

                //update the server version of the clients kiwi then send it over as an update packet.
                for (Client c : Main.Clients)
                {
                    c.setkInstance(Main.dbHelper.Login(c.getUsername(), c.getPassword()));
                }
            }
            catch (InterruptedException ex)
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
