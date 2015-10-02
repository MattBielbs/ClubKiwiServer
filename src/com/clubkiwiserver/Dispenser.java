package com.clubkiwiserver;


import java.awt.*;

/**
 * Created by Mathew on 10/2/2015.
 */
public class Dispenser
{
    protected int ID, x, y, w, h;
    protected boolean bCollide, bVisible;

    public Dispenser(int ID, int x, int y, boolean bVisible)
    {
        this.ID = ID;
        this.x = x;
        this.y = y;
        this.w = 300;
        this.h = 300;
        this.bCollide = false;
        this.bVisible = bVisible;
    }

    public int getID()
    {
        return ID;
    }

       public int getX()
    {
        return x;
    }

       public int getY()
    {
        return y;
    }

    public boolean isbVisible()
    {
        return bVisible;
    }
}
