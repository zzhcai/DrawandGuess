package app;

import srm.ReliableMulticastSocket;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Player {

    private String id;
    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;
    public Room room = null;
    public static ReliableMulticastSocket socket;

    public Player() throws IOException {
        socket = new ReliableMulticastSocket(9000);
        this.id = String.valueOf(InetAddress.getLocalHost());
    }


    public Player(String name) {

        this.name = name;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void joinRoom(InetAddress address) {

    }
}
