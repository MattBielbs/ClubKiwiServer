package com.clubkiwiserver;
import com.clubkiwi.Character.Kiwi;
import com.clubkiwiserver.DataStructs.DispenserData;
import com.clubkiwiserver.DataStructs.KiwiData;
import com.clubkiwiserver.Packet.Packet;
import com.clubkiwiserver.Packet.PacketType;

import java.net.*;
import java.util.ArrayList;

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
    private KiwiData kInstance;

    public Client(int id, ClientState clientState, InetAddress IPAddress, int port)
    {
        this.id = id;
        this.clientState = clientState;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    //massive function might split it up later.
    public void OnDataReceive(Packet p)
    {
        //Make sure its not an empty or null packet.
        if (p == null || p.getAllData().length == 0)
            return;

        //Check the packet type
        if(p.getType() == PacketType.Login_C)
        {
            //Try and log the player in
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            KiwiData k = Main.dbHelper.Login(username, password);

            if(k == null)
            {
                Main.SendData(this, PacketType.Login_S, id, "Wrong username or password");
            }
            else
            {
                if(k.getHealth() <= 0)
                {
                    //your dead
                    Main.SendData(this, PacketType.CharacterDead, k.getName());
                }
                else
                {
                    //Worked send kiwi
                    Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getHunger(), id);

                    setClientState(ClientState.LoggedIn);
                    setUsername(username);
                    setPassword(password);
                    setkInstance(k);
                }
            }
        }
        else if(p.getType() == PacketType.CreateUser_C)
        {
            //Try and create a character using the login provided.
            String username = (String) p.getData(0);
            String password = (String) p.getData(1);
            KiwiData k = Main.dbHelper.CreateUser(username, password);

            if(k == null)
            {
                //failed send error
                Main.SendData(this, PacketType.CreateUser_S, id, "That username is already taken, please try again.");
            }
            else
            {
                //worked send default kiwi
                Main.SendData(this, PacketType.CharacterList_S, k.getName(), k.getHealth(), k.getMoney(), k.getHunger(), id);

                setClientState(ClientState.LoggedIn);
                setUsername(username);
                setPassword(password);
                setkInstance(k);
            }
        }
        else if(p.getType() == PacketType.KiwiUpdate_C)
        {
            //Update character
            Main.dbHelper.UpdateCharacter(this, (Double)p.getData(0), (Double)p.getData(1), (Double)p.getData(2));

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
            getkInstance().setX((int) p.getData(0));
            getkInstance().setY((int) p.getData(1));
            if(getkInstance().getCurrentRoom() != (int)p.getData(2))
            {
                //room switch event
                getkInstance().setCurrentRoom((int) p.getData(2));

                sendDespensersFromRoom();
            }


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
        else if(p.getType() == PacketType.WorldItemRemove)
        {
            for(Client c : Main.Clients)
            {
                //set it as hidden on server
                for(ArrayList<DispenserData> a :Main.worldItems.values())
                {
                    for(DispenserData d : a)
                    {
                        if(d.getID() == (int)p.getData(0))
                        {
                            d.setbVisible(false);
                        }
                    }
                }
                //tell clients its gone
                Main.SendData(c, PacketType.WorldItemRemove, p.getData(0));
            }
        }
    }

    public void sendDespensersFromRoom()
    {
        //send all dispensers to the kiwi based on this room.
        ArrayList<DispenserData> items = Main.worldItems.getOrDefault(kInstance.getCurrentRoom(), new ArrayList<>());
        for(DispenserData item : items)
        {
            if(item.isbVisible())
                Main.SendData(this, PacketType.WorldItemAdd, item.getID(), item.getX(), item.getY(), item.isbVisible());
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

    public int getPort()
    {
        return port;
    }

    public int getId()
    {
        return id;
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

    public KiwiData getkInstance()
    {
        return kInstance;
    }


    //every time this is set then the client should be updated.
    public void setkInstance(KiwiData kInstance)
    {
       this.kInstance = kInstance;
      // this could be optimised by checking to see if anything has actually changed

        if(kInstance.getHealth() <= 0)
            Main.SendData(this, PacketType.CharacterDead, kInstance.getName());
        else
            Main.SendData(this, PacketType.KiwiUpdate_S, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getHunger());

        //Tell you about dispensers in room
        sendDespensersFromRoom();

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
                    Main.SendData(cc, type, id, kInstance.getName(), kInstance.getHealth(), kInstance.getMoney(), kInstance.getHunger());
                else if(type == PacketType.Disconnect_S)
                    Main.SendData(cc, type, id);
                else if(type == PacketType.KiwiPos_S)
                    Main.SendData(cc, type, id, kInstance.getX(), kInstance.getY(), kInstance.getCurrentRoom());
            }
        }
    }

    private void sendPlayers()
    {
        for (Client cc : Main.Clients)
        {
            if (cc.id != this.id)
            {
                Main.SendData(this, PacketType.OtherPlayer_S, cc.id, cc.kInstance.getName(), cc.kInstance.getHealth(), cc.kInstance.getMoney(), cc.kInstance.getHunger());
                Main.SendData(this, PacketType.KiwiPos_S, cc.id, cc.kInstance.getX(), cc.kInstance.getY(), cc.kInstance.getCurrentRoom());
            }
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
