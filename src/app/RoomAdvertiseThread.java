package app;

import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RoomAdvertiseThread extends Thread {
    private final Room room;
    private ReliableMulticastSocket socket;
    public volatile boolean isInterrupted = false;

    public RoomAdvertiseThread(Room room){
        this.room = room;
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

    // multicast room info to lobby every second
    public void run() {
        System.out.println("Room advertise thread started at " + room.toString());
        while (!isInterrupted) {
            byte[] out = DrawandGuess.gson.toJson(room, Room.class).getBytes();
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
    }
}
