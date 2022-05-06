package app;
import javax.swing.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A room that keeps all the information of the game itself and all users inside.
 * Its unique identifier is the current host's name, and it is the only thing considered when comparing rooms.
 * The IP and port of a room should not change once decided, but in exchange for making the currentRoom object
 * final in the main class, these two fields are not final here. DO NOT CHANGE THEM.
 */
public class Room {
    // The unique identifier of the room is its current host's ID.
    public Player host;
    public String IP;
    public int port;
    public String roomName;
    public int timeLimit;
    public CopyOnWriteArrayList<String> dictionary = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Player> playerList = new CopyOnWriteArrayList<>();
    public int numRounds = 2;
    public int numTurn = 4;
    public ArrayList<ArrayList<String>> initWords = new ArrayList<>();
    public boolean inGame = false;
    public boolean lastRound = false;
    public int numPlayers = 1;

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

    public boolean allDone() {
        int turn = DrawandGuess.turn;
        if (playerList.size() < numPlayers) {
            return false;
        }
        for (Player player: playerList) {
            // guess on even turn
            if (turn % 2 == 0) {
                if (player.guessedList.size() < (turn + 2) / 2) {
                    return false;
                }
            } else {
                if (player.drawingList.size() < (turn + 1) / 2) {
                    return false;
                }
            }
        }
        return true;
    }

    public void generateInitWords() {
        Random random = new Random();
        int num = 3 * playerList.size();
        int[] ints = new int[num];

        for (int i = 0; i < num; i++) {
            ints[i] = random.nextInt(dictionary.size());
            for (int j = 0; j < i; j++) {
                if (ints[i] == ints[j]) {
                    i--;
                    break;
                }
            }
        }

        for (int i = 0; i < playerList.size(); i++) {
            ArrayList<String> iWords = new ArrayList<>();
            iWords.add(dictionary.get(ints[3 * i]));
            iWords.add(dictionary.get(ints[3 * i + 1]));
            iWords.add(dictionary.get(ints[3 * i + 2]));
            initWords.add(iWords);
        }
    }
}
