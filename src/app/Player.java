package app;


import app.UI_util.ColorLine;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A player in the game.
 * We assume unique name by appending a random number to it.
 * Comparing players only checks if they have the same name.
 */
public class Player implements Comparable<Player> {

    public String name;
    public boolean isHost = false;
    public boolean ready = false;
    // This field is actively added when this player object is received as a multicast message.
    public Long lastActive;
    public ArrayList<String> guessedList = new ArrayList<>();
    public ArrayList<ArrayList<ColorLine>> drawingList = new ArrayList<>();
    public boolean inGame = false;

    @Override
    public boolean equals(Object o) {
        return o instanceof Player && this.name.equals(((Player) o).name);
    }

    @Override
    public String toString() {
        return "Player: " + name + " isHost: " + isHost + " ready: " + ready;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Player o) {
        return this.name.compareTo(o.name);
    }

    public void roundStart() {
        guessedList.clear();
        drawingList.clear();
    }


    public boolean allReceived(int turn) {
        // guess on even turn
        boolean received = true;

        return received;
    }
}



