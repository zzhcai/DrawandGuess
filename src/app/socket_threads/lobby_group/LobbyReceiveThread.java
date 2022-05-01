package app.socket_threads.lobby_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Room;
import srm.ReliableMulticastSocket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread that constantly receives rooms' advertisement and updates the lobby accordingly.
 * This thread should only run when the player is in the lobby panel.
 */
public class LobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;
    private final ConcurrentMap<Room, Instant> roomsLastUpdated;
    public volatile boolean interrupted = false;

    public LobbyReceiveThread(ConcurrentMap<Room, Instant> roomsLastUpdated) {
        this.roomsLastUpdated = roomsLastUpdated;
        socket = MySocketFactory.newInstance(DrawandGuess.LOBBY_SOCKET_ADDRESS);
    }

    public void run() {
        System.out.println("Lobby receive thread started");
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received: " + new String(p.getData()));
            Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
            synchronized (roomsLastUpdated) {
                roomsLastUpdated.remove(room);
                roomsLastUpdated.put(room, Instant.now());
            }
        }
        System.out.println("Lobby receive thread closed");
        try {
            socket.leaveGroup(DrawandGuess.LOBBY_SOCKET_ADDRESS, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
