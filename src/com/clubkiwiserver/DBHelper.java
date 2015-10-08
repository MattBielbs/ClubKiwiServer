package com.clubkiwiserver;
import com.clubkiwi.Character.Kiwi;
import com.clubkiwiserver.DataStructs.KiwiData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Handles all the derbydb nasty things
 * most methods use username and password saved in the client class to auth every action.
 */
public class DBHelper
{
    private Statement s;
    private ResultSet rs;
    private Connection conn;
    private Boolean bConnected = false;
    private ArrayList<Statement> statements;
    private String framework = "embedded";


    public DBHelper()
    {
        conn = null;
        statements = new ArrayList<>(); //statements added to here to be closed before exit.
        rs = null;
    }

    //Connect to the database.
    public void Connect(String username, String password, String dbName)
    {
        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);

        try
        {
            String protocol = "jdbc:derby:";
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
            System.out.println("Connected to database.");
            bConnected = true;
        }
        catch(SQLException ex)
        {
            System.out.println("Failed to connect to the database");
        }
    }

    //Closes the database correctly.
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
        catch (SQLException ex) {
            // SQL State XJO15 and SQLCode 50000 mean an OK shutdown.
            if (!(ex.getErrorCode() == 50000) && (ex.getSQLState().equals("XJ015")))
                System.err.println(ex.getMessage());
        }
    }

    //Used to create the tables in the database i they dont exist.
    public void CreateSkeleton() throws IllegalStateException
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

    //Helper function used to get the userid.
    private Integer GetUserID(String username, String password) throws IllegalStateException
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

    public KiwiData CreateUser(String username, String password) throws IllegalStateException
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
                KiwiData k = new KiwiData(username, 100, 0, 0, 0, 0, 0, 100, 100, 100);
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
    public KiwiData Login(String username, String password) throws IllegalStateException
    {
        if(!bConnected)
            throw new IllegalStateException("You must be connected to the database to login.");

        try
        {
            //grab the account id so we can get the character.
            Integer accid = GetUserID(username, password);
            if(accid != 0)
            {
                rs = s.executeQuery("SELECT * FROM characters WHERE accid =" + accid);

                if(rs.next())
                {
                    //this should always be the case else the acc needs to be deleted rip
                    return new KiwiData(rs.getString("name"), rs.getDouble("health"), rs.getDouble("money"), rs.getDouble("strength"), rs.getDouble("speed"), rs.getDouble("flight"), rs.getDouble("swag"), rs.getDouble("hunger"), rs.getDouble("mood"), rs.getDouble("energy"));
                }
                else
                {
                    System.out.println("Account has no character");
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

    public void DecreaseAllHunger() throws IllegalStateException
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

    public void UpdateCharacter(Client c, double health, double money, double strength, double speed, double flight, double swag, double hunger, double mood, double energy) throws IllegalStateException
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
    public void SetStat(Client c, String stat, double value) throws IllegalStateException
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
