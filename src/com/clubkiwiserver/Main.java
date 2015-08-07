package com.clubkiwiserver;
import com.clubkiwiserver.CVar.*;
import com.clubkiwiserver.Packet.*;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Main implements Runnable
{

    static Serializer s;
    public static ArrayList<Client> Clients;
    static DatagramSocket serverSocket;
    public static DBHelper dbHelper;
    public static GameLogic gameLogic;
    public static CVarRegistry cVarRegistry;
    public static boolean running; //All thread should while this so that the app can close properly through the exit command.
    private static Scanner scan;
    private static Thread thread;

    //used to debug print network messages
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

        //Create cVars
        cVarRegistry = new CVarRegistry();

        //Start database
        dbHelper = new DBHelper();
        dbHelper.Connect("user1", "user1", "ClubKiwi");
        dbHelper.CreateSkeleton();

        //Start gamelogic
        gameLogic = new GameLogic();


        //Start Serverloop
        serverSocket = new DatagramSocket(5678);
        Main m = new Main();
        thread = new Thread(m);
        thread.start();

        System.out.println("Server Listening...");

        //Input loop for server commands
        scan = new Scanner(System.in);
        while(running)
        {
            String command = scan.nextLine();
            cVarRegistry.doCommand(command);
        }

        serverSocket.close();
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

    public static void SendData(Client c, PacketType type, Object ... args)
    {
        try
        {
            byte[] sendData = s.Serialize(type, args);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, c.getIPAddress(), c.getPort());
            serverSocket.send(sendPacket);
        }
        catch (IOException ex)
        {
            System.out.println("Error sending data to client.");
        }
    }

    public static Client getClient(InetAddress address, int port)
    {
        for(Client c : Clients)
        {
            if(c.getIPAddress().equals(address) && c.getPort() == port)
                return c;
        }

        return null;
    }

    public static Thread getThread()
    {
        return thread;
    }
}
