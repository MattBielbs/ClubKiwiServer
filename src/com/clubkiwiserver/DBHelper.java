package com.clubkiwiserver;
import com.clubkiwi.Character.Kiwi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;
import java.security.*;

/**
 * Handles all the derbydb nasty things
 * most methods use username and password saved in the client class to auth every action.
 */
public class DBHelper
{
    Statement s;
    ResultSet rs;
    Connection conn;
    Boolean bConnected = false;
    ArrayList<Statement> statements;
    private String framework = "embedded";
    private String protocol = "jdbc:derby:";


    public DBHelper()
    {
        conn = null;
        statements = new ArrayList<Statement>(); //statements added to here to be closed before exit.
        rs = null;
    }

    public void Connect(String username, String password, String dbName)
    {
        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);

        try
        {
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
            System.out.println("Connected to database.");
            bConnected = true;
        }
        catch(SQLException ex)
        {
            System.out.println("Failed to connect to the database");
        }
    }

    public void Shutdown()
    {
        bConnected = false;

        for(Statement s : statements)
        {
            try
            {
                s.close();
            }
            catch(SQLException ex)
            {

            }
        }

        try
        {
            if(conn != null)
                conn.close();
        }
        catch(SQLException ex)
        {

        }

        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        }
        catch (SQLException se) {
            // SQL State XJO15 and SQLCode 50000 mean an OK shutdown.
            if (!(se.getErrorCode() == 50000) && (se.getSQLState().equals("XJ015")))
                System.err.println(se);
        }
    }

    public void CreateSkeleton()
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to create the skeleton.");

        try
        {
            s = conn.createStatement();
            statements.add(s);

            System.out.println("Creating Tables");
            System.out.print("Users ");
            s.execute("create table users(id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), username varchar(16), password varchar(16))");
            System.out.print("OK! \nCharacters ");
            s.execute("create table characters(id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), accid int, name varchar(16), health int, money int, strength int, speed int, flight int, swag int, hunger int, mood int, energy int)");
            System.out.print("OK! \nItems ");
            s.execute("create table items(id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(100), description varchar(100))");
            System.out.println("OK!");
        }
        catch(SQLException ex)
        {
            if(ex.getSQLState().compareToIgnoreCase("X0Y32") == 0) //<value> '<value>' already exists in <value> '<value>'.
                System.out.println("Database present, Skipping.");
            else
            {
                System.out.println("An exception occured while creating the skeleton database.");
                System.out.println(ex.getMessage());
            }
        }
    }

    public Integer GetUserID(String username, String password)
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to get user id.");

        try
        {
            //Grab all matching.
            rs = s.executeQuery("SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'");

            //User exists
            if (rs.next())
                return rs.getInt("id");
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occured while geting userid.");
            System.out.println(ex.getMessage());
        }

        return 0;
    }

    public Kiwi CreateUser(String username, String password)
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to create user.");

        try
        {
            //Make sure the user doesn't already exist.
            rs = s.executeQuery("SELECT * FROM users WHERE username='" + username + "'");

            //No results found, continue
            if (!rs.next())
            {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES ('" + username + "', '" + password + "')", Statement.RETURN_GENERATED_KEYS);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                rs.next();
                int accid = rs.getInt(1);

                //Create a default kiwi for the user
                Kiwi k = new Kiwi(username, 100, 0, 0, 0, 0, 0, 100, 100, 100);
                s.execute("INSERT INTO characters (accid, name, health, money, strength, speed, flight, swag, hunger, mood, energy) VALUES (" + accid + ", '" + username + "', 100, 0, 0, 0, 0, 0, 100, 100, 100)");
                return k;

            }
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occured while creating the user.");
            System.out.println(ex.getMessage());
        }

        return null;
    }

    //Also used to get character
    public Kiwi Login(String username, String password)
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to login.");

        try
        {
            Integer accid = GetUserID(username, password);
            if(accid != 0)
            {
                rs = s.executeQuery("SELECT * FROM characters WHERE accid =" + accid);

                if(rs.next())
                {
                    //tis should always be the case else the acc needs to be deleted rip
                    return new Kiwi(rs.getString("name"), rs.getDouble("health"), rs.getDouble("money"), rs.getDouble("strength"), rs.getDouble("speed"), rs.getDouble("flight"), rs.getDouble("swag"), rs.getDouble("hunger"), rs.getDouble("mood"), rs.getDouble("energy"));
                }
            }
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occured while logging in the user.");
            System.out.println(ex.getMessage());
        }

        return null;
    }

    public void DecreaseAllHunger()
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to decrease hunger.");

        try
        {
            s.execute("UPDATE characters SET hunger = hunger - 1 WHERE hunger > 0");
            s.execute("UPDATE characters SET health = health - 1 WHERE hunger = 0 AND health > 0");
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occurred while running tick.");
            System.out.println(ex.getMessage());
        }
    }

    public void UpdateCharacter(Client c, double health, double money, double strength, double speed, double flight, double swag, double hunger, double mood, double energy)
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to update character.");

        try
        {
            s.execute("UPDATE characters SET health = " + health + ", money = " + money + ", strength = " + strength + ", speed = " + speed + ", flight = " + flight + ", swag = " + swag + ", hunger = " + hunger + ", mood = " + mood + ", energy = " + energy + " WHERE accid = " + GetUserID(c.getUsername(), c.getPassword()));
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occurred while updating character.");
            System.out.println(ex.getMessage());
        }
    }

    //might need this later
    public void SetStat(Client c, String stat, double value)
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to set stat " + stat);

        try
        {
            s.execute("UPDATE characters SET " + value + " = " + value + " WHERE accid = " + GetUserID(c.getUsername(), c.getPassword()));
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occurred while setting stat: " + stat);
            System.out.println(ex.getMessage());
        }
    }
}
