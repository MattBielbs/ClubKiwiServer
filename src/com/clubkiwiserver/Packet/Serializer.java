package com.clubkiwiserver.Packet;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Used to Serialize and Deserialize data between transmission and sending multiple objects in one.
 */
public class Serializer
{
    Map<Type, Integer> Types;

    public Serializer()
    {
        Types = new HashMap<Type, Integer>();
        Types.put(Boolean.class, 0);
        Types.put(Integer.class, 1);
        Types.put(String.class, 2);
        Types.put(Double.class, 3);
    }

    /*
    Serializes data for network transmission
     */
    public byte[] Serialize(PacketType type, Object... data)
    {
        try
        {
            //Setup the stream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream s = new DataOutputStream(stream);

            //Write the type
            s.writeByte(type.ordinal());

            //Write the numbers of args
            s.writeByte(data.length);

            //Loop through all the args and write type and data to stream
            for (Object d : data)
            {
                //Write the integer representation of the class
                Integer current = Types.get(d.getClass());
                s.writeByte(current);

                //Write the data itself
                switch (current)
                {
                    case 0:
                        s.writeBoolean((boolean) d);
                        break;
                    case 1:
                        s.writeInt((int) d);
                        break;
                    case 2:
                        s.writeUTF((String) d);
                        break;
                    case 3:
                        s.writeDouble((Double) d);
                        break;
                }
            }

            s.close();

            //Return the finished stream for sending to the client
            return stream.toByteArray();
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    /*
    Deserializes data after network transmission
     */
    public Packet Deserialize(byte[] data)
    {
        try
        {
            //Object holder
            ArrayList<Object> Items = new ArrayList<Object>();

            //Setup Streams
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            DataInputStream s = new DataInputStream(stream);

            byte current = 0;

            //Get the packet type
            PacketType p = PacketType.values()[s.readByte()];

            //Get the num of args
            byte count = s.readByte();

            //Loop throughout the args
            for (int i = 0; i < count; i++)
            {
                current = s.readByte();

                switch (current)
                {
                    case 0:
                        Items.add(s.readBoolean());
                        break;
                    case 1:
                        Items.add(s.readInt());
                        break;
                    case 2:
                        Items.add(s.readUTF());
                        break;
                    case 3:
                        Items.add(s.readDouble());
                        break;
                }
            }

            s.close();

            //Return the array of objects
            return new Packet(p, Items.toArray());
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
