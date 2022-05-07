package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Player;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This thread periodically multicasts the player information to the room, and only the host would
 * also use this thread to multicast room information to the whole room as well.
 * This thread also periodically (the same period as multicasting) detects if the host is absent,
 * and decide if this player can be the new host.
 */
public class InRoomAdvertiseThread extends Thread {
    public volatile boolean isInterrupted = false;

    // Only become the host after MAX_NO_HOST_COUNT times updates without a host.
    private int noHostCount = 0;

    @Override
    public void run() {
        ReliableMulticastSocket socket = MySocketFactory.newInstance(null, DrawandGuess.currentRoom.port);
        while (!isInterrupted) {
            synchronized (DrawandGuess.currentRoom) {
                // Check if it's time to become the new host
                if (DrawandGuess.currentRoom.playerList.size() > 0
                        && !DrawandGuess.currentRoom.playerList.contains(DrawandGuess.currentRoom.host)
                        && DrawandGuess.currentRoom.playerList.get(0).equals(DrawandGuess.self)) {
                    noHostCount++;
                    int MAX_NO_HOST_COUNT = 3;
                    if (noHostCount >= MAX_NO_HOST_COUNT) {
                        synchronized (DrawandGuess.self) {
                            DrawandGuess.self.isHost = true;
                            DrawandGuess.self.ready = true;
                            DrawandGuess.self.notifyAll();
                        }
                        DrawandGuess.currentRoom.host = DrawandGuess.self;
                        DrawandGuess.currentRoom.notifyAll();
                    }
                } else noHostCount = 0;
            }

            // Multicast player and room information
            byte[] playerOut = DrawandGuess.gson.toJson(DrawandGuess.self, Player.class).getBytes();
            try {
                socket.send(new DatagramPacket(playerOut, playerOut.length, DrawandGuess.currentRoom.getAddress()));
                if (DrawandGuess.self.isHost) {
                    synchronized (DrawandGuess.currentRoom) {
                        byte[] roomOut = DrawandGuess.gson.toJson(DrawandGuess.currentRoom, Room.class).getBytes();
                        socket.send(new DatagramPacket(roomOut, roomOut.length, DrawandGuess.currentRoom.getAddress()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }
}
