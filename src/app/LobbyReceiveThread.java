package app;

import srm.Message;
import srm.ReliableMulticastSocket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class LobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;
    private volatile DefaultListModel<Room> dlm;

    public LobbyReceiveThread(DefaultListModel<Room> dlm) {
        this.dlm = dlm;
        int port = 9000;
        while (true) {
            try {
                socket = new ReliableMulticastSocket(port);
                socket.joinGroup(DrawandGuess.LOBBYADDRESS, null);
                break;
            } catch (IOException e) {
                if (socket == null) port++;
                else e.printStackTrace();
            }
        }
//        dlm.addElement(new Room());
    }

    public void run() {
        while (true) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
            System.out.println("Received room: " + room.toString());
            dlm.addElement(room);
        }
    }
}
