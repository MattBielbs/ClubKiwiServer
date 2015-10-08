package com.clubkiwiserver.DataStructs;

/**
 * Created by root on 5/10/15.
 * Instead of using the clients kiwi class as its getting too complex (optimization)
 */
public class KiwiData {

    //Attributes
    private String name;
    private double health, money;

    //Additional stats
    private double strength, speed, flight, swag;

    //Needs
    private double hunger, mood, energy;

    //Etc
    private int x, y, currentRoom;

    public KiwiData(String name, double health, double money, double strength, double speed, double flight, double swag, double hunger, double mood, double energy)
    {
        this.name = name;
        this.health = health;
        this.money = money;
        this.strength = strength;
        this.speed = speed;
        this.flight = flight;
        this.swag = swag;
        this.hunger = hunger;
        this.mood = mood;
        this.energy = energy;
        this.x = 0;
        this.y = 0;
        this.currentRoom = 0;
    }

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

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getFlight() {
        return flight;
    }

    public void setFlight(double flight) {
        this.flight = flight;
    }

    public double getSwag() {
        return swag;
    }

    public void setSwag(double swag) {
        this.swag = swag;
    }

    public double getHunger() {
        return hunger;
    }

    public void setHunger(double hunger) {
        this.hunger = hunger;
    }

    public double getMood() {
        return mood;
    }

    public void setMood(double mood) {
        this.mood = mood;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
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
}
