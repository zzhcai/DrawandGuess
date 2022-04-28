package app;

import java.awt.*;
import java.net.InetAddress;

public class Player {

    private int id;
    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;
    public Room room = null;

    public Player(String name) {

        this.name = name;
    }

    public void joinRoom(InetAddress address) {

    }
}
