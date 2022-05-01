package app;

import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MySocketFactory {
    public static ReliableMulticastSocket newInstance(InetSocketAddress groupAddress) {
        ReliableMulticastSocket socket = null;
        int port = 9000;
        while (true) {
            try {
                socket = new ReliableMulticastSocket(port);
                socket.joinGroup(groupAddress, null);
                break;
            } catch (IOException e) {
                if (socket == null) port++;
                else break;// Joining the same group, nothing to worry about
            }
        }
        return socket;
    }
}
