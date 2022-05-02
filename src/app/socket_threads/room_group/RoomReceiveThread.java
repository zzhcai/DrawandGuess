package app.socket_threads.room_group;

import app.DrawandGuess;
import app.MySocketFactory;
import app.Player;
import app.Room;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class RoomReceiveThread extends Thread {
    public volatile boolean interrupted = false;
    @Override
    public void run() {
        ReliableMulticastSocket socket = MySocketFactory.newInstance(DrawandGuess.currentRoom.IP, DrawandGuess.currentRoom.port);
        System.out.println("Room receive thread started at " + socket.getInetAddress());
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received at room: " + new String(p.getData()));
            Player player = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Player.class);
            if (player.name == null) {
                Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
                synchronized (DrawandGuess.currentRoom) {
                    DrawandGuess.currentRoom.roomName = room.roomName;
                    DrawandGuess.currentRoom.dictionary = room.dictionary;
                    DrawandGuess.currentRoom.host = room.host;
                    DrawandGuess.currentRoom.numRounds = room.numRounds;
                    DrawandGuess.currentRoom.playerList = room.playerList;
                    DrawandGuess.currentRoom.timeLimit = room.timeLimit;
                    DrawandGuess.currentRoom.notifyAll();
                }
            } else {
                synchronized (DrawandGuess.currentRoom) {
                    DrawandGuess.currentRoom.playerList.remove(player);
                    DrawandGuess.currentRoom.playerList.add(player);
                    DrawandGuess.currentRoom.notifyAll();
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
