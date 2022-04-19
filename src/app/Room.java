package app;

import java.net.InetAddress;

public class Room {
    public int id;
    public InetAddress address;
    public String roomName;
    public int numPlayer;
    public int maxPlayer;
//    private Host host;

    public Room(String roomName, int numPlayer, int maxPlayer) {
        this.roomName = roomName;
        this.numPlayer = numPlayer;
        this.maxPlayer = maxPlayer;
    }
}
