package com.clubkiwiserver.Packet;

/**
 * Decided to make this to contain all the needed info easily
 */
public class Packet
{
    private PacketType Type;
    private Object[] data;

    public Packet(PacketType type, Object[] data)
    {
        setType(type);
        setData(data);
    }

    public PacketType getType()
    {
        return Type;
    }

    public void setType(PacketType type)
    {
        Type = type;
    }

    public Object getData(int index)
    {
        if(index > data.length)
            throw new IndexOutOfBoundsException("Data packet does not contain that much data");

        return data[index];
    }

    public void setData(Object[] data)
    {
        this.data = data;
    }
}
