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
    private int port, id;
    private String username, password;
    private Kiwi kInstance;

    public Client(ClientState clientState, InetAddress IPAddress, int port)
    {
        this.clientState = clientState;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public void OnDataReceive(Packet p) throws Exception
    {
        if (p == null || p.getAllData().length == 0)
            return;

        if((int)Main.cVarRegistry.getCVar("debuginfo") > 1)
            System.out.println(p.getType().toString() + ": " + Main.arraytostring(p.getAllData()));

        if(p.getType() == PacketType.Login_C)
        {
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            Kiwi k = Main.dbHelper.Login(username, password);

            if(k == null)
            {
                Main.SendData(this, PacketType.Login_S, id, "Wrong username or password");
            }
            else
            {
                //worked send kiwi
                Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getSocial(), k.getEnergy());

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
                Main.SendData(this, PacketType.CreateUser_S, id, "That username is already taken, please try again.");
            }
            else
            {
                //worked send default kiwi
                Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getSocial(), k.getEnergy());

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

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
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

        //this could be optimised by checking to see if anything has actually changed
        Main.SendData(this, PacketType.KiwiUpdate_S, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getStrength(), kInstance.getSpeed(), kInstance.getFlight(), kInstance.getSwag(), kInstance.getHunger(), kInstance.getSocial(), kInstance.getEnergy());
    }

    @Override
    public String toString()
    {
        return "Client{" +
                "clientState=" + clientState +
                ", IPAddress=" + IPAddress +
                ", Port=" + port +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", kInstance=" + kInstance +
                '}';
    }
}
