package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class HostAdvertiseRoomThread extends Thread {
    public volatile boolean isInterrupted = false;

    // multicast room info to the room group every second
    public void run() {
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
        System.out.println("Host advertise room thread started at " + DrawandGuess.currentRoom);
        ReliableMulticastSocket socket = MySocketFactory.newInstance(null, DrawandGuess.currentRoom.port);
        while (!isInterrupted) {
            byte[] out = DrawandGuess.gson.toJson(DrawandGuess.currentRoom, Room.class).getBytes();
            try {
                socket.send(new DatagramPacket(out, out.length, DrawandGuess.currentRoom.getAddress()));
                System.out.println("sent at room: " + DrawandGuess.currentRoom);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Host advertise room thread closed");
        try {
            socket.leaveGroup(DrawandGuess.currentRoom.getAddress(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }

}
