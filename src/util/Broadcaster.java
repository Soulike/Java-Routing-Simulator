package util;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import static util.Converter.objectToByteArray;

public class Broadcaster
{
    public static <T extends Serializable> void broadcast(T object, DatagramSocket datagramSocket, List<Integer> portList) throws IOException
    {
        byte[] infoByteArray = objectToByteArray(object);
        byte[] buffer = new byte[infoByteArray.length];
        DatagramPacket packet = new DatagramPacket(buffer, infoByteArray.length);
        packet.setAddress(InetAddress.getLocalHost());
        packet.setData(infoByteArray);
        for (int port : portList)
        {
            packet.setPort(port);
            datagramSocket.send(packet);
        }
    }
}
