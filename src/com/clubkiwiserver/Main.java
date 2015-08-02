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
            temp += (String)o + " ";
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