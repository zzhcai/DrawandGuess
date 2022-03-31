package srm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;

public class ReliableMulticastSocket extends MulticastSocket {

    public ReliableMulticastSocket() throws IOException {
        init();
    }

    public ReliableMulticastSocket(int port) throws IOException {
        super(port);
        init();
    }

    public ReliableMulticastSocket(SocketAddress bindaddr) throws IOException {
        super(bindaddr);
        init();
    }

    private void init() {

    }

    @Override
    public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.joinGroup(mcastaddr, netIf);
    }

    @Override
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.leaveGroup(mcastaddr, netIf);
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        super.send(p);
    }

    @Override
    public void receive(DatagramPacket p) throws IOException {
        super.receive(p);
    }
}
