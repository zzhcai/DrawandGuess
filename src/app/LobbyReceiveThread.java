package app;

import srm.ReliableMulticastSocket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;

public class LobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;
    private final DefaultListModel<Room> dlm;
    public volatile boolean interrupted = false;

    public LobbyReceiveThread(DefaultListModel<Room> dlm) {
        this.dlm = dlm;
        int port = 9000;
        while (true) {
            try {
                socket = new ReliableMulticastSocket(port);
                socket.joinGroup(DrawandGuess.LOBBY_SOCKET_ADDRESS, null);
                break;
            } catch (IOException e) {
                if (socket == null) port++;
                else e.printStackTrace();
            }
        }
    }

    public void run() {
        System.out.println("Lobby receive thread started");
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received: " + new String(p.getData()));
            Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
            System.out.println("Received room: " + room.toString());
            synchronized (dlm) {
                //TODO remove inactive room
                if (!dlm.contains(room)) {
                    dlm.addElement(room);
                }
            }
        }
        System.out.println("Lobby receive thread closed");
    }
}
