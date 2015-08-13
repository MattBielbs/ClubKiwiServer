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
                Thread.sleep(Math.round(86400 * (Double) Main.cVarRegistry.getCVar("timeframe")));

                if((int)Main.cVarRegistry.getCVar("debuginfo") > 0)
                    Helper.println("Tick");

                //every 1.44minutes makes 100 health turn to 0 in 24
                Main.dbHelper.DecreaseAllHunger();

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
