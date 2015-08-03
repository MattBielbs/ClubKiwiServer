package com.clubkiwiserver;
import com.clubkiwiserver.Packet.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main implements Runnable
{

    static Serializer s;
    public static ArrayList<Client> Clients;
    static DatagramSocket serverSocket;
    static DBHelper dbHelper;
    static GameLogic gameLogic;
    static boolean running; //All thread should while this so that the app can close properly through the exit command.
    private static Scanner scan;

    public static String arraytostring(Object[] array)
    {
        String temp = "";
        for(Object o : array)
        {
            try
            {
                temp += (String) o + " ";
            }
            catch(ClassCastException ex)
            {
                if(ex.getMessage().contains("Double"))
                    temp += Double.toString((Double)o) + " ";
                else if(ex.getMessage().contains("Integer"))
                    temp += Integer.toString((Integer)o) + " ";
            }
        }
        return temp;
    }

    public static void main(String[] args) throws Exception
    {
        //Stop the database from getting owned
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                dbHelper.Shutdown();
            }
        });

        s = new Serializer();
        Clients = new ArrayList<Client>();
        running = true;

        //Start database
        dbHelper = new DBHelper();
        dbHelper.Connect("user1", "user1", "ClubKiwi");
        dbHelper.CreateSkeleton();

        //Start gamelogic
        gameLogic = new GameLogic();


        //Start Serverloop
        serverSocket = new DatagramSocket(5678);
        Main m = new Main();
        Thread thread = new Thread(m);
        thread.start();

        System.out.println("Server Listening...");

        //Input loop for server commands
        scan = new Scanner(System.in);
        while(running)
        {
            String command = scan.nextLine();

            String[] split = command.split("\\s");
            if(split[0].equalsIgnoreCase("exit"))
            {
                m.serverSocket.close();
                gameLogic.getThread().interrupt();
                running = false;
            }
            else if(split[0].equalsIgnoreCase("list"))
            {
                for(Client c : Main.Clients)
                {
                    System.out.println(c.toString());
                }
            }
            else if(split[0].equalsIgnoreCase("set"))
            {
                try
                {
                    switch (split[1])
                    {
                        case "timeframe":
                            gameLogic.timeFrame = Double.parseDouble(split[2]);
                            gameLogic.getThread().interrupt();
                            System.out.println(split[1] + " updated");
                            break;
                    }
                }
                catch(Exception ex)
                {
                    System.out.println("Invalid syntax (set variable value)");
                }
            }
            else
            {
                System.out.println("Invalid command.");
            }
        }
    }

    @Override
    public void run()
    {
        byte[] receiveData = new byte[1024];

        while (running)
        {
            try
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                Client temp = getClient(receivePacket.getAddress(), receivePacket.getPort());
                Packet p = s.Deserialize(receivePacket.getData());
                if (temp == null)
                {
                    Client lol = new Client(Client.ClientState.Connected, receivePacket.getAddress(), receivePacket.getPort());
                    Clients.add(lol);
                    lol.OnDataReceive(p);
                } else
                {
                    temp.OnDataReceive(p);
                }
            }
            catch(Exception ex)
            {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static Client getClient(InetAddress address, int port)
    {
        for(Client c : Clients)
        {
            if(c.getIPAddress().equals(address) && c.getiPort() == port)
                return c;
        }

        return null;
    }
}
