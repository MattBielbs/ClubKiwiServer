package com.clubkiwiserver;
import com.clubkiwiserver.Packet.*;

import java.net.*;

public class Main
{

    static Serializer s;

    public static void main(String[] args) throws Exception
    {
        s = new Serializer();
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while (true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);


            Packet p = s.Deserialize(receivePacket.getData());
            if (p == null)
                return;

            System.out.println("RECEIVED: \n" + p.getType().toString() + ": " + (String)p.getData(0) + ", " + (String)p.getData(1));
        //    InetAddress IPAddress = receivePacket.getAddress();
        //    int port = receivePacket.getPort();
        //    String capitalizedSentence = sentence.toUpperCase();
        //    sendData = capitalizedSentence.getBytes();
         //   DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
          //  serverSocket.send(sendPacket);
        }
    }
}
