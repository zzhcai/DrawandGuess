package app;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DrawandGuess {
    public final InetAddress LOBBYADDRESS = InetAddress.getByName("www.google.com");

    public DrawandGuess() throws UnknownHostException {
    }

    public static void main(String[] args) {
        WhiteBoardGUI.setUp();
    }
}
