package app;

import srm.ReliableMulticastSocket;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Player {

    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;

    public Player(String name) {

        this.name = name;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Player && this.name.equals(((Player) o).name);
    }

    @Override
    public String toString() {
        return "Player = " + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
