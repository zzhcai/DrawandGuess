package app;

import srm.ReliableMulticastSocket;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Objects;

public class Player implements Comparable<Player> {

    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;

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
