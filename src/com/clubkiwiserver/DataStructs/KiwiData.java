package com.clubkiwiserver.DataStructs;

/**
 * Created by root on 5/10/15.
 * Instead of using the clients kiwi class as its getting too complex (optimization)
 */
public class KiwiData {

    //Attributes
    private String name;
    private double health, money, hunger;

    //Etc
    private int x, y, currentRoom;

    public KiwiData(String name, double health, double money,double hunger)
    {
        this.name = name;
        this.health = health;
        this.money = money;
        this.hunger = hunger;
        this.x = 0;
        this.y = 0;
        this.currentRoom = 0;
    }

    //region Getters/Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getHunger() {
        return hunger;
    }

    public void setHunger(double hunger) {
        this.hunger = hunger;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }
    //endregion
}
