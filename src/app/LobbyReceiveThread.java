package app;

import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class LobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;

    public LobbyReceiveThread() throws IOException {
        socket = new ReliableMulticastSocket(9000);
        socket.joinGroup(new InetSocketAddress("239.255.255.255", 9000), null);
    }

    public void run() {
        while (true) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println(DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), String.class));
        }
    }
}
