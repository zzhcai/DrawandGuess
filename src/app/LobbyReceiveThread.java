package app;

import srm.ReliableMulticastSocket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class LobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;
    private final ConcurrentMap<Room, Instant> roomsLastUpdated;
    public volatile boolean interrupted = false;

    public LobbyReceiveThread(ConcurrentMap<Room, Instant> roomsLastUpdated) {
        this.roomsLastUpdated = roomsLastUpdated;
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
            synchronized (roomsLastUpdated) {
                roomsLastUpdated.remove(room);
                roomsLastUpdated.put(room, Instant.now());
            }
        }
        System.out.println("Lobby receive thread closed");
    }
}
