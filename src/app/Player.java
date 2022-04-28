package app;

import java.awt.*;
import java.net.InetAddress;

public class Player {

    private int id;
    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost;
    public boolean ready = false;

    public Player(String name) {
        this.name = name;
        this.isHost = false;
    }

    public void joinRoom(InetAddress address) {

    }
}
