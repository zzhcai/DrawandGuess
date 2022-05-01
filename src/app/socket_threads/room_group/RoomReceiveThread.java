package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Player;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Instant;

public class RoomReceiveThread extends Thread {
    public volatile boolean interrupted = false;
    @Override
    public void run() {
        ReliableMulticastSocket socket = MySocketFactory.newInstance(DrawandGuess.currentRoom.address);
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received: " + new String(p.getData()));
            Player player = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Player.class);
            if (player.name == null) {
                Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
            } else {
                synchronized (DrawandGuess.currentRoom) {
                    DrawandGuess.currentRoom.playerList.remove(player);
                    DrawandGuess.currentRoom.playerList.add(player);
                }
            }
        }
        System.out.println("Room receive thread closed");
        try {
            socket.leaveGroup(DrawandGuess.LOBBY_SOCKET_ADDRESS, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
