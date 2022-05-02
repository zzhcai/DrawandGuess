package app;

import srm.ReliableMulticastSocket;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Objects;

/**
 * A player in the game.
 * We assume unique name by appending a random number to it.
 * Comparing players only checks if they have the same name.
 */
public class Player implements Comparable<Player> {

    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;
    // This field is actively added when this player object is received as a multicast message.
    public Long lastActive;

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
}
