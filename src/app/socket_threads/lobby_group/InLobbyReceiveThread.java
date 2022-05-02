package app.socket_threads.lobby_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Instant;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread that constantly receives rooms' advertisement and updates the lobby accordingly.
 * Received data is kept in the concurrent map that shared with the main thread.
 * This thread should only run when the player is in the lobby panel.
 */
public class InLobbyReceiveThread extends Thread {
    private ReliableMulticastSocket socket;
    private final ConcurrentMap<Room, Instant> roomsLastUpdated;
    public volatile boolean interrupted = false;

    public InLobbyReceiveThread(ConcurrentMap<Room, Instant> roomsLastUpdated) {
        this.roomsLastUpdated = roomsLastUpdated;
        socket = MySocketFactory.newInstance(DrawandGuess.LOBBY_ADDRESS, DrawandGuess.LOBBY_PORT);
    }

    public void run() {
        System.out.println("Lobby receive thread started at: " + socket);
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received at lobby: " + new String(p.getData()));
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
