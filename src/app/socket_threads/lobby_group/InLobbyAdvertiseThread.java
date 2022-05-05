package app.socket_threads.lobby_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * A thread that constantly advertises the room's existence to the lobby.
 * This thread would wait until this player becomes the host of the room.
 */
public class InLobbyAdvertiseThread extends Thread {
    public volatile boolean isInterrupted = false;

    // multicast room info to lobby every second
    public void run() {
        // Check if this player has become the host of the room
        synchronized (DrawandGuess.self) {
            while (!DrawandGuess.self.isHost) {
                try {
                    DrawandGuess.self.wait();
                } catch (InterruptedException e) {
                    System.err.println("RoomAdvertiseThread: This player never became the host.");
                    return;
                }
                DrawandGuess.self.notifyAll();
            }
        }

        System.out.println("Room advertise thread started at " + DrawandGuess.currentRoom);
        ReliableMulticastSocket socket = MySocketFactory.newInstance(null, DrawandGuess.LOBBY_PORT);
        // Multicast this room to the lobby every second
        while (!isInterrupted) {
            synchronized (DrawandGuess.currentRoom) {
                byte[] out = DrawandGuess.gson.toJson(DrawandGuess.currentRoom, Room.class).getBytes();
                try {
                    socket.send(new DatagramPacket(out, out.length, InetAddress.getByName(DrawandGuess.LOBBY_ADDRESS), DrawandGuess.LOBBY_PORT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Room advertise thread closed");
        socket.close();
    }
}
