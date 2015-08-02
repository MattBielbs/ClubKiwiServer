package com.clubkiwiserver;
import com.clubkiwiserver.Packet.Packet;
import com.clubkiwiserver.Packet.PacketType;

import java.net.*;

/**
 * Created by Mathew on 8/2/2015.
 */
public class Client
{
    enum ClientState
    {
        Connecting,
        Connected
    }

    private ClientState clientState;
    private InetAddress IPAddress;
    private int iPort, id;

    public Client(ClientState clientState, InetAddress IPAddress, int iPort)
    {
        this.clientState = clientState;
        this.IPAddress = IPAddress;
        this.iPort = iPort;
    }

    public void OnDataReceive(Packet p) throws Exception
    {
        if (p == null || p.getAllData().length == 0)
            return;

        System.out.println(p.getType().toString() + ": " + Main.arraytostring(p.getAllData()));

        if(p.getType() == PacketType.Login_C)
        {
            int id = Main.dbHelper.Login((String) p.getData(0), (String) p.getData(1));

            if(id == 0)
            {
                //failed
                byte[] sendData = Main.s.Serialize(PacketType.Login_S, id,"Wrong username or password");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }
            else
            {
                //worked lol
                byte[] sendData = Main.s.Serialize(PacketType.Login_S, id, "Login accepted");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }
        }
        else if(p.getType() == PacketType.CreateUser_C)
        {
            int id = Main.dbHelper.CreateUser((String) p.getData(0), (String) p.getData(1));

            if(id == 0)
            {
                //failed
                byte[] sendData = Main.s.Serialize(PacketType.CreateUser_S, id,"That username is already taken, please try again.");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }
            else
            {
                //worked lol
                byte[] sendData = Main.s.Serialize(PacketType.CreateUser_S, id, "Account created.");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }

        }
        else if(p.getType() == PacketType.CharacterList_C)
        {
            //    public Kiwi(String name, double health, double money, double strength, double speed, double flight, double swag, double hunger, double social, double energy)

            //fake character for now
            byte[] sendData = Main.s.Serialize(PacketType.CharacterList_S, "Matypatty", 100, 200, 300, 400, 1, 420, 666, 0, 123);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
            Main.serverSocket.send(sendPacket);
        }

    }

    public ClientState getClientState()
    {
        return clientState;
    }

    public void setClientState(ClientState clientState)
    {
        this.clientState = clientState;
    }

    public InetAddress getIPAddress()
    {
        return IPAddress;
    }

    public void setIPAddress(InetAddress IPAddress)
    {
        this.IPAddress = IPAddress;
    }

    public int getiPort()
    {
        return iPort;
    }

    public void setiPort(int iPort)
    {
        this.iPort = iPort;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
