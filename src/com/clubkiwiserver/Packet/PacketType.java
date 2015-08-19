package com.clubkiwiserver.Packet;

/**
 * Created by Mathew on 8/2/2015.
 */
public enum PacketType
{//C=client, S=server
    //trevor says should be uppercase rip
    Connect,
    CreateUser_S,
    CreateUser_C,
    Login_S,
    Login_C,
    CharacterList_S,
    CharacterList_C,
    KiwiUpdate_S,
    KiwiUpdate_C,
    Disconnect
}
