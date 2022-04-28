package app;

import java.net.InetAddress;
import java.util.ArrayList;

public class Room {
    public int id;
    public InetAddress address;
    public String roomName;
    public int numPlayer;
    public int maxPlayer;
//    private Host host;
    public int timeLimit;
    public ArrayList<String> dictionary = new ArrayList<>();

    public Room() {
        this.roomName = "someone's room";
        this.numPlayer = 1;
        this.maxPlayer = 10;
    }

    public Room(String roomName, int numPlayer, int maxPlayer) {
        this.roomName = roomName;
        this.numPlayer = numPlayer;
        this.maxPlayer = maxPlayer;
    }
}
