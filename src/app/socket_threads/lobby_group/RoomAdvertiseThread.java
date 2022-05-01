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
 * This thread should only be active for the host of the room.
 */
public class RoomAdvertiseThread extends Thread {
    public volatile boolean isInterrupted = false;

    // multicast room info to lobby every second
    public void run() {
        while (!DrawandGuess.self.isHost) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("RoomAdvertiseThread: This player never became the host.");
                return;
            }
        }
        System.out.println("Room advertise thread started at " + DrawandGuess.currentRoom.toString());
        ReliableMulticastSocket socket = MySocketFactory.newInstance(DrawandGuess.LOBBY_SOCKET_ADDRESS);
        while (!isInterrupted) {
            byte[] out = DrawandGuess.gson.toJson(DrawandGuess.currentRoom, Room.class).getBytes();
            try {
                socket.send(new DatagramPacket(out, out.length, InetAddress.getByName(DrawandGuess.LOBBY_ADDRESS), DrawandGuess.LOBBY_PORT));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Room advertise thread closed");
        try {
            socket.leaveGroup(DrawandGuess.LOBBY_SOCKET_ADDRESS, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
