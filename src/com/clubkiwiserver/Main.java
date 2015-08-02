package com.clubkiwiserver;
import com.clubkiwiserver.Packet.*;

import java.net.*;
import java.util.ArrayList;

public class Main
{

    static Serializer s;
    static ArrayList<Client> Clients;
    static DatagramSocket serverSocket;
    static DBHelper dbHelper;
    static boolean running;

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
        s = new Serializer();
        Clients = new ArrayList<Client>();
        running = true;
        dbHelper = new DBHelper();
        dbHelper.Connect("user1", "user1", "ClubKiwi");
        dbHelper.CreateSkeleton();

        serverSocket = new DatagramSocket(5678);
        byte[] receiveData = new byte[1024];

        System.out.println("Server Listening...");

        while (running)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            Client temp = getClient(receivePacket.getAddress(), receivePacket.getPort());
            Packet p = s.Deserialize(receivePacket.getData());
            if(temp == null)
            {
                Client lol = new Client(Client.ClientState.Connected, receivePacket.getAddress(), receivePacket.getPort());
                Clients.add(lol);
                lol.OnDataReceive(p);
            }
            else
            {
                temp.OnDataReceive(p);
            }
        }

       dbHelper.Shutdown();
    }



    public static Client getClient(InetAddress address, int port)
    {
        for(Client c : Clients)
        {
            if(c.getIPAddress() == address && c.getiPort() == port)
                return c;
        }

        return null;
    }
}
