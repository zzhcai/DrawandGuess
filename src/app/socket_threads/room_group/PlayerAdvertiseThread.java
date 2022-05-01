package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Player;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;

public class PlayerAdvertiseThread extends Thread {
    public volatile boolean isInterrupted = false;
    @Override
    public void run() {
        System.out.println("Player advertise thread started at " + DrawandGuess.currentRoom);
        ReliableMulticastSocket socket = MySocketFactory.newInstance(DrawandGuess.currentRoom.address);
        while (!isInterrupted) {
            Collections.sort(DrawandGuess.currentRoom.playerList);
            // Check if it's time to become the new host
            synchronized (DrawandGuess.currentRoom) {
                if (!DrawandGuess.currentRoom.playerList.contains(DrawandGuess.currentRoom.host)
                        && DrawandGuess.currentRoom.playerList.get(0).equals(DrawandGuess.self)) {
                    synchronized (DrawandGuess.self) {
                        DrawandGuess.self.isHost = true;
                        DrawandGuess.self.notifyAll();
                    }
                }
            }
            byte[] out = DrawandGuess.gson.toJson(DrawandGuess.self, Player.class).getBytes();
            try {
                socket.send(new DatagramPacket(out, out.length, DrawandGuess.currentRoom.address));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Player advertise thread closed");
        try {
            socket.leaveGroup(DrawandGuess.currentRoom.address, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
