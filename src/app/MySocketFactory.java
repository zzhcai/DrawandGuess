package app;

import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MySocketFactory {
    /**
     * Construct a reliable multicast socket and join a specified multicast group.
     * If this socket joins any other multicast group, the port number of that group MUST match with this socket's port.
     * @param IP Inet address of the multicast group to join. If left null, this socket won't join any group.
     * @param port The port number of this Socket. Any multicast groups this socket joins in the future must share the
     *             same port number.
     */
    public static ReliableMulticastSocket newInstance(String IP, int port) {
        ReliableMulticastSocket socket = null;
        try {
            socket = new ReliableMulticastSocket(port);
            if (IP != null) socket.joinGroup(new InetSocketAddress(IP, port), null);
        } catch (IOException e) {
            System.err.println("Joining an already joined group.");// Nothing to worry about.
        }
        return socket;
    }
}
