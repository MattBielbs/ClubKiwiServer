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

    public Client(int id, ClientState clientState, InetAddress IPAddress, int port)
    {
        this.id = id;
        this.clientState = clientState;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    //massive function might split it up later.
    public void OnDataReceive(Packet p) throws Exception
    {
        //Make sure its not an empty or null packet.
        if (p == null || p.getAllData().length == 0)
            return;

        //debug info
        if((int)Main.cVarRegistry.getCVar("debuginfo") > 1)
            System.out.println(p.getType().toString() + ": " + Main.arraytostring(p.getAllData()));

        //Check the packet type
        if(p.getType() == PacketType.Login_C)
        {
            //Try and log the player in
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            Kiwi k = Main.dbHelper.Login(username, password);

            if(k == null)
            {
                Main.SendData(this, PacketType.Login_S, id, "Wrong username or password");
            }
            else
            {
                //Worked send kiwi
                Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getMood(), k.getEnergy(), id);

                setClientState(ClientState.LoggedIn);
                setUsername(username);
                setPassword(password);
                setkInstance(k);
            }
        }
        else if(p.getType() == PacketType.CreateUser_C)
        {
            //Try and create a character using the login provided.
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
                Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getStrength(), k.getSpeed(), k.getFlight(), k.getSwag(), k.getHunger(), k.getMood(), k.getEnergy(), id);

                setClientState(ClientState.LoggedIn);
                setUsername(username);
                setPassword(password);
                setkInstance(k);
            }
        }
        else if(p.getType() == PacketType.KiwiUpdate_C)
        {
            //Update character
            Main.dbHelper.UpdateCharacter(this, (Double)p.getData(0), (Double)p.getData(1), (Double)p.getData(2), (Double)p.getData(3), (Double)p.getData(4), (Double)p.getData(5), (Double)p.getData(6), (Double)p.getData(7), (Double)p.getData(8));

            //Load values from database and overwrite, also send to client.
            setkInstance(Main.dbHelper.Login(getUsername(), getPassword()));
        }
        else if(p.getType() == PacketType.Disconnect_C)
        {
            //Client disconnect packet, remove from list.
            Main.Clients.remove(this);

            //Tell other clients about this
            broadcastKiwi(PacketType.Disconnect_S);
        }
        else if(p.getType() == PacketType.KiwiPos_C)
        {
            getkInstance().setX((int)p.getData(0));
            getkInstance().setY((int)p.getData(1));

            //Tell other clients about this
            broadcastKiwi(PacketType.KiwiPos_S);
        }
        else if(p.getType() == PacketType.Chat_C)
        {
            for(Client c : Main.Clients)
            {
                Main.SendData(c, PacketType.Chat_S, id, p.getData(0));
            }
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
      // this could be optimised by checking to see if anything has actually changed
        Main.SendData(this, PacketType.KiwiUpdate_S, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getStrength(), kInstance.getSpeed(), kInstance.getFlight(), kInstance.getSwag(), kInstance.getHunger(), kInstance.getMood(), kInstance.getEnergy());

        //Tell other clients about this
        broadcastKiwi(PacketType.OtherPlayer_S);

        //Tell you about all clients
        sendPlayers();
    }

    private void broadcastKiwi(PacketType type)
    {
        for(Client cc : Main.Clients)
        {
            if (cc.id != this.id)
            {
                //All other clients
                if(type == PacketType.OtherPlayer_S)
                    Main.SendData(cc, type, id, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getStrength(), kInstance.getSpeed(), kInstance.getFlight(), kInstance.getSwag(), kInstance.getHunger(), kInstance.getMood(), kInstance.getEnergy());
                else if(type == PacketType.Disconnect_S)
                    Main.SendData(cc, type, id);
                else if(type == PacketType.KiwiPos_S)
                    Main.SendData(cc, type, id, kInstance.getX(), kInstance.getY());
            }
        }
    }

    private void sendPlayers()
    {
        for (Client cc : Main.Clients)
        {
            if (cc.id != this.id)
                Main.SendData(this, PacketType.OtherPlayer_S, cc.id, cc.kInstance.getName(), cc.kInstance.getHealth(), cc.kInstance.getMoney(), cc.kInstance.getStrength(), cc.kInstance.getSpeed(), cc.kInstance.getFlight(), cc.kInstance.getSwag(), cc.kInstance.getHunger(), cc.kInstance.getMood(), cc.kInstance.getEnergy());
        }
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
                ", password='" + password + '\'' + '}';
    }
}
