package app;

import srm.ReliableMulticastSocket;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Player {

    public String name;
    private String[] words;
    private String[] guesses;
    private Graphics[] drawings;
    public boolean isHost = false;
    public boolean ready = false;
    public static ReliableMulticastSocket socket;

    public Player() throws IOException {
        socket = new ReliableMulticastSocket(9000);
    }


    public Player(String name) {

        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
