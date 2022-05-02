package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Player;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PlayerAdvertiseThread extends Thread {
    public volatile boolean isInterrupted = false;

    private int noHostCount = 0;
    private final int MAX_NO_HOST_COUNT = 3;

    @Override
    public void run() {
        System.out.println("Player advertise thread started at " + DrawandGuess.currentRoom.getAddress());
        ReliableMulticastSocket socket = MySocketFactory.newInstance(null, DrawandGuess.currentRoom.port);
        while (!isInterrupted) {
            ArrayList<Player> players = DrawandGuess.currentRoom.playerList;
            Collections.sort(players);
            // Check if it's time to become the new host
            if (players.size() > 0
                    && !players.contains(DrawandGuess.currentRoom.host)
                    && players.get(0).equals(DrawandGuess.self)) {
                noHostCount++;
                if (noHostCount >= MAX_NO_HOST_COUNT) {
                    synchronized (DrawandGuess.self) {
                        DrawandGuess.self.isHost = true;
                        DrawandGuess.self.notifyAll();
                    }
                    synchronized (DrawandGuess.currentRoom) {
                        DrawandGuess.currentRoom.host = DrawandGuess.self;
                        DrawandGuess.currentRoom.notifyAll();
                    }
                }
            } else noHostCount = 0;
            byte[] out = DrawandGuess.gson.toJson(DrawandGuess.self, Player.class).getBytes();
            try {
                socket.send(new DatagramPacket(out, out.length, DrawandGuess.currentRoom.getAddress()));
                System.out.println("sent at room: " + DrawandGuess.self);
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
            socket.leaveGroup(DrawandGuess.currentRoom.getAddress(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
