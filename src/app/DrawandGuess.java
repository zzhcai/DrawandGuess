package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DrawandGuess {
    private static final int PORT = 9000;
    public static final InetSocketAddress LOBBYADDRESS = new InetSocketAddress("239.255.255.255", PORT);
    public static Player self;
    protected static Gson gson = new GsonBuilder().serializeNulls().create();


    public DrawandGuess() {
    }

    public static void main(String[] args) throws IOException {
        self = new Player();
        WhiteBoardGUI.setUp();
    }
}
