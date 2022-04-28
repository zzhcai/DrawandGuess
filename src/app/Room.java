package app;

import java.net.InetAddress;
import java.util.ArrayList;

public class Room implements Comparable<Room>{
    public int id;
    public InetAddress address;
    public String roomName;
    public int numPlayer;
    public int maxPlayer;
    private Player host;
    public int timeLimit;
    public ArrayList<String> dictionary = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();
    public int numRounds;

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


    @Override
    public int compareTo(Room o) {
        return o.id - this.id;
    }
}
