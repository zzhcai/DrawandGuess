package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DrawandGuess {
    public static final int LOBBY_PORT = 9000;
    public static final String LOBBY_ADDRESS = "239.255.255.255";
    public static final InetSocketAddress LOBBY_SOCKET_ADDRESS = new InetSocketAddress(LOBBY_ADDRESS, LOBBY_PORT);
    public static final Player self = new Player();
    public static final Room currentRoom = new Room();
    public static Gson gson = new GsonBuilder().serializeNulls().create();
    public static final int PLAYER_TIMEOUT = 1500;

    public static void main(String[] args) {
        WhiteBoardGUI.setUp();
    }
}
