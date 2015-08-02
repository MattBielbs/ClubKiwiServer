package com.clubkiwiserver;
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
            s.execute("create table characters(id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), accid int, name varchar(16), health int)");
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

    public Integer CreateUser(String username, String password)
    {
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
                return rs.getInt(1);

            }
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occured while creating the user.");
            System.out.println(ex.getMessage());
        }

        return 0;
    }

    public Integer Login(String username, String password)
    {
        try
        {
            //Grab all matching.
            rs = s.executeQuery("SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'");

            //User exists, return true.
            if (rs.next())
                return rs.getInt(1);
        }
        catch(SQLException ex)
        {
            System.out.println("An exception occured while creating the user.");
            System.out.println(ex.getMessage());
        }

        return 0;
    }
}