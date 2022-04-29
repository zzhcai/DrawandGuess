package app;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

public class Room implements Comparable<Room>{
    public int id;
    public InetSocketAddress address;
    public int port;
    public String roomName;
    public int numPlayer;
    public int maxPlayer;
    private Player host;
    public int timeLimit;
    public ArrayList<String> dictionary = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();
    public int numRounds;
    public RoomAdvertiseThread thread;

    public Room() throws IOException {
        this.port = new Random().nextInt(10000) + 9000;
        this.address = new InetSocketAddress(randomIP(), port);
        this.roomName = "someone's room";
        this.numPlayer = 1;
        this.maxPlayer = 10;
        this.thread = new RoomAdvertiseThread();
        thread.setRoom(this);
        thread.start();
    }

    public Room(String roomName, int maxPlayer) throws IOException {
        this.port = new Random().nextInt(10000) + 9000;
        this.address = new InetSocketAddress(randomIP(), port);
        this.roomName = roomName;
        this.numPlayer = 1;
        this.maxPlayer = maxPlayer;
        this.thread = new RoomAdvertiseThread();
        thread.setRoom(this);
        thread.start();
    }

    public Room(String roomName, int numPlayer, int maxPlayer) {
        this.roomName = roomName;
        this.numPlayer = numPlayer;
        this.maxPlayer = maxPlayer;
    }

    private String randomIP() {
        Random r = new Random();
        return (r.nextInt(15)+224) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }


    @Override
    public int compareTo(Room o) {
        return o.id - this.id;
    }

    @Override
    public String toString() {
        return "{roomid=" + id +
                ", name=" + roomName +
                ", numPlayer=" + numPlayer +
                ", maxPlayer=" + maxPlayer + "}";
    }
}
