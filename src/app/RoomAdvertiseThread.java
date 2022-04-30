package app;

import srm.ReliableMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class RoomAdvertiseThread extends Thread {
    private volatile Room room;
    private ReliableMulticastSocket socket;
    public RoomAdvertiseThread() throws IOException {
        socket = new ReliableMulticastSocket(9000);
        socket.joinGroup(new InetSocketAddress("239.255.255.255", 9000), null);
    }

    // multicast room info to lobby every second
    public void run() {
//        System.out.println(1);
        while (true) {
            while (room == null) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println(2);
            byte[] out = DrawandGuess.gson.toJson(room.toString()).getBytes();
            try {
                socket.send(new DatagramPacket(out, out.length, InetAddress.getByName("239.255.255.255"), 9000));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
