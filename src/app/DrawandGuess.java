package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DrawandGuess {
    public static final int LOBBY_PORT = 9000;
    public static final String LOBBY_ADDRESS = "239.255.255.255";
    public static final InetSocketAddress LOBBY_SOCKET_ADDRESS = new InetSocketAddress(LOBBY_ADDRESS, LOBBY_PORT);
    public static Player self;
    public static Room currentRoom;
    protected static Gson gson = new GsonBuilder().serializeNulls().create();


    public DrawandGuess() {
    }

    public static void main(String[] args) throws IOException {
        WhiteBoardGUI.setUp();
    }
}
