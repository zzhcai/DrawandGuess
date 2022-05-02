package app;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Room{
    public String hostId;
    public InetSocketAddress address;
    public int port;
    public String roomName;
    public int maxPlayer;
    public int timeLimit;
    public ArrayList<String> dictionary = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();
    public int numRounds;

    public Room(Player host, String roomName,int maxPlayer) {
        this.port = new Random().nextInt(10000) + 9000;
        this.address = new InetSocketAddress(randomIP(), port);
        this.hostId = host.name;
        playerList.add(host);
        this.roomName = roomName;
        this.maxPlayer = maxPlayer;
    }

    private String randomIP() {
        Random r = new Random();
        return (r.nextInt(15)+224) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Room && this.hostId.equals(((Room) o).hostId);
    }

    @Override
    public String toString() {
        return "{roomid=" + hostId +
                ", name=" + roomName +
                ", numPlayer=" + playerList.size() +
                ", maxPlayer=" + maxPlayer + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostId);
    }
}
