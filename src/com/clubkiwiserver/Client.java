package com.clubkiwiserver;
import com.clubkiwi.Character.Kiwi;
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
        Connected,
        LoggedIn
    }

    private ClientState clientState;
    private InetAddress IPAddress;
    private int iPort, id;
    private String username, password;
    private Kiwi kInstance;

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
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            Kiwi k = Main.dbHelper.Login(username, password);

            if(k == null)
            {
                //failed send error message
                byte[] sendData = Main.s.Serialize(PacketType.Login_S, id,"Wrong username or password");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }
            else
            {
                //worked send kiwi
                byte[] sendData = Main.s.Serialize(PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getSocial(), k.getEnergy());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);

                setClientState(ClientState.LoggedIn);
                setUsername(username);
                setPassword(password);
                setkInstance(k);
            }
        }
        else if(p.getType() == PacketType.CreateUser_C)
        {
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            Kiwi k = Main.dbHelper.CreateUser(username, password);

            if(k == null)
            {
                //failed send error
                byte[] sendData = Main.s.Serialize(PacketType.CreateUser_S, id,"That username is already taken, please try again.");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);
            }
            else
            {
                //worked send default kiwi
                byte[] sendData = Main.s.Serialize(PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getSocial(), k.getEnergy());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
                Main.serverSocket.send(sendPacket);

                setClientState(ClientState.LoggedIn);
                setUsername(username);
                setPassword(password);
                setkInstance(k);
            }
        }
        else if(p.getType() == PacketType.Disconnect)
        {
            Main.Clients.remove(this);
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

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Kiwi getkInstance()
    {
        return kInstance;
    }

    //every time this is set then the client should be updated.
    public void setkInstance(Kiwi kInstance)
    {
        this.kInstance = kInstance;

        try
        {
            byte[] sendData = Main.s.Serialize(PacketType.KiwiUpdate_S, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getStrength(), kInstance.getSpeed(), kInstance.getFlight(), kInstance.getSwag(), kInstance.getHunger(), kInstance.getSocial(), kInstance.getEnergy());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPort);
            Main.serverSocket.send(sendPacket);
        }
        catch(Exception ex)
        {
            System.out.println("Error updating client");
        }
    }

    @Override
    public String toString()
    {
        return "Client{" +
                "clientState=" + clientState +
                ", IPAddress=" + IPAddress +
                ", iPort=" + iPort +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", kInstance=" + kInstance +
                '}';
    }
}
