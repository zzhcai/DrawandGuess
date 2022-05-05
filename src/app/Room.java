package app;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A room that keeps all the information of the game itself and all users inside.
 * Its unique identifier is the current host's name, and it is the only thing considered when comparing rooms.
 * The IP and port of a room should not change once decided, but in exchange for making the currentRoom object
 * final in the main class, these two fields are not final here. DO NOT CHANGE THEM.
 */
public class Room{
    // The unique identifier of the room is its current host's ID.
    public Player host;
    public String IP;
    public int port;
    public String roomName;
    public int timeLimit;
    public CopyOnWriteArrayList<String> dictionary = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Player> playerList = new CopyOnWriteArrayList<>();
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
                ", maxTime=" + timeLimit +
                ", address=" + IP + "port=" + port + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(host.name);
    }

    public InetSocketAddress getAddress() { return new InetSocketAddress(IP, port); }
}
