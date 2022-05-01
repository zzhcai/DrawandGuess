package app;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Room{
    // The unique identifier of the room is its current host's ID.
    public Player host;
    public String IP;
    public int port;
    public String roomName;
    public int maxPlayer;
    public int timeLimit;
    public ArrayList<String> dictionary = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();
    public int numRounds;

    public Room() {
        this.port = new Random().nextInt(10000) + 9000;
        this.IP = randomIP();
    }

    public String randomIP() {
        Random r = new Random();
        return (r.nextInt(15)+224) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Room && this.host.name.equals(((Room) o).host.name);
    }

    @Override
    public String toString() {
        return "{host=" + host.name +
                ", name=" + roomName +
                ", numPlayer=" + playerList.size() +
                ", maxPlayer=" + maxPlayer +
                ", address=" + IP + "port=" + port + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(host.name);
    }

    public InetSocketAddress getAddress() { return new InetSocketAddress(IP, port); }
}
